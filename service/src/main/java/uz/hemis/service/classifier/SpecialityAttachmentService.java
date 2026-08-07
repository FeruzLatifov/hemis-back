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
import uz.hemis.domain.entity.academic.EducationType;
import uz.hemis.domain.entity.classifier.HSpeciality;
import uz.hemis.domain.entity.classifier.HSpecialityAttachment;
import uz.hemis.domain.repository.EducationTypeRepository;
import uz.hemis.domain.repository.HSpecialityAttachmentRepository;
import uz.hemis.domain.repository.HSpecialityRepository;
import uz.hemis.service.classifier.dto.SpecialityAttachmentCreateDto;
import uz.hemis.service.classifier.dto.SpecialityAttachmentRowDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Attach unified-classifier specialities to OTMs ({@code h_speciality_attachment}).
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
            "universityCode", "specialityId", "status", "educationForm", "createdAt", "updatedAt");
    private static final Sort DEFAULT_SORT = Sort.by("universityCode").ascending();

    private final HSpecialityAttachmentRepository repository;
    private final HSpecialityRepository specialityRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final ScopeResolver scopeResolver;

    // =====================================================
    // READ (REPLICA) — scope-filtered
    // =====================================================

    /**
     * Paginated, tenant-scoped list of attachments with resolved speciality names.
     * The {@code universityCode} request param is only ever validated against — never
     * used to widen — the caller's server-derived scope.
     */
    public Page<SpecialityAttachmentRowDto> list(String universityCode, UUID specialityId,
                                                 String status, Pageable pageable) {
        log.debug("Listing speciality attachments: universityCode={}, specialityId={}, status={}",
                universityCode, specialityId, status);
        AccessScope scope = scopeResolver.currentScope();
        if (scope.isDenyAll()) {
            throw new AccessDeniedException("No university data scope for the current principal");
        }
        Pageable safePageable = sanitize(pageable);

        Page<HSpecialityAttachment> page;
        if (scope.unrestricted()) {
            if (hasText(universityCode)) {
                page = repository.searchScoped(List.of(universityCode.trim()), specialityId, status, safePageable);
            } else {
                page = repository.searchAll(specialityId, status, safePageable);
            }
        } else {
            page = repository.searchScoped(scopedCodes(scope, universityCode), specialityId, status, safePageable);
        }
        return withResolvedNames(page, safePageable);
    }

    /** Single attachment with resolved speciality name — 403 if outside the caller's scope, 404 if missing. */
    public SpecialityAttachmentRowDto getById(UUID id) {
        HSpecialityAttachment a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SpecialityAttachment", "id", id));
        assertInScope(a.getUniversityCode());
        HSpeciality s = specialityRepository.findById(a.getSpecialityId()).orElse(null);
        return toRow(a, s, educationTypeNames());
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
        if (repository.existsDuplicate(code, dto.specialityId(), educationForm, null)) {
            throw new ConflictException("Speciality is already attached to this university for the given education form");
        }

        HSpecialityAttachment entity = new HSpecialityAttachment();
        entity.setUniversityCode(code);
        entity.setSpecialityId(dto.specialityId());
        entity.setEducationForm(educationForm);
        entity.setStatus("ACTIVE");

        HSpecialityAttachment saved = repository.save(entity);
        log.info("Speciality attachment created: id={}", saved.getId());
        return toRow(saved, speciality, educationTypeNames());
    }

    /** Detach (soft delete) — 403 if the row is outside the caller's scope, 404 if missing. */
    @Transactional
    public void delete(UUID id) {
        HSpecialityAttachment a = repository.findById(id)
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

    private Page<SpecialityAttachmentRowDto> withResolvedNames(Page<HSpecialityAttachment> page, Pageable pageable) {
        List<HSpecialityAttachment> content = page.getContent();
        if (content.isEmpty()) {
            // Preserve the real total on an empty (e.g. past-the-end) slice — Page.empty() hard-codes total=0.
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }
        List<UUID> specialityIds = content.stream()
                .map(HSpecialityAttachment::getSpecialityId)
                .distinct()
                .toList();
        Map<UUID, HSpeciality> specById = specialityRepository.findAllById(specialityIds).stream()
                .collect(Collectors.toMap(HSpeciality::getId, Function.identity()));
        Map<String, String> eduNames = educationTypeNames();

        List<SpecialityAttachmentRowDto> rows = content.stream()
                .map(a -> toRow(a, specById.get(a.getSpecialityId()), eduNames))
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

    private SpecialityAttachmentRowDto toRow(HSpecialityAttachment a, HSpeciality speciality,
                                             Map<String, String> eduNames) {
        String eduType = speciality != null ? speciality.getEducationType() : null;
        return new SpecialityAttachmentRowDto(
                a.getId().toString(),
                a.getUniversityCode(),
                a.getSpecialityId().toString(),
                speciality != null ? speciality.getCode() : null,
                speciality != null ? speciality.getNameUz() : null,
                eduType,
                eduType == null ? null : eduNames.getOrDefault(eduType, eduType),
                a.getEducationForm(),
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
}
