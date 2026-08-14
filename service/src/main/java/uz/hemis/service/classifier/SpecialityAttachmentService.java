package uz.hemis.service.classifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.common.exception.ConflictException;
import uz.hemis.common.exception.ResourceNotFoundException;
import uz.hemis.domain.entity.academic.EducationForm;
import uz.hemis.domain.entity.academic.EducationType;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.UniversitySpecialityAttachment;
import uz.hemis.domain.entity.university.University;
import uz.hemis.domain.repository.EducationFormRepository;
import uz.hemis.domain.repository.EducationTypeRepository;
import uz.hemis.domain.repository.UniversityRepository;
import uz.hemis.domain.repository.UniversitySpecialityAttachmentRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.service.classifier.dto.SpecialityAttachmentCreateDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentFilterOptionsDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentFilterOptionsDto.Option;
import uz.hemis.service.classifier.dto.SpecialityAttachmentRowDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentSnapshotDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentSnapshotFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Attach unified-classifier specialities to OTMs ({@code university_speciality_attachment}).
 *
 * <p>"Which speciality is this university allowed to run." Unlike the classifier itself
 * (global reference data), an attachment is <strong>tenant-owned</strong> and therefore
 * enforces the second authorization dimension — {@link AccessScope} — <em>fail-closed</em>
 * in this layer, closing the cross-OTM IDOR:</p>
 * <ul>
 *   <li><b>list</b> — a restricted (OTM) caller sees only its own OTM's rows; a ministry
 *       caller sees all (optionally filtered by {@code universityCode}); a deny-all caller 403s.</li>
 *   <li><b>getById / delete</b> — the loaded row's {@code universityCode} must be in scope (403 otherwise).</li>
 *   <li><b>create</b> — the requested {@code universityCode} must be allowed by the caller's scope
 *       (an OTM caller may only attach to its own OTM; a ministry caller to any).</li>
 * </ul>
 *
 * <p>The scope is always derived server-side from the authenticated subject via
 * {@link ScopeResolver}, never from a request parameter (a request {@code universityCode}
 * is only ever <em>validated against</em> the scope). Not cached — every read is a live,
 * scope-filtered query, so no scoped result can leak across OTMs through a shared cache.</p>
 *
 * @since 2.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SpecialityAttachmentService {

    /** Sort-property whitelist — blocks column-abuse / 500 on a bad {@code sort} param. */
    private static final Set<String> ALLOWED_SORTS = Set.of(
            "universityCode", "specialityId", "status", "educationForm", "eduYear", "createdAt", "updatedAt");
    private static final Sort DEFAULT_SORT = Sort.by("universityCode").ascending();
    /** Default assignment year for a manually-created attachment when the caller omits it. */
    private static final int DEFAULT_EDU_YEAR = 2026;

    private final UniversitySpecialityAttachmentRepository repository;
    private final HSpecialityRepository specialityRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final EducationFormRepository educationFormRepository;
    private final UniversityRepository universityRepository;
    private final ScopeResolver scopeResolver;

    // =====================================================
    // READ (REPLICA) — scope-filtered
    // =====================================================

    /**
     * Paginated, tenant-scoped list of attachments with resolved speciality names.
     * The {@code universityCode} request param is only ever validated against — never
     * used to widen — the caller's server-derived scope.
     */
    public Page<SpecialityAttachmentRowDto> list(String universityCode, UUID specialityId, String status,
                                                 String educationType, String educationForm, Integer eduYear,
                                                 Pageable pageable) {
        log.debug("Listing speciality attachments: universityCode={}, specialityId={}, status={}, educationType={}, educationForm={}, eduYear={}",
                universityCode, specialityId, status, educationType, educationForm, eduYear);
        AccessScope scope = scopeResolver.currentScope();
        if (scope.isDenyAll()) {
            throw new AccessDeniedException("No university data scope for the current principal");
        }
        Pageable safePageable = sanitize(pageable);
        String eduType = blankToNull(educationType);
        String eduForm = blankToNull(educationForm);

        Page<UniversitySpecialityAttachment> page;
        if (scope.unrestricted()) {
            if (hasText(universityCode)) {
                page = repository.searchScoped(List.of(universityCode.trim()), specialityId, status, eduForm, eduType, eduYear, safePageable);
            } else {
                page = repository.searchAll(specialityId, status, eduForm, eduType, eduYear, safePageable);
            }
        } else {
            page = repository.searchScoped(scopedCodes(scope, universityCode), specialityId, status, eduForm, eduType, eduYear, safePageable);
        }
        return withResolvedNames(page, safePageable);
    }

    /**
     * Filter-dropdown options — only the OTMs / education types / forms that ACTUALLY occur in the
     * caller's in-scope attachments (never the full classifier), so a dropdown never offers a choice
     * that returns zero rows. A restricted (OTM) caller sees only its own scope; a ministry caller
     * sees every distinct value present.
     */
    public SpecialityAttachmentFilterOptionsDto filterOptions() {
        AccessScope scope = scopeResolver.currentScope();
        if (scope.isDenyAll()) {
            throw new AccessDeniedException("No university data scope for the current principal");
        }
        List<String> uniCodes = repository.findDistinctUniversityCodes();
        if (!scope.unrestricted()) {
            uniCodes = uniCodes.stream().filter(scope::allows).toList();
        }
        Map<String, String> uniNames = universityNames(uniCodes);
        // Only OTMs that resolve to a real registry row — drop orphan codes whose university is
        // absent/soft-deleted in hemishe_e_university (so the filter never offers an unnamed "562").
        // Ordered by numeric OTM code, not by name.
        List<Option> universities = uniCodes.stream()
                .filter(uniNames::containsKey)
                .map(c -> new Option(c, uniNames.get(c)))
                .sorted(Comparator.comparingLong((Option o) -> codeOrder(o.code())).thenComparing(Option::code))
                .toList();

        Map<String, String> eduNames = educationTypeNames();
        List<Option> educationTypes = repository.findDistinctEducationTypes().stream()
                .map(c -> new Option(c, eduNames.getOrDefault(c, c)))
                .sorted(Comparator.comparing(Option::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<String, String> formNames = educationFormNames();
        List<Option> educationForms = repository.findDistinctEducationForms().stream()
                .map(c -> new Option(c, formNames.getOrDefault(c, c)))
                .sorted(Comparator.comparing(Option::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        // Academic years present in attachments — value is the start year ("2026"), label is the
        // span ("2026-2027"). Newest first (repo ORDER BY eduYear DESC); grows as future years seed.
        List<Option> years = repository.findDistinctEduYears().stream()
                .filter(java.util.Objects::nonNull)
                .map(y -> new Option(String.valueOf(y), y + "-" + (y + 1)))
                .toList();

        return new SpecialityAttachmentFilterOptionsDto(universities, educationTypes, educationForms, years);
    }

    /** Single attachment with resolved speciality name — 403 if outside the caller's scope, 404 if missing. */
    public SpecialityAttachmentRowDto getById(UUID id) {
        UniversitySpecialityAttachment a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpecialityAttachment", "id", id));
        assertInScope(a.getUniversityCode());
        HSpeciality s = specialityRepository.findById(a.getSpecialityId()).orElse(null);
        return toRow(a, s, educationTypeNames(),
                universityNames(List.of(a.getUniversityCode())), educationFormNames(),
                parentsById(s != null ? List.of(s) : List.of()));
    }

    // =====================================================
    // OTM-FACING DISTRIBUTION (bootstrap PULL) — live from the read replica
    // =====================================================

    /**
     * Full snapshot of one OTM's live attachments — the bootstrap PULL an OTM (Univer)
     * reads to learn which specialities it may run and in which education forms.
     *
     * <p>Unlike {@link #list}, this is NOT scope-filtered here: the caller is a single
     * OTM whose own {@code universityCode} is derived server-side from its JWT
     * {@code university_code} claim (never a request parameter) and passed in directly —
     * so the query is inherently single-tenant.</p>
     *
     * <p><strong>Deliberately NOT application-cached.</strong> This is a per-tenant, index-backed
     * lookup ({@code idx_univ_spec_attach_univ}, ~150 rows) plus a batched PK resolve — a few
     * milliseconds off the read replica. A per-OTM Redis entry would be <em>slower</em> than the
     * indexed read (serialize/deserialize + network &gt; index scan — the project's documented
     * cache anti-pattern) and would reintroduce the cache-invalidation/cross-pod-staleness problem
     * for data an admin edits interactively. So it is served live from the replica: always fresh
     * (bounded only by sub-second replication lag), no eviction to get wrong. If load ever proves
     * this read hot, add caching back with event-driven cross-pod invalidation (CacheVersionService
     * pub/sub), not a TTL-only cache. The global classifier snapshot ({@code specialityDistribution})
     * stays cached — that one is a large, shared, all-tenant result, a different shape.</p>
     */
    public List<SpecialityAttachmentSnapshotDto> getSnapshot(String universityCode,
                                                             SpecialityAttachmentSnapshotFilter filter) {
        String code = universityCode == null ? "" : universityCode.trim();
        if (code.isEmpty()) {
            return List.of();
        }
        List<UniversitySpecialityAttachment> rows = repository.findByUniversityCode(code);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<UUID> specialityIds = rows.stream()
                .map(UniversitySpecialityAttachment::getSpecialityId)
                .distinct()
                .toList();
        Map<UUID, HSpeciality> specById = specialityRepository.findAllById(specialityIds).stream()
                .collect(Collectors.toMap(HSpeciality::getId, Function.identity()));
        Map<String, String> eduNames = educationTypeNames();
        // Optional column filters (eduYear / educationType / educationForm / status / specialityCode).
        // They only narrow THIS OTM's own set; the tenant is the JWT claim, never a filter, so no
        // parameter can widen scope to another OTM. Applied in-memory over the tiny per-tenant read.
        SpecialityAttachmentSnapshotFilter criteria =
                filter != null ? filter : SpecialityAttachmentSnapshotFilter.none();
        List<SpecialityAttachmentSnapshotDto> snapshot = rows.stream()
                .map(a -> toSnapshot(a, specById.get(a.getSpecialityId()), eduNames))
                .filter(criteria::matches)
                .toList();
        log.debug("Speciality-attachment snapshot for OTM {}: {} rows (filtered={})",
                code, snapshot.size(), !criteria.isEmpty());
        return snapshot;
    }

    // =====================================================
    // WRITE (MASTER) — scope-guarded
    // =====================================================

    /**
     * Attach a speciality to an OTM. The requested {@code universityCode} must be in the caller's
     * scope (403 otherwise); a duplicate live attachment for (OTM, speciality, form) is a 409.
     */
    @Transactional
    public SpecialityAttachmentRowDto create(SpecialityAttachmentCreateDto dto) {
        String code = dto.universityCode().trim();
        log.info("Attaching speciality {} to OTM {}", dto.specialityId(), code);
        assertCanWrite(code);

        HSpeciality speciality = specialityRepository.findById(dto.specialityId())
                .orElseThrow(() -> new ResourceNotFoundException("HSpeciality", "id", dto.specialityId()));

        String educationForm = blankToNull(dto.educationForm());
        int eduYear = dto.eduYear() != null ? dto.eduYear() : DEFAULT_EDU_YEAR;
        if (repository.existsDuplicate(code, dto.specialityId(), educationForm, eduYear, null)) {
            throw new ConflictException("Speciality is already attached to this university for the given education form and year");
        }

        UniversitySpecialityAttachment entity = new UniversitySpecialityAttachment();
        entity.setUniversityCode(code);
        entity.setSpecialityId(dto.specialityId());
        entity.setEducationForm(educationForm);
        entity.setEduYear(eduYear);
        entity.setStatus("ACTIVE");

        UniversitySpecialityAttachment saved = repository.save(entity);
        log.info("Speciality attachment created: id={}", saved.getId());
        return toRow(saved, speciality, educationTypeNames(),
                universityNames(List.of(saved.getUniversityCode())), educationFormNames(),
                parentsById(List.of(speciality)));
    }

    /** Detach (soft delete) — 403 if the row is outside the caller's scope, 404 if missing. */
    @Transactional
    public void delete(UUID id) {
        UniversitySpecialityAttachment a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpecialityAttachment", "id", id));
        assertInScope(a.getUniversityCode());
        a.softDelete();
        repository.save(a);
        log.info("Speciality attachment detached (soft): id={}", id);
    }

    // =====================================================
    // Scope guard (fail-closed)
    // =====================================================

    /** Effective allowed codes for a restricted caller: a validated request code, or its full scope set. */
    private Collection<String> scopedCodes(AccessScope scope, String requested) {
        if (hasText(requested)) {
            String code = requested.trim();
            if (!scope.allows(code)) {
                throw new AccessDeniedException("University out of scope: " + code);
            }
            return List.of(code);
        }
        return scope.universityCodes(); // non-empty — a restricted scope with no codes is deny-all (rejected upstream)
    }

    /** Read/mutate a single row only if its OTM is in the caller's scope. */
    private void assertInScope(String universityCode) {
        if (!scopeResolver.currentScope().allows(universityCode)) {
            throw new AccessDeniedException("Attachment is outside your university scope");
        }
    }

    /** Create guard: deny-all → 403; otherwise the target OTM must be allowed by the scope. */
    private void assertCanWrite(String universityCode) {
        AccessScope scope = scopeResolver.currentScope();
        if (scope.isDenyAll()) {
            throw new AccessDeniedException("No university data scope for the current principal");
        }
        if (!scope.allows(universityCode)) {
            throw new AccessDeniedException("University out of scope: " + universityCode);
        }
    }

    // =====================================================
    // Name resolution (batch — no N+1)
    // =====================================================

    private Page<SpecialityAttachmentRowDto> withResolvedNames(Page<UniversitySpecialityAttachment> page, Pageable pageable) {
        List<UniversitySpecialityAttachment> content = page.getContent();
        if (content.isEmpty()) {
            // Preserve the real total on an empty (e.g. past-the-end) slice — Page.empty() hard-codes total=0.
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }
        List<UUID> specialityIds = content.stream()
                .map(UniversitySpecialityAttachment::getSpecialityId)
                .distinct()
                .toList();
        Map<UUID, HSpeciality> specById = specialityRepository.findAllById(specialityIds).stream()
                .collect(Collectors.toMap(HSpeciality::getId, Function.identity()));
        Map<String, String> eduNames = educationTypeNames();
        Map<String, String> formNames = educationFormNames();
        List<String> uniCodes = content.stream()
                .map(UniversitySpecialityAttachment::getUniversityCode)
                .distinct()
                .toList();
        Map<String, String> uniNames = universityNames(uniCodes);
        Map<UUID, HSpeciality> parentsById = parentsById(specById.values());

        List<SpecialityAttachmentRowDto> rows = content.stream()
                .map(a -> toRow(a, specById.get(a.getSpecialityId()), eduNames, uniNames, formNames, parentsById))
                .toList();
        return new PageImpl<>(rows, pageable, page.getTotalElements());
    }

    /** code → name from the {@code hemishe_h_education_type} classifier (5 static rows) — no per-row query. */
    private Map<String, String> educationTypeNames() {
        return educationTypeRepository.findAll().stream()
                .filter(e -> e.getCode() != null)
                .collect(Collectors.toMap(EducationType::getCode,
                        e -> e.getName() != null ? e.getName() : e.getCode(), (x, y) -> x));
    }

    /** code → name from the {@code hemishe_h_education_form} classifier (a few static rows). */
    private Map<String, String> educationFormNames() {
        return educationFormRepository.findAll().stream()
                .filter(e -> e.getCode() != null)
                .collect(Collectors.toMap(EducationForm::getCode,
                        e -> e.getName() != null ? e.getName() : e.getCode(), (x, y) -> x));
    }

    /** code → name for the given university codes (batch by PK — no N+1). */
    private Map<String, String> universityNames(Collection<String> codes) {
        List<String> distinct = codes.stream().filter(SpecialityAttachmentService::hasText).distinct().toList();
        if (distinct.isEmpty()) {
            return Map.of();
        }
        return universityRepository.findAllById(distinct).stream()
                .filter(u -> u.getCode() != null)
                .collect(Collectors.toMap(University::getCode,
                        u -> u.getName() != null ? u.getName() : u.getCode(), (x, y) -> x));
    }

    private SpecialityAttachmentRowDto toRow(UniversitySpecialityAttachment a, HSpeciality speciality,
                                             Map<String, String> eduNames, Map<String, String> uniNames,
                                             Map<String, String> formNames, Map<UUID, HSpeciality> parentsById) {
        String eduType = speciality != null ? speciality.getEducationType() : null;
        String form = a.getEducationForm();
        Integer level = speciality != null ? speciality.getHierarchyLevel() : null;
        UUID parentId = speciality != null ? speciality.getParentId() : null;
        // Parent line is shown ONLY for an L4 "Ichki yo'nalish" — its parent is the L3 "Yo'nalish".
        // An L3 "Yo'nalish" row shows NO parent (its own parent is the L2 "Ta'lim sohasi", which this
        // registry does not surface). This registry only works with L3/L4.
        boolean showParent = level != null && level == 4 && parentId != null;
        HSpeciality parent = showParent ? parentsById.get(parentId) : null;
        String parentName = parent == null ? null
                : (parent.getNameUz() != null ? parent.getNameUz() : parent.getCode());
        String parentCode = parent == null ? null : parent.getCode();
        return new SpecialityAttachmentRowDto(
                a.getId().toString(),
                a.getUniversityCode(),
                uniNames.get(a.getUniversityCode()),
                a.getSpecialityId().toString(),
                speciality != null ? speciality.getCode() : null,
                speciality != null ? speciality.getNameUz() : null,
                level,
                parentName,
                parentCode,
                eduType,
                eduType == null ? null : eduNames.getOrDefault(eduType, eduType),
                form,
                form == null ? null : formNames.getOrDefault(form, form),
                a.getEduYear(),
                a.getStatus()
        );
    }

    /** id → parent speciality entity for the given rows (batch — no N+1); yields parent name + code. */
    private Map<UUID, HSpeciality> parentsById(Collection<HSpeciality> specialities) {
        List<UUID> parentIds = specialities.stream()
                .filter(s -> s != null && s.getParentId() != null)
                .map(HSpeciality::getParentId)
                .distinct()
                .toList();
        if (parentIds.isEmpty()) {
            return Map.of();
        }
        return specialityRepository.findAllById(parentIds).stream()
                .collect(Collectors.toMap(HSpeciality::getId, s -> s, (x, y) -> x));
    }

    private SpecialityAttachmentSnapshotDto toSnapshot(UniversitySpecialityAttachment a, HSpeciality speciality,
                                                       Map<String, String> eduNames) {
        String eduType = speciality != null ? speciality.getEducationType() : null;
        return new SpecialityAttachmentSnapshotDto(
                a.getSpecialityId().toString(),
                speciality != null ? speciality.getCode() : null,
                speciality != null ? speciality.getNameUz() : null,
                eduType,
                eduType == null ? null : eduNames.getOrDefault(eduType, eduType),
                a.getEducationForm(),
                a.getEduYear(),
                a.getStatus()
        );
    }

    /** Rebuild the pageable keeping only whitelisted sort properties (falls back to {@link #DEFAULT_SORT}). */
    private static Pageable sanitize(Pageable pageable) {
        List<Sort.Order> safe = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (ALLOWED_SORTS.contains(order.getProperty())) {
                safe.add(order);
            }
        }
        Sort sort = safe.isEmpty() ? DEFAULT_SORT : Sort.by(safe);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** Numeric ordering key for an OTM code ("396" → 396); non-numeric codes sort last. */
    private static long codeOrder(String code) {
        try {
            return Long.parseLong(code.trim());
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }
}
