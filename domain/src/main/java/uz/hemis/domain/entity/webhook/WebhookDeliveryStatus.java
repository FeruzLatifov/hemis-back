package uz.hemis.domain.entity.webhook;

/**
 * Webhook delivery state machine.
 *
 * <p>State transitions:</p>
 * <pre>
 *   pending → success | failed | retry | dlq
 *   retry   → success | failed | retry | dlq
 * </pre>
 *
 * @see V015_create_webhook_infrastructure.sql
 */
public enum WebhookDeliveryStatus {
    /** Consumer yuborishni boshladi, hali javob kelmagan. */
    PENDING("pending"),

    /** Univer 2xx HTTP status qaytardi. */
    SUCCESS("success"),

    /** Univer 4xx (terminal — qayta yuborilmaydi, payload xato). */
    FAILED("failed"),

    /** Timeout / 5xx / network error — exponential backoff bilan keyingi attempt. */
    RETRY("retry"),

    /** Max retries tugadi — Dead Letter Queue. Manual admin tekshiruvi. */
    DLQ("dlq");

    private final String dbValue;

    WebhookDeliveryStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static WebhookDeliveryStatus fromDbValue(String value) {
        for (WebhookDeliveryStatus s : values()) {
            if (s.dbValue.equals(value)) return s;
        }
        throw new IllegalArgumentException("Unknown WebhookDeliveryStatus: " + value);
    }

    /** Terminal state — keyingi attempt yo'q. */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == DLQ;
    }
}
