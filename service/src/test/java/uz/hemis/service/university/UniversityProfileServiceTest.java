package uz.hemis.service.university;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.hemis.domain.entity.university.UniversityProfile;
import uz.hemis.domain.repository.UniversityProfileRepository;
import uz.hemis.common.auth.AccessScope;
import uz.hemis.common.auth.ScopeResolver;
import uz.hemis.service.university.dto.DocumentMetaDto;
import uz.hemis.service.university.dto.SocialLinksDto;
import uz.hemis.service.university.dto.UniversityProfileDto;
import uz.hemis.service.university.dto.UniversityProfileRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniversityProfileService — JSONB parse + upsert + cache")
class UniversityProfileServiceTest {

    @Mock private UniversityProfileRepository repository;
    @Mock private ScopeResolver scopeResolver;

    private UniversityProfileService service;
    private ObjectMapper realObjectMapper;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();
        service = new UniversityProfileService(repository, realObjectMapper, scopeResolver);
    }

    @Test
    @DisplayName("getProfile — topilmasa default DTO (empty documents)")
    void getProfile_notFound_returnsDefault() {
        when(repository.findByUniversityCode("337")).thenReturn(Optional.empty());

        UniversityProfileDto dto = service.getProfile("337");

        assertThat(dto.getUniversityCode()).isEqualTo("337");
        assertThat(dto.getSocialLinks()).isNull();
        assertThat(dto.getDocuments()).isEmpty();
    }

    @Test
    @DisplayName("getProfile — entity fields DTO'ga ko'chiriladi")
    void getProfile_found_mapped() {
        UniversityProfile entity = UniversityProfile.builder()
                .universityCode("337")
                .phone("+998901234567")
                .email("info@adu.uz")
                .description("Andijon Davlat Universiteti")
                .logoKey("logos/337.png")
                .mapUrl("https://maps.example.com/337")
                .latitude(new java.math.BigDecimal("40.7821"))
                .longitude(new java.math.BigDecimal("72.3442"))
                .build();
        when(repository.findByUniversityCode("337")).thenReturn(Optional.of(entity));

        UniversityProfileDto dto = service.getProfile("337");

        assertThat(dto.getPhone()).isEqualTo("+998901234567");
        assertThat(dto.getEmail()).isEqualTo("info@adu.uz");
        assertThat(dto.getDescription()).isEqualTo("Andijon Davlat Universiteti");
        assertThat(dto.getLogoKey()).isEqualTo("logos/337.png");
    }

    @Test
    @DisplayName("getProfile — invalid JSONB silent fallback (log warn, no 500)")
    void getProfile_malformedSocialJsonb_returnsNull() {
        UniversityProfile entity = UniversityProfile.builder()
                .universityCode("337")
                .socialLinks("{not-valid-json}")
                .documents("[also-broken-{")
                .build();
        when(repository.findByUniversityCode("337")).thenReturn(Optional.of(entity));

        UniversityProfileDto dto = service.getProfile("337");

        // Malformed JSONB yields null/empty, NOT exception
        assertThat(dto.getSocialLinks()).isNull();
        assertThat(dto.getDocuments()).isEmpty();
    }

    @Test
    @DisplayName("upsert — scope guard + new entity + JSONB serialization")
    void upsert_newEntity_createsAndSaves() throws Exception {
        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());
        UniversityProfileRequest req = new UniversityProfileRequest();
        req.setPhone("+998901111111");
        req.setEmail("test@337.uz");
        req.setSocialLinks(SocialLinksDto.builder().telegram("t.me/337").build());

        when(repository.findByUniversityCode("337")).thenReturn(Optional.empty());
        when(repository.save(any(UniversityProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UniversityProfileDto result = service.upsert("337", req);

        verify(scopeResolver).currentScope();
        assertThat(result.getPhone()).isEqualTo("+998901111111");
        // social_links serialized as JSON string in entity, parsed back to DTO
        assertThat(result.getSocialLinks()).isNotNull();
        assertThat(result.getSocialLinks().getTelegram()).isEqualTo("t.me/337");
    }

    @Test
    @DisplayName("upsert — existing entity update")
    void upsert_existingEntity_updatesFields() {
        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());
        UniversityProfile existing = UniversityProfile.builder()
                .universityCode("337")
                .phone("OLD-PHONE")
                .build();
        when(repository.findByUniversityCode("337")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UniversityProfileRequest req = new UniversityProfileRequest();
        req.setPhone("NEW-PHONE");

        UniversityProfileDto result = service.upsert("337", req);

        assertThat(existing.getPhone()).isEqualTo("NEW-PHONE");
        assertThat(result.getPhone()).isEqualTo("NEW-PHONE");
    }

    @Test
    @DisplayName("upsert — documents list serialized to JSONB")
    void upsert_documentsList() {
        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());
        UniversityProfileRequest req = new UniversityProfileRequest();
        req.setDocuments(List.of(
                DocumentMetaDto.builder().type("CHARTER").name("Charter").fileKey("docs/charter.pdf").build(),
                DocumentMetaDto.builder().type("LICENSE").name("License").fileKey("docs/license.pdf").build()
        ));

        when(repository.findByUniversityCode("337")).thenReturn(Optional.empty());
        when(repository.save(any(UniversityProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UniversityProfileDto result = service.upsert("337", req);

        assertThat(result.getDocuments()).hasSize(2);
        assertThat(result.getDocuments().get(0).getName()).isEqualTo("Charter");
    }

    @Test
    @DisplayName("upsert — empty documents list serializes to null (column NULL, not '[]')")
    void upsert_emptyDocumentsList_nullSerialized() {
        when(scopeResolver.currentScope()).thenReturn(AccessScope.global());
        UniversityProfileRequest req = new UniversityProfileRequest();
        req.setDocuments(List.of());  // empty list

        when(repository.findByUniversityCode("337")).thenReturn(Optional.empty());
        when(repository.save(any(UniversityProfile.class))).thenAnswer(inv -> {
            UniversityProfile e = inv.getArgument(0);
            // Empty list serializes to null per service logic
            assertThat(e.getDocuments()).isNull();
            return e;
        });

        service.upsert("337", req);
    }
}
