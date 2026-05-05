package uz.hemis.app.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("TraceIdFilter")
class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Yangi traceId generate qiladi header bo'lmasa")
    void generatesNewTraceIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(any(), any());
        // Filter MDC ni clear qiladi finally'da, lekin response header qoladi
        String requestId = resp.getHeader(TraceIdFilter.HEADER_REQUEST_ID);
        assertThat(requestId).isNotNull().isNotBlank();
        assertThat(requestId).hasSize(8); // UUID short
    }

    @Test
    @DisplayName("X-Request-ID header'ni propagate qiladi")
    void propagatesXRequestId() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Request-ID", "client-trace-12345");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        assertThat(resp.getHeader(TraceIdFilter.HEADER_REQUEST_ID)).isEqualTo("client-trace-12345");
    }

    @Test
    @DisplayName("X-Trace-Id header — fallback (X-Request-ID bo'lmasa)")
    void fallbackXTraceId() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Trace-Id", "trace-from-gateway");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        assertThat(resp.getHeader(TraceIdFilter.HEADER_REQUEST_ID)).isEqualTo("trace-from-gateway");
    }

    @Test
    @DisplayName("MDC traceId chain ichida set qilingan, finally'da clear")
    void mdcSetDuringChainAndClearedAfter() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Request-ID", "test-trace");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);
        // Chain ichida MDC ko'rinishi kerak
        doAnswer(inv -> {
            assertThat(MDC.get(TraceIdFilter.MDC_TRACE_ID)).isEqualTo("test-trace");
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(req, resp, chain);

        // Filter chain tugagandan keyin MDC clean
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_ID)).isNull();
    }

    @Test
    @DisplayName("64 chardan ortiq header — truncate (log injection oldini olish)")
    void truncatesLongHeader() throws Exception {
        String longTrace = "x".repeat(200);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Request-ID", longTrace);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, resp, chain);

        assertThat(resp.getHeader(TraceIdFilter.HEADER_REQUEST_ID)).hasSize(64);
    }
}
