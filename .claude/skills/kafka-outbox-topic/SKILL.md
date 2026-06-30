---
name: kafka-outbox-topic
description: Yangi Kafka outbox topic + producer + consumer + DLQ qo'shish (ADR-0007). Trigger - "outbox", "Kafka topic qo'sh", "sync event", "OTM ga yubor", "event publishing".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Add Kafka Outbox Topic

> ADR-0007 (Kafka-first sync) + ADR-0010 (Employee Sync Outbox). Outbox pattern — DB transactional + Kafka reliable.

## Workflow

### 1. Naming convention

| Topic | Format | Misol |
|-------|--------|-------|
| Domain events | `hemis.<domain>.<event>.v1` | `hemis.employee.created.v1` |
| DLQ | `<topic>.dlq` | `hemis.employee.created.v1.dlq` |
| Compacted (state) | `hemis.<domain>.state.v1` | `hemis.classifier.state.v1` |

### 2. Outbox jadval (agar yo'q bo'lsa)

`liquibase-changeset` skill orqali V### migration:

```sql
CREATE TABLE IF NOT EXISTS outbox_event (
    id            BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(64)  NOT NULL,
    aggregate_id   VARCHAR(64)  NOT NULL,
    event_type    VARCHAR(128) NOT NULL,
    topic         VARCHAR(128) NOT NULL,
    payload       JSONB        NOT NULL,
    headers       JSONB        DEFAULT '{}',
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING',  -- PENDING/SENT/FAILED
    retry_count   INTEGER      NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at       TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_event(status, next_retry_at) WHERE status = 'PENDING';
```

### 3. Producer (transactional write)

```java
@Service @RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repo;
    private final OutboxEventRepository outbox;
    private final ObjectMapper json;

    @Transactional   // DB + outbox bitta tx ichida
    public Employee create(EmployeeDto dto) {
        Employee saved = repo.save(toEntity(dto));
        outbox.save(OutboxEvent.builder()
            .aggregateType("Employee")
            .aggregateId(saved.getId().toString())
            .eventType("EmployeeCreated")
            .topic("hemis.employee.created.v1")
            .payload(json.valueToTree(toEvent(saved)))
            .build());
        return saved;
    }
}
```

### 4. Outbox poller (`@Scheduled`)

```java
@Component @RequiredArgsConstructor
public class OutboxPoller {
    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        outbox.findPendingBatch(100).forEach(e -> {
            try {
                kafka.send(e.getTopic(), e.getAggregateId(), e.getPayload().toString()).get();
                e.markSent();
            } catch (Exception ex) {
                e.incrementRetry();   // exponential backoff
                if (e.getRetryCount() >= 5) e.markFailed();   // → DLQ
            }
        });
    }
}
```

### 5. Consumer (idempotent)

```java
@KafkaListener(topics = "hemis.employee.created.v1", groupId = "univer-sync")
public void onEmployeeCreated(@Payload String payload, @Header("kafka_receivedMessageKey") String key) {
    if (idempotencyStore.seen(key)) return;   // duplicate skip
    // process...
    idempotencyStore.mark(key);
}
```

### 6. DLQ + monitoring

```yaml
# application.yml
spring.kafka:
  listener.ack-mode: MANUAL
  consumer.properties.isolation.level: read_committed
  producer.acks: all
  producer.transaction-id-prefix: hemis-tx-
```

Metrics: `kafka_producer_record_send_total`, `outbox_pending_count`, `outbox_dlq_count`.

### 7. Topic provisioning

`docker-compose.yml` yoki `KafkaTopicConfig.java`:
```java
@Bean public NewTopic employeeCreated() {
    return TopicBuilder.name("hemis.employee.created.v1")
        .partitions(6).replicas(3).config(TopicConfig.RETENTION_MS_CONFIG, "604800000").build();
}
```

## Verification

```bash
# Topic mavjud
docker exec hemis-kafka kafka-topics --bootstrap-server localhost:9092 --list | grep <topic>

# Outbox queue holati
psql -d $DB_MASTER_NAME -c "SELECT status, count(*) FROM outbox_event GROUP BY status;"

# Consumer lag
docker exec hemis-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group univer-sync
```

## Constraints

- ❌ Outbox + DB save'ni alohida tx'da → eventual consistency lost
- ❌ Consumer idempotent emas → duplicate ish
- ❌ DLQ yo'q → poison pill butun consumer'ni to'xtatadi
- ❌ Topic naming `.v1` versionsiz → schema evolution buziladi
- ❌ `acks=1` (faqat leader) — data loss riski
- ✅ `acks=all` + `enable.idempotence=true`

## See also

- ADR-0007 (sync architecture) · ADR-0010 (employee outbox)
- `domain/.../entity/outbox/OutboxEvent.java` — mavjud outbox entity (`@Table(name = "outbox_event")`)
- `.claude/skills/liquibase-changeset` — migration
- `docs/architecture/` — sync diagrams
