package uz.hemis.api.university.controller.auth;

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
 * OAuth 2.0 token endpoint for 224 OTM B2B integrations.
 *
 * <p>Accepts ONLY {@code grant_type=client_credentials}. The Basic-auth credentials
 * are looked up in the {@code oauth_client} table — each university has a per-client
 * secret, IP whitelist and rate limit.</p>
 *
 * <p>Legacy password flow remains untouched on {@code POST /app/rest/v2/oauth/token}
 * (api-legacy). OTMs migrate at their own pace by switching URL + credentials
 * together — both require a code change anyway.</p>
 *
 * @since 2.1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/university")
@Tag(
    name = "OTM B2B Auth",
    description = "224 universitet backend (univer.php) uchun mashina avtorizatsiya — OAuth 2.0 client_credentials grant"
)
public class UniversityOAuthTokenController {

    private final OAuthClientTokenIssuer tokenIssuer;

    @Operation(
        summary = "OTM token olish (client_credentials)",
        description = """
            Universitet backend (univer.php) uchun mashina tokeni olish.

            **Auth model:** Basic auth header'da `client_id:client_secret` —
            qiymatlar `oauth_client` jadvalidan tekshiriladi (har OTM uchun
            alohida secret, IP whitelist va rate limit).

            **cURL misol:**
            ```bash
            curl -X POST "https://hemis.uz/api/v1/university/oauth/token" \\
              -u "univer_101:<your_client_secret>" \\
              -H "Content-Type: application/x-www-form-urlencoded" \\
              -d "grant_type=client_credentials"
            ```

            **JWT claim'lar:**
            - `typ`: `CLIENT`
            - `client_type`: `UNIVERSITY_BACKEND`
            - `university_code`: `<OTM kodi>`
            - `scope`: `machine`
            - `authorities`: ruxsat qatori
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token muvaffaqiyatli yaratildi",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "access_token": "eyJhbGciOiJIUzI1NiIs...",
                      "token_type": "bearer",
                      "expires_in": 3600,
                      "scope": "machine"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid grant_type — faqat 'client_credentials' qabul qilinadi",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                {"error":"unsupported_grant_type","error_description":"Only 'client_credentials' is supported on this endpoint"}
                """))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid client_id, secret yoki IP whitelist'dan tashqari",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                {"error":"invalid_client","error_description":"Invalid client credentials"}
                """))
        )
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

    /**
     * JSON variant — legacy {@code univer.php} HTTP clients sometimes default to JSON
     * Content-Type. Mirrors the dual-format pattern in {@code LegacyOAuthTokenController}.
     */
    @Operation(
        summary = "Universitet token olish (JSON body — client_credentials)",
        description = """
            Form-encoded variant'ning JSON body bilan ekvivalenti.
            Univer Yii2 frontend (PHP) ba'zi versiyalarida JSON RPC orqali token oladi.

            **Body misol:**
            ```json
            {
              "grant_type": "client_credentials",
              "scope": "students.view"
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid grant_type"),
        @ApiResponse(responseCode = "401", description = "Invalid client_id, secret yoki IP whitelist'dan tashqari")
    })
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
