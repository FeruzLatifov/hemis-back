package uz.hemis.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Service methodlarga qo'yiladigan audit annotation.
 *
 * <p>AuditAspect tomonidan ishlov beriladi — method bajarilganda
 * ActivityEvent publish qilinadi.</p>
 *
 * <pre>
 * {@code @Audited(action = AuditAction.CREATE, entity = "University")}
 * public UniversityDetailDto create(UniversityRequestDto dto) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    AuditAction action();
    String entity();
    Class<?> entityClass() default void.class;
    String keyArg() default "";
}
