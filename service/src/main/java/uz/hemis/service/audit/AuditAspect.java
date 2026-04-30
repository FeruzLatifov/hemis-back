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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * Audit/meta maydonlar — changed_fields'dan olib tashlanadi (canonical camelCase).
     * Snapshot'larda (old_value/new_value) qoladi, faqat diff summary'dan chiqariladi.
     */
    private static final Set<String> META_FIELDS = Set.of(
            "version", "createTs", "createdBy", "updateTs", "updatedBy"
    );

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

        try {
            Object result = pjp.proceed();

            // JPA loadOldValue topa olmagan bo'lsa (raw JDBC service'lar — entity yo'q),
            // service runtime'da o'rnatgan ThreadLocal qiymatga fallback.
            if (oldValue == null) {
                Object threadLocalOld = AuditContextHolder.getOldValue();
                if (threadLocalOld != null) {
                    oldValue = toMap(threadLocalOld);
                }
            }

            // Natijadan new value olish.
            // DELETE da new_value har doim null — record o'chirildi, after-state yo'q.
            Map<String, Object> newValue = audited.action() == AuditAction.DELETE
                    ? null
                    : toMap(result);

            // Entity nomi — ustuvorlik:
            // 1. Service runtime'da o'rnatgan qiymat (AuditContextHolder) — dynamic table nomlari uchun.
            // 2. JPA @Table(name) qiymati (entityClass orqali).
            // 3. audited.entity() ga fallback.
            String entityName = AuditContextHolder.getEntityName();
            if (entityName == null) {
                entityName = resolveTableName(audited.entityClass());
            }
            if (entityName == null) {
                entityName = audited.entity();
            }

            // Entity ID ni aniqlash:
            // - CREATE da PK natijadan keladi (yangi yaratilgan record), argumentdagi qiymat
            //   (masalan apiKey = "gender") faqat klassifikator turini ifodalaydi → result ustun.
            // - UPDATE/DELETE da PK argumentdan keladi (oldValue yuklash uchun ham kerak).
            if (audited.action() == AuditAction.CREATE) {
                String resultId = extractIdFromResult(result);
                if (resultId != null) {
                    entityId = resultId;
                }
            } else if (entityId == null) {
                entityId = extractIdFromResult(result);
            }

            // Changed fields
            List<String> changedFields = (oldValue != null && newValue != null)
                    ? detectChangedFields(oldValue, newValue) : null;

            String description = audited.action() + " " + audited.entity()
                    + (entityId != null ? "[" + entityId + "]" : "");

            eventPublisher.publishEvent(ActivityEvent.builder()
                    .context(context)
                    .action(audited.action())
                    .entityType(audited.entity())
                    .entityId(entityId)
                    .entityName(entityName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .changedFields(changedFields)
                    .description(description)
                    .build());

            return result;
        } finally {
            AuditContextHolder.clear();
        }
    }

    /**
     * Entity ni DB dan yuklash va Map ga aylantirish.
     * entityManager.detach() bilan persistence context dan chiqariladi.
     */
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
                // CRITICAL: DO NOT detach! The same entity may be the "target" in the
                // calling service method. Detaching it would make save() fail silently
                // (merge creates a new managed instance but doesn't copy pending changes).
                // Instead, just convert to Map immediately — this captures the current state.
                return toMap(objectMapper.convertValue(entity, Map.class));
            }
        } catch (Exception e) {
            log.debug("Failed to load old value for audit (lazy collection expected): {}", e.getMessage());
        }
        return null;
    }

    /**
     * Entity ID ni argumentdan aniqlash: faqat keyArg ko'rsatilgan bo'lsa.
     * keyArg yo'q bo'lsa — null qaytaramiz, aspect natijadan olishga harakat qiladi
     * (CREATE'da yangi PK, UPDATE/DELETE'da result.id/code).
     */
    private String resolveEntityId(ProceedingJoinPoint pjp, Audited audited) {
        String keyArg = audited.keyArg();
        if (keyArg == null || keyArg.isEmpty()) return null;

        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) return null;

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Parameter[] params = sig.getMethod().getParameters();
        for (int i = 0; i < params.length; i++) {
            if (keyArg.equals(params[i].getName()) || keyArg.equals(sig.getParameterNames()[i])) {
                return args[i] != null ? args[i].toString() : null;
            }
        }
        return null;
    }

    private AuditContext buildContext() {
        AuditContext.AuditContextBuilder builder = AuditContext.builder()
                .requestId(MDC.get("requestId"))
                .ip(MDC.get("clientIp"));

        HttpServletRequest request = currentRequest();
        if (request != null) {
            builder.userAgent(request.getHeader("User-Agent"));
            builder.endpoint(request.getMethod() + " " + request.getRequestURI());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            // JWT'da sub = userId (UUID), username — alohida claim.
            // Ikkalasini ham snapshot qilamiz: userId stable identifier,
            // username — auditor uchun human-readable, cross-DB join'siz.
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                String sub = jwt.getSubject();
                if (sub != null) {
                    try {
                        builder.userId(UUID.fromString(sub));
                    } catch (IllegalArgumentException ignored) {
                        builder.username(sub);
                    }
                }
                String usernameClaim = jwt.getClaimAsString("username");
                if (usernameClaim != null && !usernameClaim.isBlank()) {
                    builder.username(usernameClaim);
                }
                String fullNameClaim = jwt.getClaimAsString("full_name");
                if (fullNameClaim != null && !fullNameClaim.isBlank()) {
                    builder.fullName(fullNameClaim);
                }
            } else {
                String name = auth.getName();
                if (name != null) {
                    try {
                        builder.userId(UUID.fromString(name));
                    } catch (IllegalArgumentException ignored) {
                        builder.username(name);
                    }
                }
            }
        }

        return builder.build();
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
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

    private String resolveTableName(Class<?> entityClass) {
        if (entityClass == null || entityClass == void.class) return null;
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        return entityClass.getSimpleName();
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
        // Normalize keys to canonical camelCase to align entity (Jackson default)
        // with DTO (@JsonProperty snake_case). Compare intersection only — skips
        // entity-only meta fields (version, updateTs, ...) and DTO-only enrichment.
        // Output uses the original key names from new_value so changed_fields
        // matches the JSON keys in new_value/old_value.
        Map<String, String> oldByCanonical = canonicalKeyIndex(oldVal);
        Map<String, String> newByCanonical = canonicalKeyIndex(newVal);
        Set<String> common = new HashSet<>(oldByCanonical.keySet());
        common.retainAll(newByCanonical.keySet());
        common.removeAll(META_FIELDS);
        List<String> changed = new ArrayList<>();
        for (String canonical : common) {
            String oldKey = oldByCanonical.get(canonical);
            String newKey = newByCanonical.get(canonical);
            if (!Objects.equals(oldVal.get(oldKey), newVal.get(newKey))) {
                changed.add(newKey);
            }
        }
        return changed.isEmpty() ? null : changed;
    }

    private Map<String, String> canonicalKeyIndex(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();
        for (String key : map.keySet()) {
            result.put(toCamelCase(key), key);
        }
        return result;
    }

    private String toCamelCase(String key) {
        if (key == null || key.isEmpty()) return key;
        int i = 0;
        while (i < key.length() && key.charAt(i) == '_') i++;
        String stripped = key.substring(i);
        if (!stripped.contains("_")) return stripped;
        StringBuilder sb = new StringBuilder(stripped.length());
        boolean upper = false;
        for (char c : stripped.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

}
