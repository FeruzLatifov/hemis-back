package uz.hemis.domain.entity.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transactional Outbox Pattern entity (Chris Richardson).
 *
 * <p>V015 migration'da yaratilgan jadval — atomic DB write + event publish.
 * {@code @Transactional} ichida bu row va asosiy domain entity birga yoziladi.
 * Background {@code OutboxPublisher} ({@code @Scheduled}) keyin {@code published_at IS NULL}
 * row'larni Kafka topic'ga jo'natadi.</p>
 *
 * <p><strong>Topic routing:</strong> {@code hemis.{aggregateType}.events.v{schemaVersion}}.
 * Misol: {@code hemis.employee.events.v1}, {@code hemis.classifier.events.v1}.</p>
 *
 * <p><strong>Multi-domain discriminator:</strong> {@code aggregateType} maydoni —
 * bir jadval employee, student, classifier, webhook event'lariga xizmat qiladi.</p>
 *
 * @see <a href="https://microservices.io/patterns/data/transactional-outbox.html">Transactional Outbox</a>
 * @since ADR-0010 / V015 (2026-05-08)
 */
@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    /** Primary key — DB tomonida {@code gen_random_uuid()} bilan generate qilinadi. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Domain discriminator — Kafka topic routing uchun.
     * Ruxsat etilgan qiymatlar (V015 CHECK constraint):
     * employee, employee_job, student, teacher, classifier, university, building, audit.
     */
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    /** Domain entity primary key (UUID yoki natural key string). */
    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    /**
     * Event tipi. Ruxsat etilgan qiymatlar (V015 CHECK constraint):
     * created, updated, deleted, synced, soft_deleted, restored, conflict_resolved.
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    /**
     * Full event payload (JSONB). Consumer-friendly — DB join'siz event body.
     * Schema evolution {@link #schemaVersion} orqali boshqariladi.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    /** Schema version — Apicurio/manual versioning uchun. Default 1. */
    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion = 1;

    /** Event sodir bo'lgan vaqt — DB default {@code CURRENT_TIMESTAMP}. */
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    /**
     * Kafka'ga jo'natilgan vaqt. {@code NULL} = hali jo'natilmagan (pending poll).
     * OutboxPublisher poll query: {@code WHERE published_at IS NULL}.
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /** Retry urinish soni. Max 100 (V015 CHECK constraint). */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /** Oxirgi retry xatosi (full exception message yoki Kafka error). */
    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    /** Request-level correlation ID (HTTP X-Correlation-ID header). Distributed tracing. */
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    /** Causation event ID (event chain). NULL = root event. */
    @Column(name = "causation_id", length = 100)
    private String causationId;

    /** Event yaratuvchi user/service identifikatori. */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    @PrePersist
    void prePersist() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
    }

    /**
     * Kafka topic nomi — {@code hemis.{aggregateType}.events.v{schemaVersion}}.
     * Misol: {@code hemis.classifier.events.v1}.
     */
    @Transient
    public String getKafkaTopic() {
        return "hemis." + aggregateType + ".events.v" + schemaVersion;
    }

    /** {@code true} = hali Kafka'ga jo'natilmagan. */
    @Transient
    public boolean isPending() {
        return publishedAt == null;
    }
}
