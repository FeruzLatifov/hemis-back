package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.entity.UniversityFounder;
import uz.hemis.domain.entity.UniversityLifecycle;
import uz.hemis.service.university.UniversityExternalDataService;
import uz.hemis.service.university.UniversityInfoService;
import uz.hemis.service.university.UniversityOfficialService;
import uz.hemis.service.university.UniversityProfileService;
import uz.hemis.service.university.dto.UniversityCadastreDto;
import uz.hemis.service.university.dto.UniversityFounderDto;
import uz.hemis.service.university.dto.UniversityLegalDto;
import uz.hemis.service.university.dto.UniversityLifecycleDto;
import uz.hemis.service.university.dto.UniversityLifecycleRequest;
import uz.hemis.service.university.dto.OfficialDto;
import uz.hemis.service.university.dto.OfficialRequest;
import uz.hemis.service.university.dto.UniversityDashboardDto;
import uz.hemis.service.university.dto.UniversityProfileDto;
import uz.hemis.service.university.dto.UniversityProfileRequest;
import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/web/university")
@Tag(name = "University Info", description = "University legal, founders, lifecycle, cadastre")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class UniversityInfoController {

    private final UniversityInfoService universityInfoService;
    private final UniversityExternalDataService externalDataService;
    private final UniversityOfficialService officialService;
    private final UniversityProfileService profileService;

    @GetMapping("/{code}/dashboard")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get university dashboard", description = "Returns all info (legal, founders, lifecycle, cadastre) for one university")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<UniversityDashboardDto>> getDashboard(
            @Parameter(description = "University code")
            @PathVariable String code
    ) {
        log.info("Getting university dashboard for code: {}", code);
        UniversityDashboardDto dashboard = universityInfoService.getUniversityDashboard(code);
        return ResponseEntity.ok(ResponseWrapper.success(dashboard));
    }

    @GetMapping("/{code}/legal")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get legal entity info", description = "Returns legal entity information for a university")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Legal info retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<UniversityLegalDto>> getLegal(
            @Parameter(description = "University code")
            @PathVariable String code
    ) {
        log.info("Getting legal info for university code: {}", code);
        UniversityLegalDto legal = universityInfoService.getLegalDto(code);
        return ResponseEntity.ok(ResponseWrapper.success(legal));
    }

    @GetMapping("/{code}/founders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current founders", description = "Returns current founders list for a university")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Founders list retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<List<UniversityFounderDto>>> getFounders(
            @Parameter(description = "University code")
            @PathVariable String code
    ) {
        log.info("Getting founders for university code: {}", code);
        List<UniversityFounder> founders = universityInfoService.getFounders(code);
        if (founders == null) founders = Collections.emptyList();
        List<UniversityFounderDto> dtos = founders.stream().map(UniversityFounderDto::from).toList();
        return ResponseEntity.ok(ResponseWrapper.success(dtos));
    }

    @GetMapping("/{code}/lifecycle")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get lifecycle events", description = "Returns lifecycle events ordered by date descending for a university")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lifecycle events retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<List<UniversityLifecycleDto>>> getLifecycle(
            @Parameter(description = "University code")
            @PathVariable String code
    ) {
        log.info("Getting lifecycle events for university code: {}", code);
        List<UniversityLifecycle> lifecycle = universityInfoService.getLifecycle(code);
        if (lifecycle == null) lifecycle = Collections.emptyList();
        List<UniversityLifecycleDto> dtos = lifecycle.stream().map(UniversityLifecycleDto::from).toList();
        return ResponseEntity.ok(ResponseWrapper.success(dtos));
    }

    @GetMapping("/{code}/cadastre")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cadastre objects", description = "Returns cadastre objects list for a university")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cadastre objects retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<List<UniversityCadastreDto>>> getCadastre(
            @Parameter(description = "University code")
            @PathVariable String code
    ) {
        log.info("Getting cadastre objects for university code: {}", code);
        var cadastre = universityInfoService.getCadastre(code);
        if (cadastre == null) cadastre = Collections.emptyList();
        List<UniversityCadastreDto> dtos = cadastre.stream().map(UniversityCadastreDto::from).toList();
        return ResponseEntity.ok(ResponseWrapper.success(dtos));
    }

    // =====================================================
    // SYNC ENDPOINTS (trigger external API fetch)
    // =====================================================

    @PostMapping("/{code}/sync")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sync all external data", description = "Fetch legal entity + cadastre from external API and save. TIN is resolved automatically from university code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed"),
            @ApiResponse(responseCode = "400", description = "University not found or TIN is empty"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ResponseWrapper<UniversityDashboardDto>> syncAll(
            @Parameter(description = "University code") @PathVariable String code
    ) {
        log.info("Syncing all external data for university={}", code);
        externalDataService.syncAll(code);
        UniversityDashboardDto dashboard = universityInfoService.getUniversityDashboard(code);
        return ResponseEntity.ok(ResponseWrapper.success(dashboard));
    }

    @PostMapping("/{code}/lifecycle")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add lifecycle event", description = "Add a lifecycle event (CLOSED, MERGED, SPLIT, etc.)")
    public ResponseEntity<ResponseWrapper<UniversityLifecycleDto>> addLifecycleEvent(
            @PathVariable String code,
            @Valid @RequestBody UniversityLifecycleRequest request
    ) {
        log.info("Adding lifecycle event for university={}, type={}", code, request.getEventType());
        UniversityLifecycle saved = universityInfoService.addLifecycleEvent(request.toEntity(code));
        return ResponseEntity.ok(ResponseWrapper.success(UniversityLifecycleDto.from(saved)));
    }

    // =====================================================
    // OFFICIALS (rector, prorektors — ministry-appointed)
    // =====================================================

    @GetMapping("/{code}/officials")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get university officials", description = "Returns ministry-appointed officials. history=true shows former officials too.")
    public ResponseEntity<ResponseWrapper<List<OfficialDto>>> getOfficials(
            @PathVariable String code,
            @RequestParam(defaultValue = "false") boolean history
    ) {
        log.info("Getting officials for university={}, history={}", code, history);
        List<OfficialDto> officials = officialService.getOfficials(code, !history);
        return ResponseEntity.ok(ResponseWrapper.success(officials));
    }

    @PostMapping("/{code}/officials")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Appoint official", description = "Appoint a rector/prorektor. Previous holder is automatically deactivated.")
    public ResponseEntity<ResponseWrapper<OfficialDto>> appointOfficial(
            @PathVariable String code,
            @Valid @RequestBody OfficialRequest request
    ) {
        log.info("Appointing official: university={}, position={}", code, request.getPositionCode());
        OfficialDto official = officialService.appointOfficial(code, request);
        return ResponseEntity.ok(ResponseWrapper.success(official));
    }

    @DeleteMapping("/{code}/officials/{metaId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dismiss official", description = "Deactivate an official appointment with decree")
    public ResponseEntity<Void> removeOfficial(
            @PathVariable String code,
            @PathVariable java.util.UUID metaId,
            @RequestParam(required = false) String decree
    ) {
        log.info("Dismissing official: university={}, metaId={}, decree={}", code, metaId, decree);
        officialService.removeOfficial(metaId, decree);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lookup/person/{pinfl}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lookup person by PINFL", description = "Find person in employee, teacher, or external API. Document/birthDate needed for external API.")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> lookupPerson(
            @PathVariable String pinfl,
            @RequestParam(required = false) String document,
            @RequestParam(required = false) String birthDate
    ) {
        log.info("Looking up person: pinfl={}, doc={}, birth={}", pinfl, document != null ? "***" : null, birthDate);
        Map<String, Object> person = officialService.lookupByPinfl(pinfl, document, birthDate);
        return ResponseEntity.ok(ResponseWrapper.success(person));
    }

    // =====================================================
    // PROFILE (contacts, social links, documents — admin-managed)
    // =====================================================

    @GetMapping("/{code}/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get university public profile", description = "Contacts, social links, description, documents")
    public ResponseEntity<ResponseWrapper<UniversityProfileDto>> getProfile(
            @PathVariable String code
    ) {
        log.info("Getting profile for university={}", code);
        return ResponseEntity.ok(ResponseWrapper.success(profileService.getProfile(code)));
    }

    @PutMapping("/{code}/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upsert university public profile", description = "Create or update contacts, social links, documents")
    public ResponseEntity<ResponseWrapper<UniversityProfileDto>> updateProfile(
            @PathVariable String code,
            @Valid @RequestBody UniversityProfileRequest request
    ) {
        log.info("Upserting profile for university={}", code);
        return ResponseEntity.ok(ResponseWrapper.success(profileService.upsert(code, request)));
    }

    @GetMapping("/positions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get leadership positions", description = "Returns position classifier for university leadership")
    public ResponseEntity<ResponseWrapper<List<Map<String, Object>>>> getPositions() {
        List<Map<String, Object>> positions = officialService.getLeadershipPositions();
        return ResponseEntity.ok(ResponseWrapper.success(positions));
    }

}
