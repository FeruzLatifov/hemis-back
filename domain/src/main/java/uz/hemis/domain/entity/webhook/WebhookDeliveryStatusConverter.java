package uz.hemis.domain.entity.webhook;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter: enum ↔ lowercase db value ('pending','success','failed','retry','dlq').
 *
 * <p>V016 migration CHECK constraint lowercase string'larni kutadi
 * ({@code status IN ('pending','success','failed','retry','dlq')}), enum nomi esa
 * uppercase ({@code PENDING}). Default {@code EnumType.STRING} uppercase'ni yozardi.</p>
 */
@Converter(autoApply = false)
public class WebhookDeliveryStatusConverter implements AttributeConverter<WebhookDeliveryStatus, String> {

    @Override
    public String convertToDatabaseColumn(WebhookDeliveryStatus attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public WebhookDeliveryStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : WebhookDeliveryStatus.fromDbValue(dbValue);
    }
}
