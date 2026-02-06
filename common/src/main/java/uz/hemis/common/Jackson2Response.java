package uz.hemis.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for controllers that return Jackson 2 (com.fasterxml.jackson) objects.
 *
 * <p>Spring Boot 4 uses Jackson 3 (tools.jackson) for HTTP serialization.
 * Controllers annotated with @Jackson2Response will have their Jackson 2 JsonNode
 * responses automatically converted to Jackson 3 compatible objects.</p>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Jackson2Response {
}
