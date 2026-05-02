package uz.hemis.api.external.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import uz.hemis.common.dto.TokenResponse;
import uz.hemis.security.service.OAuthClientTokenIssuer;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("ExternalOAuthTokenController unit tests")
class ExternalOAuthTokenControllerTest {

    private OAuthClientTokenIssuer tokenIssuer;
    private ExternalOAuthTokenController controller;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        tokenIssuer = mock(OAuthClientTokenIssuer.class);
        controller = new ExternalOAuthTokenController(tokenIssuer);
        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("tokenForm delegates all params to issuer")
    void tokenForm_delegatesParams() {
        TokenResponse expected = new TokenResponse("access", "Bearer", "refresh", 3600, "scope-x");
        doReturn(ResponseEntity.ok(expected))
                .when(tokenIssuer).issue(eq("Basic abc"), eq("client_credentials"), eq("scope-x"), any());

        ResponseEntity<?> response = controller.tokenForm("Basic abc", "client_credentials", "scope-x", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(tokenIssuer).issue(eq("Basic abc"), eq("client_credentials"), eq("scope-x"), eq(request));
    }

    @Test
    @DisplayName("tokenForm propagates issuer error responses")
    void tokenForm_propagatesIssuerError() {
        doReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_client")))
                .when(tokenIssuer).issue(any(), any(), any(), any());

        ResponseEntity<?> response = controller.tokenForm(null, "client_credentials", null, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("tokenJson reads grant_type and scope from body")
    void tokenJson_readsBody() {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("scope", "read");
        doReturn(ResponseEntity.ok(new TokenResponse("at", "Bearer", "rt", 60, "read")))
                .when(tokenIssuer).issue(any(), any(), any(), any());

        controller.tokenJson("Basic xyz", body, request);

        ArgumentCaptor<String> grantCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> scopeCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenIssuer).issue(eq("Basic xyz"), grantCaptor.capture(), scopeCaptor.capture(), eq(request));
        assertThat(grantCaptor.getValue()).isEqualTo("client_credentials");
        assertThat(scopeCaptor.getValue()).isEqualTo("read");
    }

    @Test
    @DisplayName("tokenJson handles null body without NPE")
    void tokenJson_nullBody() {
        doReturn(ResponseEntity.badRequest().body(Map.of("error", "invalid_request")))
                .when(tokenIssuer).issue(any(), any(), any(), any());

        ResponseEntity<?> response = controller.tokenJson("Basic xyz", null, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(tokenIssuer).issue(eq("Basic xyz"), eq(null), eq(null), eq(request));
    }

    @Test
    @DisplayName("tokenJson handles missing grant_type/scope keys")
    void tokenJson_missingKeys() {
        Map<String, String> body = Map.of("foo", "bar");
        doReturn(ResponseEntity.badRequest().build())
                .when(tokenIssuer).issue(any(), any(), any(), any());

        controller.tokenJson(null, body, request);

        verify(tokenIssuer).issue(eq(null), eq(null), eq(null), eq(request));
    }
}
