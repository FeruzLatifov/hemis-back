package uz.hemis.service.classifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.exception.BusinessRuleException;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.classifier.HEducationType;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.HSpecialityYear;
import uz.hemis.domain.entity.classifier.ReviewStatus;
import uz.hemis.domain.repository.HEducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.domain.repository.HSpecialityYearRepository;
import uz.hemis.service.classifier.dto.ClassifierOptionDto;
import uz.hemis.service.classifier.dto.SpecialityCreateDto;
import uz.hemis.service.classifier.dto.SpecialityClassifierDistResponse;
import uz.hemis.service.classifier.dto.SpecialityDistItemDto;
import uz.hemis.service.classifier.dto.SpecialityDuplicateCheckDto;
import uz.hemis.service.classifier.dto.SpecialityDuplicateItemDto;
import uz.hemis.service.classifier.dto.SpecialityNodeDto;
import uz.hemis.service.classifier.dto.SpecialityRowDto;
import uz.hemis.service.classifier.dto.SpecialityUpdateDto;
import uz.hemis.service.outbox.OutboxEventPublisher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Unified speciality classifier curation service ({@code h_speciality}).
 *
 * <p>Ministry-side reference data — <strong>NOT tenant-scoped</strong>: the classifier
 * is one global source of truth distributed unchanged to all 224 OTMs, so access is
 * governed only by the permission dimension ({@code @PreAuthorize} at the controller),
 * never by {@code AccessScope}. Reads run on the replica (class-level {@code readOnly});
 * {@link #update} overrides to master.</p>
 *
 * <p>The tree is built in-memory from the {@code parent_id} self-reference; years
 * ({@link HSpecialityYear}) are batch-loaded to avoid N+1.</p>
 *
 * <p><strong>Education type</strong> is a code ('11'=Bakalavr, '12'=Magistr) FK into the
 * frozen {@code hemishe_h_education_type} classifier (the same {@code Student._education_type}
 * references). Its display name is resolved once per read via {@link #educationTypeNames()}
 * (5 static rows) and threaded into the DTO mappers alongside the year map — no N+1.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HSpecialityService {

    /** Apostrophe variants folded to a space in the search key (mirrors the ETL {@code fold()}). */
    private static final String APOSTROPHES = "'’ʻʼ‘`";

    /** Education types this classifier admits (mirrors the V018 CHECK): '11'=Bakalavr, '12'=Magistr. */
    private static final Set<String> ALLOWED_EDUCATION_TYPES = Set.of("11", "12");

    /** Fixed taxonomy depth: Bilim sohasi → Ta'lim sohasi → Yo'nalish → Ichki yo'nalish. */
    private static final int LEVELS_MAX = 4;


    private final HSpecialityRepository repository;
    private final HSpecialityYearRepository yearRepository;
    private final HEducationTypeRepository educationTypeRepository;
    private final OutboxEventPublisher outboxPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    // =====================================================
    // READ (REPLICA)
    // =====================================================

    /**
     * Full hierarchical tree for one education type (bachelor/master), or all types when
     * {@code educationType} is {@code null}. A node whose parent is absent from the active set is
     * surfaced as a root (never silently dropped).
     *
     * <p>When {@code year} is supplied, the tree is pruned to the edition of that year: a leaf
     * is kept iff it carries the year, and a branch is kept iff it has a kept descendant — so the
     * ancestor categories of a matching speciality survive while empty branches drop out.</p>
     */
    public List<SpecialityNodeDto> getTree(String educationType, Integer year) {
        return getTreeFiltered(educationType, null, null, year);
    }

    /**
     * Filtered tree for the Excel export — keeps the export honest ("what you see is what you
     * export"). Any subset of {@code reviewStatus} / free-text {@code q} / {@code year} may be
     * applied; a row is kept iff it matches the filter OR has a kept descendant, so the ancestor
     * categories of a match survive and the hierarchy stays intact. All-null ⇒ the whole classifier.
     */
    public List<SpecialityNodeDto> getTreeFiltered(String educationType, ReviewStatus reviewStatus,
                                                   String q, Integer year) {
        log.debug("Building speciality tree: educationType={}, reviewStatus={}, q={}, year={}",
                educationType, reviewStatus, q, year);
        List<HSpeciality> all = repository.findAllFiltered(educationType, null); // all statuses — ancestors needed
        if (all.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Integer>> years = loadYears(all);

        boolean anyFilter = year != null || reviewStatus != null || (q != null && !q.isBlank());
        if (anyFilter) {
            all = retainMatchingBranches(all, years, year, reviewStatus, q);
            if (all.isEmpty()) {
                return List.of();
            }
        }
        return buildTree(all, years, educationTypeNames());
    }

    /** Assemble the parent→children tree from a flat set, roots sorted in display order. */
    private List<SpecialityNodeDto> buildTree(List<HSpeciality> all, Map<UUID, List<Integer>> years,
                                              Map<String, String> eduNames) {
        Set<UUID> presentIds = all.stream().map(HSpeciality::getId).collect(Collectors.toSet());

        Map<UUID, List<HSpeciality>> byParent = all.stream()
                .filter(s -> s.getParentId() != null && presentIds.contains(s.getParentId()))
                .collect(Collectors.groupingBy(HSpeciality::getParentId));

        return all.stream()
                .filter(s -> s.getParentId() == null || !presentIds.contains(s.getParentId()))
                .sorted(displayOrder(years))
                .map(root -> toNode(root, byParent, years, eduNames))
                .toList();
    }

    /**
     * Paginated flat list for the curation grid — filter by {@code educationType} (bachelor/master),
     * {@code reviewStatus} ({@code NEEDS_REVIEW} = "to'g'rilash kerak"), free-text {@code q},
     * and edition {@code year} (a row matches iff it carries that year).
     *
     * <p>Ordered in the SAME canonical order as the tree + Excel export — newest edition year first,
     * then ascending numeric code (via {@link #displayOrder}) — so all three views agree row-for-row.
     * That order is not expressible as a portable JPQL {@code ORDER BY} (numeric cast of a string code
     * plus max-of-child-year), so the flat set is sorted in memory with the tree's own comparator and
     * paged here. Set size (~5.4k active rows) makes this equivalent in cost to building the tree.</p>
     */
    public Page<SpecialityRowDto> list(String educationType, ReviewStatus reviewStatus, String q,
                                       Integer year, Pageable pageable) {
        log.debug("Listing specialities: educationType={}, reviewStatus={}, q={}, year={}",
                educationType, reviewStatus, q, year);
        List<HSpeciality> all = repository.findAllFiltered(educationType, reviewStatus); // active + type + status
        Map<UUID, List<Integer>> years = loadYears(all);
        Map<String, String> eduNames = educationTypeNames();
        String rawLower = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);

        List<HSpeciality> matched = all.stream()
                .filter(s -> listMatches(s, years, year, rawLower))
                .sorted(displayOrder(years))
                .toList();

        int from = (int) Math.min(pageable.getOffset(), matched.size());
        int to = Math.min(from + pageable.getPageSize(), matched.size());
        List<SpecialityRowDto> content = matched.subList(from, to).stream()
                .map(s -> toRow(s, years, eduNames))
                .toList();
        return new PageImpl<>(content, pageable, matched.size());
    }

    /** Flat-list filter mirroring the former {@code search()} @Query (status already applied upstream). */
    private static boolean listMatches(HSpeciality s, Map<UUID, List<Integer>> years, Integer year, String rawLower) {
        if (year != null) {
            List<Integer> ys = years.get(s.getId());
            if (ys == null || !ys.contains(year)) {
                return false;
            }
        }
        if (rawLower != null) {
            boolean hit = (s.getNameUz() != null && s.getNameUz().toLowerCase(Locale.ROOT).contains(rawLower))
                    || (s.getCode() != null && s.getCode().toLowerCase(Locale.ROOT).contains(rawLower))
                    || (s.getNameSearch() != null && s.getNameSearch().contains(rawLower))
                    || s.getId().toString().contains(rawLower); // UUID search (id is lower-case hex)
            if (!hit) {
                return false;
            }
        }
        return true;
    }

    /**
     * Distinct edition years available across the active classifier, newest first — the option set
     * for the year-filter dropdown. Optionally scoped to one education type (bachelor/master).
     */
    public List<Integer> availableYears(String educationType) {
        log.debug("Listing available speciality years: educationType={}", educationType);
        return repository.findDistinctYears(educationType);
    }

    /** Single node with its resolved years and direct children. 404 if missing. */
    public SpecialityNodeDto getById(UUID id) {
        HSpeciality s = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HSpeciality", "id", id));
        List<HSpeciality> kids = repository.findByParentId(id);

        List<HSpeciality> everything = new ArrayList<>(kids);
        everything.add(s);
        Map<UUID, List<Integer>> years = loadYears(everything);
        Map<String, String> eduNames = educationTypeNames();

        List<SpecialityNodeDto> children = kids.stream()
                .sorted(displayOrder(years))
                .map(c -> toLeaf(c, years, eduNames))
                .toList();
        return toNodeWith(s, years, children, eduNames);
    }

    /**
     * Advisory duplicate lookup for the manual add form: existing active rows whose {@code code}
     * equals the entered code OR whose folded name equals the entered name, scoped to {@code educationType}.
     * It NEVER blocks a create — {@code code} is intentionally non-unique. A match under
     * {@code parentId} is flagged as a sibling collision (the strongest "you probably didn't mean
     * to add this again" signal). Empty (both terms blank) short-circuits with no matches.
     */
    public SpecialityDuplicateCheckDto findDuplicates(String code, String name,
                                                      String educationType, UUID parentId) {
        String c = blankToNull(code);
        String ns = foldSearch(blankToNull(name)); // fold the name exactly like name_search was seeded
        if (c == null && ns == null) {
            return new SpecialityDuplicateCheckDto(false, false, false, List.of());
        }
        List<HSpeciality> rows = repository.findDuplicates(educationType, c, ns);
        Map<UUID, List<Integer>> years = loadYears(rows);
        Map<String, String> eduNames = educationTypeNames();
        List<SpecialityDuplicateItemDto> matches = rows.stream()
                .limit(25)
                .map(s -> {
                    boolean codeMatch = c != null && c.equals(s.getCode());
                    boolean nameMatch = ns != null && ns.equals(s.getNameSearch());
                    boolean sameParent = parentId != null && parentId.equals(s.getParentId());
                    return new SpecialityDuplicateItemDto(
                            s.getId().toString(), s.getCode(), s.getNameUz(),
                            s.getEducationType(), eduName(s.getEducationType(), eduNames),
                            s.getReviewStatus() != null ? s.getReviewStatus().getValue() : null,
                            s.getHierarchyLevel(), years.getOrDefault(s.getId(), List.of()),
                            codeMatch, nameMatch, sameParent);
                })
                .toList();
        boolean codeExists = matches.stream().anyMatch(SpecialityDuplicateItemDto::codeMatch);
        boolean nameExists = matches.stream().anyMatch(SpecialityDuplicateItemDto::nameMatch);
        // Literal twin: an active row with the SAME name AND the SAME code (null == null). Ignores
        // parent/type-position — the same code+name is one node, so re-adding it anywhere is a dup.
        boolean exactDuplicate = rows.stream().anyMatch(s -> {
            boolean sameName = ns != null && ns.equals(s.getNameSearch());
            boolean sameCode = c == null ? s.getCode() == null : c.equals(s.getCode());
            return sameName && sameCode;
        });
        return new SpecialityDuplicateCheckDto(codeExists, nameExists, exactDuplicate, matches);
    }

    /**
     * Full APPROVED distributable snapshot (FLAT v1) for the {@code api-university} OTM bootstrap
     * pull. Cached as global reference data (evicted on {@link #update}). {@code educationType} nullable
     * (both bachelor + master). Uses the SAME predicate as the PUSH guard so both channels agree.
     */
    @Cacheable(value = "specialityDistribution", key = "#educationType != null ? #educationType : 'ALL'")
    public SpecialityClassifierDistResponse getDistribution(String educationType) {
        log.debug("Building speciality distribution snapshot: educationType={}", educationType);
        List<HSpeciality> rows = repository.findAllForDistribution(educationType);
        Map<UUID, List<Integer>> years = loadYears(rows);
        Map<String, String> eduNames = educationTypeNames();
        List<SpecialityDistItemDto> data = rows.stream().map(s -> toDistItem(s, years, eduNames)).toList();
        long version = repository.sumDistributionVersion(educationType);
        return new SpecialityClassifierDistResponse(true, "OK", distTitle(educationType), version, data.size(), data);
    }

    /**
     * OTM-facing classifier title — matches the old-hemis {@code ClassifiersServiceBean} registration
     * (apostrophes normalized to the project-standard straight {@code '}). Univer stores it as the
     * classifier's display name.
     */
    private static String distTitle(String educationType) {
        if ("11".equals(educationType)) return "Bakalavriat ta'lim yo'nalishlari";
        if ("12".equals(educationType)) return "Magistratura mutaxassisliklari";
        return "Mutaxassisliklar klassifikatori";
    }

    // =====================================================
    // WRITE (MASTER)
    // =====================================================

    /**
     * Curate a speciality: fix code/name/type/years and optionally promote
     * {@code NEEDS_REVIEW → APPROVED}. Years are fully replaced when supplied.
     *
     * <p>Additively distributes the curated row to the 224 OTMs via the modern PUSH path
     * (outbox {@code aggregate_type="classifier"} → webhook fanout) — but ONLY for
     * distributable rows (APPROVED + code-bearing + active), the same predicate the
     * {@code api-university} bootstrap pull uses. The frozen legacy classifier pull is untouched.</p>
     */
    @Transactional
    @CacheEvict(value = "specialityDistribution", allEntries = true)
    public SpecialityNodeDto update(UUID id, SpecialityUpdateDto dto) {
        log.info("Updating speciality: id={}, reviewStatus={}", id, dto.reviewStatus());
        HSpeciality s = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HSpeciality", "id", id));

        // Capture pre-mutation distribution state so a demotion (APPROVED→NEEDS_REVIEW),
        // deactivation, or code-clear of an already-distributed row can be RETRACTED from OTMs.
        boolean wasDistributable = isDistributable(s);
        String priorCode = s.getCode();

        // Partial update: an omitted (null) optional field is LEFT UNCHANGED; only an explicitly
        // supplied value (including "" → cleared) overwrites. nameUz is required, so it always applies.
        if (dto.code() != null) {
            s.setCode(blankToNull(dto.code()));
        }
        s.setNameUz(dto.nameUz().trim());
        // name_search is DB-generated (V018) — no longer set here.
        if (dto.nameOz() != null) {
            s.setNameOz(blankToNull(dto.nameOz()));
        }
        if (dto.nameRu() != null) {
            s.setNameRu(blankToNull(dto.nameRu()));
        }
        if (dto.nameEn() != null) {
            s.setNameEn(blankToNull(dto.nameEn()));
        }
        if (dto.educationType() != null) {
            s.setEducationType(validateEducationType(dto.educationType()));
        }
        // Placement — mirror the create form (depth + parent). null level = leave placement untouched.
        if (dto.hierarchyLevel() != null) {
            applyPlacement(s, dto.hierarchyLevel(), dto.parentId());
        }
        if (dto.reviewStatus() != null) {
            ReviewStatus newStatus = ReviewStatus.fromValue(dto.reviewStatus());
            // Segregation of duties: promoting NEEDS_REVIEW → APPROVED brings a hand-entered/
            // unverified row into OTM distribution, so it needs the dedicated .approve capability
            // on top of .edit. Both are ministry curation roles only — machine roles like OTM_API
            // are read-only on classifiers (S004 grants view only + revokes writes; S016), so no
            // OTM token can edit or promote. Plain edits and demotions (APPROVED → NEEDS_REVIEW)
            // stay on .edit.
            if (newStatus == ReviewStatus.APPROVED
                    && s.getReviewStatus() != ReviewStatus.APPROVED
                    && !currentUserHasAuthority("classifiers.speciality.approve")) {
                throw new AccessDeniedException(
                        "Promoting a speciality to APPROVED requires 'classifiers.speciality.approve'");
            }
            s.setReviewStatus(newStatus);
        }
        validateYears(dto.years());
        repository.save(s);

        if (dto.years() != null) {
            replaceYears(id, dto.years());
            // Years live in the child table h_speciality_year, so a years-ONLY edit leaves `s` clean
            // → @Version wouldn't bump → SUM(version) (the OTM cache-bust) would miss the year change.
            // Force a version increment so ANY distributed-data change (years included) is detectable
            // by the OTM (no reliance on Univer's always-refresh). Robust regardless of field values.
            entityManager.lock(s, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        }
        log.info("Speciality updated: id={}", id);

        distribute(s, wasDistributable, priorCode);
        return getById(id);
    }

    /**
     * Re-place a row to the chosen depth + parent, mirroring the create form. The parent must exist,
     * share the row's education type, and sit exactly one level above the target depth. Guards:
     * <ul>
     *   <li><b>cycle</b> — the new parent may not be the row itself or one of its descendants;</li>
     *   <li><b>overflow</b> — the row's subtree must still fit the fixed 4-level taxonomy after the
     *       move ({@code targetLevel + subtreeDepth <= 4}).</li>
     * </ul>
     * A depth change is cascaded to every descendant (their display {@code hierarchy_level} shifts by
     * the same delta; the authoritative {@code parent_id} links are untouched). Re-pointing to the
     * current depth + parent is a harmless no-op.
     */
    private void applyPlacement(HSpeciality s, int targetLevel, UUID newParentId) {
        HSpeciality newParent = null;
        if (targetLevel == 1) {
            if (newParentId != null) {
                throw new BusinessRuleException("SPECIALITY_ROOT_HAS_NO_PARENT",
                        "A top-level (level 1) speciality has no parent");
            }
        } else {
            if (newParentId == null) {
                throw new BusinessRuleException("SPECIALITY_PARENT_REQUIRED",
                        "A level 2-4 speciality requires a parent");
            }
            if (newParentId.equals(s.getId())) {
                throw new BusinessRuleException("SPECIALITY_PARENT_SELF",
                        "A speciality cannot be its own parent");
            }
            newParent = repository.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("HSpeciality", "parentId", newParentId));
            if (!Objects.equals(newParent.getEducationType(), s.getEducationType())) {
                throw new BusinessRuleException("SPECIALITY_PARENT_TYPE_MISMATCH",
                        "The new parent's education type must match the speciality");
            }
            Integer pl = newParent.getHierarchyLevel();
            if (pl == null || pl + 1 != targetLevel) {
                throw new BusinessRuleException("SPECIALITY_PARENT_LEVEL_MISMATCH",
                        "The new parent must sit exactly one level above the chosen level");
            }
            if (isDescendantOrSelf(newParent, s.getId())) {
                throw new BusinessRuleException("SPECIALITY_PARENT_CYCLE",
                        "A speciality cannot be moved under its own descendant");
            }
        }
        if (targetLevel + subtreeDepth(s.getId()) > LEVELS_MAX) {
            throw new BusinessRuleException("SPECIALITY_MAX_DEPTH",
                    "The move would push this row's subtree past the fixed 4-level taxonomy");
        }
        int oldLevel = s.getHierarchyLevel() == null ? targetLevel : s.getHierarchyLevel();
        s.setParent(newParent);
        s.setHierarchyLevel(targetLevel);
        int delta = targetLevel - oldLevel;
        if (delta != 0) {
            cascadeLevel(s.getId(), delta);
        }
    }

    /** True when {@code ancestorId} is {@code node} itself or one of its ancestors (walks up parent_id). */
    private boolean isDescendantOrSelf(HSpeciality node, UUID ancestorId) {
        HSpeciality cur = node;
        for (int guard = 0; cur != null && guard <= LEVELS_MAX; guard++) {
            if (ancestorId.equals(cur.getId())) {
                return true;
            }
            UUID pid = cur.getParentId();
            if (pid == null) {
                break;
            }
            cur = repository.findById(pid).orElse(null);
        }
        return false;
    }

    /** Depth of the deepest descendant below {@code parentId} (0 for a leaf). */
    private int subtreeDepth(UUID parentId) {
        int max = 0;
        for (HSpeciality child : repository.findByParentId(parentId)) {
            max = Math.max(max, 1 + subtreeDepth(child.getId()));
        }
        return max;
    }

    /** Shift every descendant's display level by {@code delta} (the parent_id links stay intact). */
    private void cascadeLevel(UUID parentId, int delta) {
        for (HSpeciality child : repository.findByParentId(parentId)) {
            Integer cl = child.getHierarchyLevel();
            child.setHierarchyLevel((cl == null ? 0 : cl) + delta);
            repository.save(child);
            cascadeLevel(child.getId(), delta);
        }
    }

    /**
     * Manually add a new speciality ({@code h_speciality}). Born {@code NEEDS_REVIEW}
     * (the entity default is {@code APPROVED}, so it is overridden here) — a hand-entered
     * row is NOT distributed to the 224 OTMs until an admin promotes it to APPROVED via
     * {@link #update}, which is the single channel that drives distribution.
     *
     * <p>Placement: {@code parentId == null} → a top-level (level 1) node; otherwise a child
     * under the resolved parent, with {@code hierarchyLevel = parent.hierarchyLevel + 1}. A
     * child must share its parent's education type (a MASTER row cannot sit under a BACHELOR
     * parent) — a mismatch is a 422 business-rule violation.</p>
     */
    @Transactional
    @CacheEvict(value = "specialityDistribution", allEntries = true)
    public SpecialityNodeDto create(SpecialityCreateDto dto) {
        log.info("Creating speciality: educationType={}, parentId={}", dto.educationType(), dto.parentId());
        HSpeciality s = new HSpeciality();
        s.setCode(blankToNull(dto.code()));
        s.setNameUz(dto.nameUz().trim());
        s.setNameOz(blankToNull(dto.nameOz()));
        s.setNameRu(blankToNull(dto.nameRu()));
        s.setNameEn(blankToNull(dto.nameEn()));
        // name_search is a GENERATED column (V018) — never set on the entity; fold the name locally only
        // to build the twin-lookup key (must match the DB's h_speciality_fold()).
        String nameSearch = foldSearch(s.getNameUz());
        String educationType = validateEducationType(dto.educationType());
        s.setEducationType(educationType);

        // Resolve + validate the parent UP FRONT so an invalid/mismatched/level-4 parentId fails
        // identically whether or not a twin exists (the merge path keeps the existing row's placement,
        // but a bad parentId must never be silently accepted). Placement is applied on the new-row path.
        HSpeciality parent = null;
        Integer parentLevel = null;
        if (dto.parentId() != null) {
            parent = repository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("HSpeciality", "parentId", dto.parentId()));
            if (!Objects.equals(parent.getEducationType(), educationType)) {
                throw new BusinessRuleException("SPECIALITY_PARENT_LEVEL_MISMATCH",
                        "Child education type must match its parent");
            }
            parentLevel = parent.getHierarchyLevel();
            // The classifier is a fixed 4-level taxonomy — a level-4 node cannot have a child.
            // The UI already caps this, but a raw POST must not create an out-of-range level 5.
            if (parentLevel != null && parentLevel >= 4) {
                throw new BusinessRuleException("SPECIALITY_MAX_DEPTH",
                        "The classifier is a fixed 4-level taxonomy; a level-4 node cannot have a child");
            }
        }

        // Year-versioned MERGE (no duplicates): if an active row with the SAME type + code + folded
        // name already exists, do NOT create a second row — attach the requested NEW years to it and
        // return it. Blocks only when nothing new would be added (every requested year already present,
        // or none supplied). This enforces (type, code, name, year) uniqueness while letting one
        // speciality accumulate editions on a single row. The create endpoint is authoritative here.
        List<HSpeciality> twins = repository.findExactTwins(educationType, s.getCode(), nameSearch);
        if (!twins.isEmpty()) {
            Map<UUID, List<Integer>> twinYears = loadYears(twins);
            Set<Integer> existingYears = twinYears.values().stream()
                    .flatMap(List::stream).collect(Collectors.toSet());
            List<Integer> requested = dto.years() == null ? List.of()
                    : dto.years().stream().filter(Objects::nonNull).distinct().toList();
            validateYears(requested);
            List<Integer> toAdd = requested.stream().filter(y -> !existingYears.contains(y)).toList();
            if (toAdd.isEmpty()) {
                throw new ConflictException(
                        "A speciality with the same code and name already exists for the given year(s) (education type %s)"
                                .formatted(eduName(educationType, educationTypeNames())));
            }
            // Merge into the richest twin (most years) — a deterministic single target.
            HSpeciality target = twins.stream()
                    .max(Comparator.comparingInt(t -> twinYears.getOrDefault(t.getId(), List.of()).size()))
                    .orElseGet(() -> twins.get(0));
            boolean wasDistributable = isDistributable(target);
            String priorCode = target.getCode();
            List<Integer> merged = new ArrayList<>(twinYears.getOrDefault(target.getId(), List.of()));
            merged.addAll(toAdd);
            target.setUpdatedBy("system"); // mark dirty → save() bumps @Version + audit (PULL ETag reflects the new year)
            repository.save(target);
            replaceYears(target.getId(), merged);
            distribute(target, wasDistributable, priorCode); // no-op unless the target is distributable
            log.info("Speciality year-merged into existing: targetId={}, addedYears={}", target.getId(), toAdd);
            return getById(target.getId());
        }
        // Hand-entered rows start unverified: NOT distributable until curated to APPROVED.
        s.setReviewStatus(ReviewStatus.NEEDS_REVIEW);
        s.setActive(true);
        s.setIsChecked(false);

        // Placement + derived depth from the pre-validated parent: a root is level 1, a child
        // inherits parent.level + 1.
        if (parent != null) {
            s.setParent(parent);
            s.setHierarchyLevel(parentLevel != null ? parentLevel + 1 : 2);
        } else {
            s.setHierarchyLevel(1);
        }

        validateYears(dto.years());
        repository.save(s);
        if (dto.years() != null) {
            replaceYears(s.getId(), dto.years());
        }
        // No PUSH here: a NEEDS_REVIEW row is never distributable, so the 224 OTMs see it
        // only after it is promoted to APPROVED via update() (which handles the fanout).
        log.info("Speciality created: id={}, hierarchyLevel={}", s.getId(), s.getHierarchyLevel());
        return getById(s.getId());
    }

    /**
     * Modern PUSH of a curated speciality to the 224 OTMs, reusing the already-eligible
     * {@code aggregate_type="classifier"} topic ({@code hemis.classifier.events.v1}) → webhook fanout.
     * The frozen legacy pull and {@code OLD_CLASSIFIER_MAP} are intentionally NOT involved.
     *
     * <ul>
     *   <li><b>UPDATE</b> — the row is distributable (APPROVED + code + active; identical predicate to
     *       {@code findAllForDistribution} so PUSH and PULL never diverge). Wire {@code item} is the same
     *       {@link SpecialityDistItemDto} FLAT v1 shape the pull serves → both channels byte-consistent.</li>
     *   <li><b>DELETE (retraction)</b> — the row was distributable but no longer is (demotion, deactivation,
     *       code-clear); OTMs soft-deactivate it by the retained code. Prevents push-fed OTMs keeping a stale row.</li>
     *   <li><b>nothing</b> — was not distributable and still isn't (e.g. a routine NEEDS_REVIEW curation edit) →
     *       no fanout, so editing the 53 NEEDS_REVIEW rows never spams 224 OTMs.</li>
     * </ul>
     */
    private void distribute(HSpeciality s, boolean wasDistributable, String priorCode) {
        if (isDistributable(s)) {
            SpecialityDistItemDto item = toDistItem(s, loadYears(List.of(s)), educationTypeNames());
            outboxPublisher.publish(
                    "classifier",
                    "speciality:" + s.getId(),
                    "updated",
                    Map.of("classifier_type", "speciality", "action", "UPDATE", "item", item));
            log.info("Speciality distributed (PUSH UPDATE): id={}, code={}", s.getId(), s.getCode());
        } else if (wasDistributable) {
            // Retract by the code the OTMs know the row by (current if intact, else the pre-edit code).
            String retractCode = s.getCode() != null ? s.getCode() : priorCode;
            outboxPublisher.publish(
                    "classifier",
                    "speciality:" + s.getId(),
                    "updated",
                    Map.of("classifier_type", "speciality", "action", "DELETE",
                            "item", Map.of("id", s.getId().toString(), "specialityCode", retractCode == null ? "" : retractCode)));
            log.info("Speciality retracted (PUSH DELETE): id={}, code={}", s.getId(), retractCode);
        }
    }

    /** A row is distributable to OTMs iff APPROVED, code-bearing, and active (the PUSH/PULL predicate). */
    private static boolean isDistributable(HSpeciality s) {
        return s.getReviewStatus() == ReviewStatus.APPROVED
                && s.getCode() != null
                && Boolean.TRUE.equals(s.getActive());
    }

    // =====================================================
    // Helpers
    // =====================================================

    /**
     * Validate + normalize a submitted education-type code with a clean 422 before the DB CHECK/FK
     * fires. This classifier admits only '11'=Bakalavr / '12'=Magistr (mirrors the V018 CHECK); the
     * FK to {@code hemishe_h_education_type} guarantees the code is a real classifier row at flush.
     */
    private String validateEducationType(String educationType) {
        String code = blankToNull(educationType);
        if (code == null || !ALLOWED_EDUCATION_TYPES.contains(code)) {
            throw new BusinessRuleException("SPECIALITY_EDUCATION_TYPE_INVALID",
                    "Education type must be '11' (Bakalavr) or '12' (Magistr)");
        }
        return code;
    }

    /**
     * Reject any submitted year absent from {@code h_education_year} (the FK target),
     * with a clean 422 — otherwise the insert fails at flush with an opaque 400 that
     * rolls the whole create/update back. Null/empty is a no-op (years left unchanged).
     */
    private void validateYears(List<Integer> years) {
        if (years == null || years.isEmpty()) {
            return;
        }
        Set<Integer> valid = new HashSet<>(yearRepository.findValidEducationYears());
        List<Integer> unknown = years.stream()
                .filter(java.util.Objects::nonNull)
                .filter(y -> !valid.contains(y))
                .distinct()
                .sorted()
                .toList();
        if (!unknown.isEmpty()) {
            throw new BusinessRuleException("SPECIALITY_YEAR_NOT_ALLOWED",
                    "Year(s) not in the education-year classifier: " + unknown);
        }
    }

    private void replaceYears(UUID specialityId, List<Integer> years) {
        // Delete-then-insert with an EXPLICIT flush between the two: within a single flush
        // Hibernate's ActionQueue executes ALL inserts before ALL deletes, so without this
        // the re-inserted years would collide with the not-yet-deleted old rows on the
        // uq_h_speciality_year (speciality_id, year) unique constraint whenever the sets overlap.
        yearRepository.deleteBySpecialityId(specialityId);
        yearRepository.flush();
        years.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(year -> {
                    HSpecialityYear row = new HSpecialityYear();
                    row.setSpecialityId(specialityId);
                    row.setYear(year);
                    yearRepository.save(row);
                });
    }

    /**
     * Keep only the specialities belonging to the given edition {@code year}: every node whose own
     * years contain it, plus every ancestor of such a node (so the tree path to the root survives).
     * Operates on the already-loaded set — no extra query.
     */
    /**
     * Retain rows matching the (year AND status AND text) filter, PLUS every ancestor of a match,
     * so the hierarchy stays intact (a NEEDS_REVIEW category above a matching APPROVED leaf survives).
     * Any filter arg may be null. {@code q} matches the folded {@code nameUz} or the raw code substring.
     */
    private List<HSpeciality> retainMatchingBranches(List<HSpeciality> all,
                                                     Map<UUID, List<Integer>> years,
                                                     Integer year, ReviewStatus status, String q) {
        String folded = (q == null || q.isBlank()) ? null : foldSearch(q);
        if (folded != null && folded.isEmpty()) {
            folded = null; // e.g. an all-apostrophe query folds to empty — never match-all via contains("")
        }
        String rawLower = folded == null ? null : q.trim().toLowerCase(Locale.ROOT);
        Map<UUID, HSpeciality> byId = all.stream()
                .collect(Collectors.toMap(HSpeciality::getId, s -> s));
        Set<UUID> keep = new HashSet<>();
        for (HSpeciality s : all) {
            if (!matches(s, years, year, status, folded, rawLower)) {
                continue;
            }
            // Walk this matching node up to its root, stopping at the first already-kept ancestor.
            UUID cur = s.getId();
            while (cur != null && keep.add(cur)) {
                HSpeciality node = byId.get(cur);
                cur = (node != null) ? node.getParentId() : null;
            }
        }
        return all.stream().filter(s -> keep.contains(s.getId())).toList();
    }

    /** True iff the row itself satisfies every supplied (non-null) filter. */
    private static boolean matches(HSpeciality s, Map<UUID, List<Integer>> years, Integer year,
                                   ReviewStatus status, String folded, String rawLower) {
        if (year != null) {
            List<Integer> ys = years.get(s.getId());
            if (ys == null || !ys.contains(year)) {
                return false;
            }
        }
        if (status != null && s.getReviewStatus() != status) {
            return false;
        }
        if (folded != null) {
            String ns = s.getNameSearch();
            boolean nameHit = ns != null && ns.contains(folded);
            boolean codeHit = s.getCode() != null
                    && s.getCode().toLowerCase(Locale.ROOT).contains(rawLower);
            if (!nameHit && !codeHit) {
                return false;
            }
        }
        return true;
    }

    /** Batch-load year lists keyed by speciality id (single query, no N+1). */
    private Map<UUID, List<Integer>> loadYears(List<HSpeciality> specialities) {
        if (specialities.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = specialities.stream().map(HSpeciality::getId).toList();
        Map<UUID, List<Integer>> byId = new LinkedHashMap<>();
        for (HSpecialityYear y : yearRepository.findBySpecialityIds(ids)) {
            byId.computeIfAbsent(y.getSpecialityId(), k -> new ArrayList<>()).add(y.getYear());
        }
        return byId;
    }

    /**
     * Education-type options for the classifier's own Create/Edit pickers (Bakalavr / Magistr),
     * read from the {@code hemishe_h_education_type} classifier — NOT hard-coded. Scoped to the two
     * types this classifier admits ({@link #ALLOWED_EDUCATION_TYPES}), sorted by sortOrder then code,
     * multilingual (name / nameRu / nameEn). Mirrors the attachments dictionary but is served under
     * {@code classifiers.speciality.view}, so the classifier page needs no cross-feature permission.
     */
    public List<ClassifierOptionDto> listEducationTypes() {
        return educationTypeRepository.findByIsActiveTrueOrderBySortOrderAscCodeAsc().stream()
                .filter(t -> t.getCode() != null && ALLOWED_EDUCATION_TYPES.contains(t.getCode()))
                .map(t -> new ClassifierOptionDto(t.getCode(), t.getName(), t.getNameRu(), t.getNameEn()))
                .toList();
    }

    /**
     * code → localized display name from the {@code hemishe_h_education_type} classifier (5 static rows),
     * loaded once per read operation and threaded into the DTO mappers (no per-row query → no N+1).
     * A code with no live classifier row falls back to the code itself.
     */
    private Map<String, String> educationTypeNames() {
        return educationTypeRepository.findAll().stream()
                .filter(e -> e.getCode() != null)
                .collect(Collectors.toMap(HEducationType::getCode,
                        e -> e.getName() != null ? e.getName() : e.getCode(),
                        (a, b) -> a));
    }

    /** Null-safe education-type name resolution from the pre-loaded map (falls back to the raw code). */
    private static String eduName(String code, Map<String, String> names) {
        return code == null ? null : names.getOrDefault(code, code);
    }

    /** Newest year of a node (no years → sorts last), from the batch-loaded map. */
    private static int newestYear(HSpeciality s, Map<UUID, List<Integer>> years) {
        List<Integer> ys = years.get(s.getId());
        return (ys == null || ys.isEmpty())
                ? Integer.MIN_VALUE
                : ys.stream().mapToInt(Integer::intValue).max().getAsInt();
    }

    /** Numeric value of the code (null / non-numeric → sorts last). */
    private static long codeValue(HSpeciality s) {
        String c = s.getCode();
        if (c == null || c.isBlank()) return Long.MAX_VALUE;
        try {
            return Long.parseLong(c.trim());
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Canonical display order for the tree: newest edition year first (2021 above 2020),
     * then ascending numeric code. Replaces the former string {@code BY_CODE}, which
     * lexicographically interleaved the two code generations (0100000 / 100000 / 1000000).
     */
    private static Comparator<HSpeciality> displayOrder(Map<UUID, List<Integer>> years) {
        return Comparator.comparingInt((HSpeciality s) -> newestYear(s, years)).reversed()
                .thenComparingLong(HSpecialityService::codeValue)
                .thenComparing(HSpeciality::getNameUz, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private SpecialityNodeDto toNode(HSpeciality s, Map<UUID, List<HSpeciality>> byParent,
                                     Map<UUID, List<Integer>> years, Map<String, String> eduNames) {
        List<SpecialityNodeDto> children = byParent.getOrDefault(s.getId(), List.of()).stream()
                .sorted(displayOrder(years))
                .map(c -> toNode(c, byParent, years, eduNames))
                .toList();
        return toNodeWith(s, years, children, eduNames);
    }

    private SpecialityNodeDto toLeaf(HSpeciality s, Map<UUID, List<Integer>> years, Map<String, String> eduNames) {
        return toNodeWith(s, years, List.of(), eduNames);
    }

    private SpecialityNodeDto toNodeWith(HSpeciality s, Map<UUID, List<Integer>> years,
                                         List<SpecialityNodeDto> children, Map<String, String> eduNames) {
        return new SpecialityNodeDto(
                s.getId().toString(),
                s.getCode(),
                s.getNameUz(),
                s.getNameOz(),
                s.getNameRu(),
                s.getNameEn(),
                s.getEducationType(),
                eduName(s.getEducationType(), eduNames),
                s.getReviewStatus() != null ? s.getReviewStatus().getValue() : null,
                s.getParentId() != null ? s.getParentId().toString() : null,
                s.getHierarchyLevel(),
                s.getActive(),
                s.getIsChecked(),
                s.getVersion(),
                years.getOrDefault(s.getId(), List.of()),
                children
        );
    }

    private SpecialityDistItemDto toDistItem(HSpeciality s, Map<UUID, List<Integer>> years,
                                             Map<String, String> eduNames) {
        return new SpecialityDistItemDto(
                s.getId().toString(),
                s.getCode(),
                s.getNameUz(),
                s.getNameOz(),
                s.getNameRu(),
                s.getNameEn(),
                s.getEducationType(),
                eduName(s.getEducationType(), eduNames),
                s.getParentId() != null ? s.getParentId().toString() : null,
                s.getHierarchyLevel(),
                years.getOrDefault(s.getId(), List.of()),
                s.getActive(),
                s.getIsChecked(),
                s.getVersion()
        );
    }

    private SpecialityRowDto toRow(HSpeciality s, Map<UUID, List<Integer>> years, Map<String, String> eduNames) {
        return new SpecialityRowDto(
                s.getId().toString(),
                s.getCode(),
                s.getNameUz(),
                s.getNameOz(),
                s.getNameRu(),
                s.getNameEn(),
                s.getEducationType(),
                eduName(s.getEducationType(), eduNames),
                s.getReviewStatus() != null ? s.getReviewStatus().getValue() : null,
                s.getParentId() != null ? s.getParentId().toString() : null,
                s.getHierarchyLevel(),
                s.getActive(),
                s.getVersion(),
                years.getOrDefault(s.getId(), List.of())
        );
    }

    /**
     * Query-side fold for the {@code name_search} lookup key. MUST stay byte-identical to the SQL
     * {@code h_speciality_fold()} (V018) that GENERATES the stored column and to the ETL {@code fold()}:
     * apostrophe variants → space, lowercase, collapse whitespace. No NFKD unaccent — the DB fold that
     * backs the generated column cannot call a non-IMMUTABLE unaccent, so this must not either, or a
     * folded query key would stop matching the stored value.
     */
    private static String foldSearch(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            sb.append(APOSTROPHES.indexOf(c) >= 0 ? ' ' : c);
        }
        return sb.toString().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** True if the current authenticated user holds {@code authority} (for the promote guard). */
    private static boolean currentUserHasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }
}
