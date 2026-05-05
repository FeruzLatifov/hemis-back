package uz.hemis.service.university;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hemis.common.audit.AuditAction;
import uz.hemis.common.audit.Audited;
import uz.hemis.domain.entity.university.UniversityProfile;
import uz.hemis.domain.repository.UniversityProfileRepository;
import uz.hemis.service.security.TenantGuard;
import uz.hemis.service.university.dto.DocumentMetaDto;
import uz.hemis.service.university.dto.SocialLinksDto;
import uz.hemis.service.university.dto.UniversityProfileDto;
import uz.hemis.service.university.dto.UniversityProfileRequest;

import java.util.Collections;
import java.util.List;

/**
 * Service for university public profile — contacts, social media, documents.
 *
 * <p>Translates between the flat entity (with two JSONB string columns) and a strongly typed
 * DTO. Parsing/serialization errors are logged but never propagate — a malformed JSONB column
 * yields an empty social/documents block rather than a 500.</p>
 *
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UniversityProfileService {

    private final UniversityProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    private final TenantGuard tenantGuard;

    private static final TypeReference<List<DocumentMetaDto>> DOCS_TYPE = new TypeReference<>() {};

    @Transactional(readOnly = true)
    public UniversityProfileDto getProfile(String universityCode) {
        return profileRepository.findByUniversityCode(universityCode)
                .map(this::toDto)
                .orElseGet(() -> UniversityProfileDto.builder()
                        .universityCode(universityCode)
                        .socialLinks(null)
                        .documents(Collections.emptyList())
                        .build());
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, entity = "UniversityProfile", keyArg = "universityCode")
    public UniversityProfileDto upsert(String universityCode, UniversityProfileRequest request) {
        tenantGuard.verifyOwnershipOrAdmin(universityCode);
        UniversityProfile entity = profileRepository.findByUniversityCode(universityCode)
                .orElseGet(() -> UniversityProfile.builder()
                        .universityCode(universityCode)
                        .source("manual")
                        .build());

        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setDescription(request.getDescription());
        entity.setLogoKey(request.getLogoKey());
        entity.setSocialLinks(serialize(request.getSocialLinks()));
        entity.setDocuments(serialize(request.getDocuments()));
        entity.setMapUrl(request.getMapUrl());
        entity.setLatitude(request.getLatitude());
        entity.setLongitude(request.getLongitude());

        UniversityProfile saved = profileRepository.save(entity);
        log.info("University profile upserted: code={}", universityCode);
        return toDto(saved);
    }

    // =====================================================
    // Mapping
    // =====================================================

    private UniversityProfileDto toDto(UniversityProfile e) {
        return UniversityProfileDto.builder()
                .universityCode(e.getUniversityCode())
                .phone(e.getPhone())
                .email(e.getEmail())
                .description(e.getDescription())
                .logoKey(e.getLogoKey())
                .socialLinks(parseSocial(e.getSocialLinks()))
                .documents(parseDocs(e.getDocuments()))
                .mapUrl(e.getMapUrl())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .build();
    }

    private SocialLinksDto parseSocial(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, SocialLinksDto.class);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse social_links JSONB: {}", ex.getMessage());
            return null;
        }
    }

    private List<DocumentMetaDto> parseDocs(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, DOCS_TYPE);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to parse documents JSONB: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String serialize(Object value) {
        if (value == null) return null;
        if (value instanceof List<?> list && list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize profile JSONB value", ex);
            return null;
        }
    }
}
