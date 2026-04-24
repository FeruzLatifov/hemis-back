package uz.hemis.api.external.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.security.service.OAuthClientTokenIssuer;

import java.util.Map;

/**
 * OAuth 2.0 token endpoint for external state systems
 * (MyGov, OneID, Hokimiyat, GUVD, Tax, BIMM, …).
 *
 * <p>Accepts ONLY {@code grant_type=client_credentials}. Each external partner has
 * its own row in {@code oauth_client} ({@code client_type=EXTERNAL_SYSTEM}) with a
 * dedicated secret, IP whitelist and rate limit.</p>
 *
 * <p>Functionally identical to {@code UniversityOAuthTokenController} — the URL
 * separation is intentional: distinct Swagger sections, distinct metrics, and
 * distinct sunset / contract lifecycles per audience.</p>
 *
 * @since 2.1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/external")
@Tag(
    name = "External Systems Auth",
    description = "Tashqi davlat sistemalari (MyGov, OneID, Hokimiyat, …) uchun mashina avtorizatsiya — OAuth 2.0 client_credentials grant"
)
public class ExternalOAuthTokenController {

    private final OAuthClientTokenIssuer tokenIssuer;

    @Operation(
        summary = "External token olish (client_credentials)",
        description = """
            Davlat sistemasi (MyGov, OneID, …) uchun mashina tokeni olish.

            **Auth model:** Basic auth header'da `client_id:client_secret` —
            `oauth_client` jadvalidan (`client_type=EXTERNAL_SYSTEM`).

            **cURL misol:**
            ```bash
            curl -X POST "https://hemis.uz/api/v1/external/oauth/token" \\
              -u "mygov_sync:<secret>" \\
              -H "Content-Type: application/x-www-form-urlencoded" \\
              -d "grant_type=client_credentials"
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token muvaffaqiyatli yaratildi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid grant_type"),
        @ApiResponse(responseCode = "401", description = "Invalid client_id, secret yoki IP whitelist'dan tashqari")
    })
    @PostMapping(
        value = "/oauth/token",
        consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<?> tokenForm(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "grant_type", required = false) String grantType,
            @RequestParam(value = "scope", required = false) String scope,
            HttpServletRequest request
    ) {
        return tokenIssuer.issue(authorization, grantType, scope, request);
    }

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> tokenJson(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) {
        String grantType = body == null ? null : body.get("grant_type");
        String scope = body == null ? null : body.get("scope");
        return tokenIssuer.issue(authorization, grantType, scope, request);
    }
}
