package uz.hemis.common.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka payload — markaz HTTP entry → consumer.
 *
 * <p>Topic: {@code hemis.employee.sync.inbound.v1}, key = PINFL (partition routing).</p>
 *
 * <p>Bir xil PINFL → bir xil partition → bir consumer thread serial qayta ishlaydi.
 * Shuning uchun 224 OTM bir xil xodimni concurrent push qilsa ham — Hibernate
 * {@code @Version} optimistic lock collision YO'Q (lock JPA emas, Kafka partition).</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeSyncEvent(
        UUID batchId,
        String universityCode,
        String syncUser,
        EmployeeSyncDto payload,
        Instant publishedAt
) implements Serializable {
}
