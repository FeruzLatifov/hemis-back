package uz.hemis.service.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WireMock infrastructure smoke test — pattern demonstration.
 *
 * <p>External HTTP integration testlar uchun ushbu pattern'ni qayta ishlating.
 * Misol foydalanuvchilar: {@code ApiMspdClient}, {@code BimmService},
 * {@code GuvdLegacyService}, {@code HemisApiService}.</p>
 *
 * <p><strong>Pattern:</strong></p>
 * <ol>
 *   <li>{@code @BeforeEach} — WireMock server dynamic port'da ishga tushiriladi</li>
 *   <li>{@code stubFor(...)} — kutilgan HTTP request → response stub</li>
 *   <li>RestClient/HttpClient ni stub URL bilan inject qilish</li>
 *   <li>Test ichida real HTTP call → WireMock o'rnatilgan stub'ni qaytaradi</li>
 *   <li>{@code @AfterEach} — server stop (Docker/network resource leak yo'q)</li>
 * </ol>
 *
 * <p>Production foydalanuvchi bo'lib: MSPD client uchun
 * {@code BeforeEach(client = new ApiMspdClient(stubUrl))} qiling va MSPD
 * API'ning OAuth token endpoint, cadastre query, error path'larni stub
 * qilib testlang.</p>
 */
@DisplayName("WireMock — HTTP stub infrastructure smoke test")
class WireMockSampleTest {

    private WireMockServer wireMockServer;
    private RestClient client;

    @BeforeEach
    void setUp() {
        // Dynamic port — CI/parallel test'larda konflikt bo'lmasligi uchun
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        client = RestClient.builder()
                .baseUrl("http://localhost:" + wireMockServer.port())
                .build();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("GET /health → 200 + JSON body returned by stub")
    void getEndpoint_returnsStubbedJson() {
        wireMockServer.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"UP\",\"version\":\"1.0.0\"}")));

        String body = client.get()
                .uri("/health")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("\"status\":\"UP\"");
        assertThat(body).contains("\"version\":\"1.0.0\"");
    }

    @Test
    @DisplayName("Request header validation — stub matches only with correct Authorization")
    void headerMatcher_requiresExactBearer() {
        wireMockServer.stubFor(get(urlEqualTo("/protected"))
                .withHeader("Authorization", equalTo("Bearer expected-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("granted")));

        // To'g'ri token — 200
        String ok = client.get()
                .uri("/protected")
                .header("Authorization", "Bearer expected-token")
                .retrieve()
                .body(String.class);
        assertThat(ok).isEqualTo("granted");

        // Noto'g'ri token — WireMock 404 qaytaradi (stub mismatch)
        assertThatThrownBy(() ->
                client.get()
                        .uri("/protected")
                        .header("Authorization", "Bearer wrong-token")
                        .retrieve()
                        .body(String.class)
        ).hasMessageContaining("404");
    }

    @Test
    @DisplayName("5xx error stub — client kutilgan retry/fail xulqini sinab ko'radi")
    void error5xx_propagatesAsException() {
        wireMockServer.stubFor(get(urlEqualTo("/flaky"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("service unavailable")));

        assertThatThrownBy(() ->
                client.get()
                        .uri("/flaky")
                        .retrieve()
                        .body(String.class)
        ).hasMessageContaining("503");
    }
}
