package uz.hemis.api.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.employee.EmployeeSyncAcceptedResponse;
import uz.hemis.common.dto.employee.EmployeeSyncDto;
import uz.hemis.common.vo.Pinfl;
import uz.hemis.service.employee.EmployeeSyncProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Univer (per-OTM Yii2 e_employee) → markaz {@code employee} + {@code employee_job} sync.
 *
 * <p>Async via Kafka (markaz ichida). Univer hech narsa bilmaydi — sof REST POST.
 * Markaz controller pre-validate qilib qabul qiladi → KafkaTemplate publish (key=PINFL) →
 * 202 Accepted qaytaradi. DB upsert async {@link uz.hemis.service.employee.EmployeeSyncConsumer}
 * ichida sodir bo'ladi.</p>
 *
 * <p>Bir xil PINFL har doim bir xil Kafka partition'ga tushadi → bitta consumer thread
 * serial qayta ishlaydi. 224 OTM birga sync qilsa ham — Hibernate {@code @Version}
 * optimistic lock collision yo'q.</p>
 *
 * <p>University identification:
 * <ol>
 *   <li><b>Production:</b> OAuth2 client_credentials — universityCode JWT {@code university_code} claim'dan</li>
 *   <li><b>Dev/test:</b> HTTP header {@code X-University-Code} (localhost only, security filter permitAll)</li>
 * </ol>
 * URL'da universityCode YO'Q — caller spoofing'dan himoyalanish (ministry convention).
 *
 * @since ADR-0010
 */
@RestController
@RequestMapping("/api/v1/university")
@Tag(name = "University Employee Sync", description = "OTM xodimlarini markazga yuborish (async via Kafka)")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EmployeeSyncController {

    private static final long PUBLISH_TIMEOUT_DEFAULT_MS = 30_000L;

    private final EmployeeSyncProducer producer;

    @PostMapping("/employees/sync")
    @Operation(summary = "Xodimlarni sync qilish (async)",
            description = "OTM xodimlar ro'yxatini markaz Kafka topic'iga publish qiladi. " +
                    "DB upsert async — 202 Accepted darhol qaytadi. " +
                    "Per-row natija {@code employee_sync_log} jadvalidan {@code batchId} bo'yicha. " +
                    "Idempotent: pinfl (employee), (universityCode, sourceUid) (job) bo'yicha INSERT ON CONFLICT.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Sync queue'ga olindi"),
            @ApiResponse(responseCode = "400", description = "Validatsiya xatosi (request body)"),
            @ApiResponse(responseCode = "401", description = "universityCode aniqlanmadi"),
            @ApiResponse(responseCode = "503", description = "Kafka publish timeout — Univer keyinroq retry qilsin")
    })
    public ResponseEntity<ResponseWrapper<EmployeeSyncAcceptedResponse>> sync(
            @Parameter(hidden = true) HttpServletRequest request,
            @Valid @RequestBody @NotEmpty List<EmployeeSyncDto> items) {
        String universityCode = resolveUniversityCode(request);
        String syncUser = resolveSyncUser(universityCode);
        UUID batchId = UUID.randomUUID();

        log.info("POST /university/employees/sync universityCode={} batchId={} itemCount={}",
                universityCode, batchId, items.size());

        List<EmployeeSyncAcceptedResponse.RejectionDetail> rejections = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (EmployeeSyncDto dto : items) {
            // Pre-validate PINFL: invalid bo'lsa Kafka'ga qo'ymaymiz (consumer ham reject qilardi
            // — lekin sinxron rad etish Univer tomonida xato tushunarli bo'ladi).
            if (!Pinfl.isValid(dto.getPinfl())) {
                rejections.add(new EmployeeSyncAcceptedResponse.RejectionDetail(
                        mask(dto.getPinfl()),
                        dto.getSourceUid(),
                        "Invalid PINFL (must be 14 digits)"));
                continue;
            }
            futures.add(producer.publish(batchId, universityCode, syncUser, dto));
        }

        // acks=all yetib kelishini kutamiz — 202 oldin yo'qotilgan xabar bo'lmasin.
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(PUBLISH_TIMEOUT_DEFAULT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            log.error("Kafka publish timeout universityCode={} batchId={} pendingCount={}",
                    universityCode, batchId, futures.size(), te);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Kafka publish timeout — keyinroq qayta urinib ko'ring");
        } catch (Exception ex) {
            log.error("Kafka publish failed universityCode={} batchId={}", universityCode, batchId, ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Kafka publish failed — keyinroq qayta urinib ko'ring");
        }

        EmployeeSyncAcceptedResponse response = new EmployeeSyncAcceptedResponse(
                batchId, futures.size(), rejections.size(), rejections);

        log.info("Sync queued universityCode={} batchId={} accepted={} rejected={}",
                universityCode, batchId, futures.size(), rejections.size());

        return ResponseEntity.accepted().body(ResponseWrapper.success(response));
    }

    /**
     * universityCode aniqlash:
     *   1. JWT claim {@code university_code} (OAuth2 client_credentials).
     *   2. Dev fallback: HTTP header {@code X-University-Code} (faqat dev profile, security permitAll).
     */
    private String resolveUniversityCode(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String fromToken = jwt.getClaimAsString("university_code");
            if (fromToken != null && !fromToken.isBlank()) {
                return fromToken;
            }
        }
        String fromHeader = request.getHeader("X-University-Code");
        if (fromHeader != null && !fromHeader.isBlank()) {
            return fromHeader;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "universityCode aniqlanmadi — JWT 'university_code' claim yoki 'X-University-Code' header kerak");
    }

    /** Audit user — JWT subject (client_id), aks holda fallback. */
    private String resolveSyncUser(String universityCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) return sub;
        }
        return "univer-sync-" + universityCode;
    }

    private static String mask(String pinfl) {
        if (pinfl == null) return "(null)";
        if (pinfl.length() < 6) return "***";
        return pinfl.substring(0, 4) + "****" + pinfl.substring(pinfl.length() - 2);
    }
}
