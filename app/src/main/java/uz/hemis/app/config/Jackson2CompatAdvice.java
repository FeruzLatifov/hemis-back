package uz.hemis.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import uz.hemis.common.Jackson2Response;

/**
 * Bridge for Jackson 2 JsonNode types in Spring Boot 4 (Jackson 3).
 *
 * Spring Boot 4 uses Jackson 3 (tools.jackson) for HTTP message conversion.
 * Jackson 3 does not recognise Jackson 2 (com.fasterxml.jackson) JsonNode types
 * and serialises them as plain POJOs, exposing getter metadata instead of the
 * actual JSON tree content.
 *
 * This advice targets any controller annotated with @Jackson2Response.
 */
@ControllerAdvice(annotations = Jackson2Response.class)
@RequiredArgsConstructor
public class Jackson2CompatAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof JsonNode) {
            return objectMapper.convertValue(body, Object.class);
        }
        return body;
    }
}
