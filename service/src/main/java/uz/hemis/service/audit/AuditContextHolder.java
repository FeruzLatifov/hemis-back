package uz.hemis.service.audit;

/**
 * ThreadLocal context for @Audited methods to override audit fields at runtime.
 *
 * <p>Used when static @Audited values aren't enough — e.g., universal services
 * where the actual table name is resolved dynamically from method args.</p>
 *
 * <p>AuditAspect reads values after pjp.proceed() and clears the holder in a
 * finally block, so callers don't need to manage cleanup.</p>
 */
public final class AuditContextHolder {

    private static final ThreadLocal<String> ENTITY_NAME = new ThreadLocal<>();
    private static final ThreadLocal<Object> OLD_VALUE = new ThreadLocal<>();
    private static final ThreadLocal<String> SCOPE_KEY = new ThreadLocal<>();

    private AuditContextHolder() {}

    public static void setEntityName(String name) {
        ENTITY_NAME.set(name);
    }

    public static String getEntityName() {
        return ENTITY_NAME.get();
    }

    /** Owner of the record (OTM code) — what an owner-scoped history query filters on. */
    public static void setScopeKey(String scopeKey) {
        SCOPE_KEY.set(scopeKey);
    }

    public static String getScopeKey() {
        return SCOPE_KEY.get();
    }

    /** Pass a snapshot of the entity before the change (DTO/Map/entity). */
    public static void setOldValue(Object value) {
        OLD_VALUE.set(value);
    }

    public static Object getOldValue() {
        return OLD_VALUE.get();
    }

    public static void clear() {
        ENTITY_NAME.remove();
        OLD_VALUE.remove();
        SCOPE_KEY.remove();
    }
}
