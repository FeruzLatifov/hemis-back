package uz.hemis.service.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uz.hemis.common.audit.*;

import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Audit Aspect — @Audited annotation bilan belgilangan service
 * methodlarni avtomatik audit qiladi.
 *
 * <p>CREATE/UPDATE/DELETE operatsiyalarni activity_log ga yozadi.</p>
 * <p>Old/new value va changed fields ni aniqlaydi.</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hemis.audit.enabled", havingValue = "true", matchIfMissing = false)
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        AuditContext context = buildContext();
        Map<String, Object> oldValue = null;

        String entityId = resolveEntityId(pjp, audited);

        // UPDATE/DELETE uchun — oldingi holatni DB dan yuklash
        if (entityId != null && audited.entityClass() != void.class
                && (audited.action() == AuditAction.UPDATE || audited.action() == AuditAction.DELETE)) {
            oldValue = loadOldValue(audited.entityClass(), entityId);
        }

        Object result;
        try {
            result = pjp.proceed();
        } catch (Exception e) {
            // ErrorEvent faqat GlobalExceptionHandler da publish qilinadi (duplicate oldini olish)
            throw e;
        }

        // Natijadan new value olish
        Map<String, Object> newValue = toMap(result);

        // DELETE da result void bo'lishi mumkin — oldValue ni newValue sifatida ham ko'rsatish
        if (audited.action() == AuditAction.DELETE && newValue == null) {
            newValue = oldValue;
        }

        // Entity nomini aniqlash
        String entityName = extractEntityName(result != null ? result : (oldValue != null ? oldValue : null));

        // Entity ID ni aniqlash (natijadan yoki argumentdan)
        if (entityId == null) {
            entityId = extractIdFromResult(result);
        }

        // Changed fields
        List<String> changedFields = (oldValue != null && newValue != null)
                ? detectChangedFields(oldValue, newValue) : null;

        eventPublisher.publishEvent(ActivityEvent.builder()
                .context(context)
                .action(audited.action())
                .entityType(audited.entity())
                .entityId(entityId)
                .entityName(entityName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedFields(changedFields)
                .description(audited.action() + " " + audited.entity())
                .build());

        return result;
    }

    /**
     * Entity ni DB dan yuklash va Map ga aylantirish.
     * entityManager.detach() bilan persistence context dan chiqariladi.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadOldValue(Class<?> entityClass, String entityId) {
        try {
            Object entity;
            try {
                UUID uuid = UUID.fromString(entityId);
                entity = entityManager.find(entityClass, uuid);
            } catch (IllegalArgumentException e) {
                entity = entityManager.find(entityClass, entityId);
            }
            if (entity != null) {
                entityManager.detach(entity);
                return toMap(objectMapper.convertValue(entity, Map.class));
            }
        } catch (Exception e) {
            log.warn("Failed to load old value for audit: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Entity ID ni aniqlash: keyArg bo'lsa — shu nomli argumentni topish,
     * aks holda birinchi UUID/String argumentdan olish.
     */
    private String resolveEntityId(ProceedingJoinPoint pjp, Audited audited) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) return null;

        String keyArg = audited.keyArg();
        if (keyArg != null && !keyArg.isEmpty()) {
            MethodSignature sig = (MethodSignature) pjp.getSignature();
            Parameter[] params = sig.getMethod().getParameters();
            for (int i = 0; i < params.length; i++) {
                if (keyArg.equals(params[i].getName()) || keyArg.equals(sig.getParameterNames()[i])) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
        }

        return extractEntityId(args);
    }

    private AuditContext buildContext() {
        AuditContext.AuditContextBuilder builder = AuditContext.builder()
                .requestId(MDC.get("requestId"))
                .ip(MDC.get("clientIp"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null) {
                try {
                    builder.userId(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                    builder.username(name);
                }
            }
        }

        return builder.build();
    }

    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) return null;
        Object first = args[0];
        if (first instanceof UUID uuid) return uuid.toString();
        if (first instanceof String s && s.length() < 128) return s;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Map) return (Map<String, Object>) obj;
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractEntityName(Object result) {
        if (result == null) return null;
        Map<String, Object> map;
        if (result instanceof Map) {
            map = (Map<String, Object>) result;
        } else {
            map = toMap(result);
        }
        if (map == null) return null;
        try {
            for (String key : List.of("name", "title", "code", "username")) {
                Object val = map.get(key);
                if (val != null) return val.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractIdFromResult(Object result) {
        if (result == null) return null;
        try {
            Map<String, Object> map = toMap(result);
            if (map == null) return null;
            Object id = map.get("id");
            if (id != null) return id.toString();
            Object code = map.get("code");
            if (code != null) return code.toString();
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> detectChangedFields(Map<String, Object> oldVal, Map<String, Object> newVal) {
        List<String> changed = new ArrayList<>();
        Set<String> allKeys = new HashSet<>(oldVal.keySet());
        allKeys.addAll(newVal.keySet());
        for (String key : allKeys) {
            Object o = oldVal.get(key);
            Object n = newVal.get(key);
            if (!Objects.equals(o, n)) {
                changed.add(key);
            }
        }
        return changed.isEmpty() ? null : changed;
    }

}
