package uz.hemis.service.outbox;

/**
 * Outbox event publish xatosi (JSON serialization fail).
 *
 * <p>Transactional bo'lib chiqilgani uchun parent transaction rollback bo'ladi —
 * domain entity ham, outbox row ham yozilmaydi.</p>
 */
public class OutboxPublishException extends RuntimeException {
    public OutboxPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
