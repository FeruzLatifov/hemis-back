# HEMIS Webhook — Univer Implementation Guide

> **Auditoriya:** Univer kod jamoa (PHP Yii2 dasturchilari) + 224 OTM IT (deploy).
>
> **Bir gapda:** HEMIS markaz endi sizga real-time event yuboradi (klassifikator, qoidalar).
> Bu — GitHub/Telegram webhook bilan bir xil idiom. Univer kodbase'iga bir marta yoziladi,
> 224 OTM avtomatik update oladi.

---

## 1. Konsepsiya — 30 soniyalik tushuncha

**Hozir:** Univer markazga ma'lumot **yuborib** turibdi (talaba, xodim).

**Endi:** Markaz Univer'ga ma'lumot **qaytarib bera oladi** — yangi rayon qo'shildi, "15-noyabrgacha talaba qo'shish taqiq" qoidasi joriy etildi, OTM blocked.

**Analog:**
```
Telegram bot       ──webhook──▶  Sizning bot serveri
GitHub             ──webhook──▶  CI/CD pipeline
HEMIS markaz       ──webhook──▶  Sizning Univer
```

---

## 2. HTTP shartnomasi

### Request

```
POST {callbackUrl}
Content-Type: application/json
X-Hemis-Signature: a3c7d9e2...         ← HMAC SHA-256(secret, timestamp.body)
X-Hemis-Timestamp: 1715568000           ← Unix epoch seconds (UTC)
X-Hemis-University-Code: 337            ← Sizning OTM kod

{
  "event_id":       "550e8400-e29b-41d4-a716-446655440000",
  "event_type":     "classifier.updated",
  "aggregate_type": "classifier",
  "aggregate_id":   "123",
  "occurred_at":    "2026-05-13T10:30:00",
  "schema_version": 1,
  "data":           { ... event-specific payload ... }
}
```

### Response (tezda — 30 sekund ichida)

```
HTTP/1.1 200 OK
Content-Type: application/json

{ "status": "accepted", "event_id": "550e8400-..." }
```

### Status code semantikasi

| HTTP | Markaz tomondan | Univer side niyat |
|------|-----------------|--------------------|
| 2xx  | SUCCESS         | Event qabul qilindi, qayta yuborilmaydi |
| 4xx  | FAILED (terminal) | Payload xato (schema mos kelmadi) — markaz qayta yubormaydi |
| 5xx  | RETRY           | Vaqtinchalik xato (DB down) — markaz qayta yuboradi (exponential backoff) |
| Timeout (30s+) | RETRY | Markaz timeout sezadi, qayta yuboradi |

---

## 3. Step-by-step implementation (Yii2)

### 3.1 Migration — `hemis_callback_log` jadval

```php
// univer/migrations/m260513_120000_create_hemis_callback_log.php
public function safeUp()
{
    $this->createTable('hemis_callback_log', [
        'event_id'      => $this->string(64)->notNull(),  // UUID from markaz
        'event_type'    => $this->string(50)->notNull(),
        'payload'       => $this->json(),
        'received_at'   => $this->integer()->notNull(),
        'applied_at'    => $this->integer()->null(),
        'status'        => "ENUM('queued','applied','failed','duplicate') NOT NULL DEFAULT 'queued'",
        'error_message' => $this->text()->null(),
        'PRIMARY KEY (event_id)',
    ]);
    $this->createIndex('idx_callback_status', 'hemis_callback_log', 'status');
    $this->createIndex('idx_callback_received', 'hemis_callback_log', 'received_at');
}
```

### 3.2 Controller — callback endpoint

```php
// univer/controllers/HemisCallbackController.php
namespace app\controllers;

use Yii;
use yii\rest\Controller;
use yii\web\ForbiddenHttpException;

class HemisCallbackController extends Controller
{
    public $enableCsrfValidation = false;

    public function actionEvent()
    {
        // 1. AUTH — HMAC signature tekshirish
        $this->verifyHemisSignature();

        // 2. Parse
        $event = Yii::$app->request->getBodyParams();
        $eventId   = $event['event_id'];
        $eventType = $event['event_type'];

        // 3. IDEMPOTENCY — duplicate event skip
        if ($this->isDuplicate($eventId)) {
            return ['status' => 'duplicate', 'event_id' => $eventId];
        }

        // 4. Log + queue
        Yii::$app->db->createCommand()->insert('hemis_callback_log', [
            'event_id'    => $eventId,
            'event_type'  => $eventType,
            'payload'     => json_encode($event['data']),
            'received_at' => time(),
            'status'      => 'queued',
        ])->execute();

        // 5. Async worker (yii2-queue Redis backend allaqachon bor)
        Yii::$app->queue->push(new \app\jobs\ApplyHemisEventJob([
            'eventId'   => $eventId,
            'eventType' => $eventType,
            'data'      => $event['data'],
        ]));

        // 6. Tez javob (markaz 30s timeout)
        return ['status' => 'accepted', 'event_id' => $eventId];
    }

    private function verifyHemisSignature()
    {
        $signature = Yii::$app->request->headers->get('X-Hemis-Signature');
        $timestamp = Yii::$app->request->headers->get('X-Hemis-Timestamp');
        $body      = Yii::$app->request->getRawBody();

        // Replay attack — 5 daqiqa
        if (!$timestamp || abs(time() - intval($timestamp)) > 300) {
            throw new ForbiddenHttpException('Stale or missing timestamp');
        }

        $secret   = Yii::$app->params['hemisWebhookSecret']; // .env'dan
        $expected = hash_hmac('sha256', "$timestamp.$body", $secret);

        if (!hash_equals($expected, $signature)) {
            throw new ForbiddenHttpException('Invalid signature');
        }
    }

    private function isDuplicate($eventId)
    {
        return (new \yii\db\Query())
            ->from('hemis_callback_log')
            ->where(['event_id' => $eventId])
            ->exists();
    }
}
```

### 3.3 Worker — async event apply

```php
// univer/jobs/ApplyHemisEventJob.php
namespace app\jobs;

use Yii;
use yii\base\BaseObject;
use yii\queue\JobInterface;

class ApplyHemisEventJob extends BaseObject implements JobInterface
{
    public $eventId, $eventType, $data;

    public function execute($queue)
    {
        try {
            switch ($this->eventType) {
                case 'classifier.updated':
                    $this->applyClassifier();
                    break;
                case 'rule.push':
                    $this->applyRule();
                    break;
                case 'otm.blocked':
                    $this->applyOtmBlock();
                    break;
                case 'webhook.test':
                    Yii::info("Sandbox test received: {$this->eventId}");
                    break;
                default:
                    Yii::warning("Unknown event type: {$this->eventType}");
            }

            Yii::$app->db->createCommand()->update('hemis_callback_log',
                ['status' => 'applied', 'applied_at' => time()],
                ['event_id' => $this->eventId]
            )->execute();
        } catch (\Exception $e) {
            Yii::$app->db->createCommand()->update('hemis_callback_log',
                ['status' => 'failed', 'error_message' => $e->getMessage()],
                ['event_id' => $this->eventId]
            )->execute();
            throw $e;  // yii2-queue retry mexanizmi
        }
    }

    private function applyClassifier()
    {
        $type = $this->data['classifier_type']; // h_region, h_gender, ...

        // Univer DB classifier jadvallari `h_*` prefiks bilan (h_gender, h_education_type, ...).
        // Markaz `hemishe_h_*` ishlatadi (CUBA legacy) — lekin Univer'da bu prefix YO'Q.
        // Markaz `classifier_type` apiKey'ni `h_*` formatda yuboradi (ADR-0006), shuning uchun:
        //   apiKey="h_education_type" → table="h_education_type"
        //   apiKey="education_type"   → table="h_education_type" (defensive — agar h_ unutilsa)
        $table = strpos($type, 'h_') === 0 ? $type : 'h_' . $type;

        Yii::$app->db->createCommand()->upsert($table, $this->data['item'])->execute();
        Yii::$app->cache->delete("classifier:$type");
    }

    private function applyOtmBlock() { /* ... */ }
    // applyRule() — DEFERRED: Univer'da `system_rule` schema dizayni hozircha yo'q.
    // Markaz rule.push event yuborganda — no-op + warning log (ApplyHemisEventJob:149-157).
}
```

> **Diqqat (2026-05-19 fix):** Actual production kod `ApplyHemisEventJob.php:98-103` shu defensive prefiks logikasini ishlatadi. Avvalgi `hemishe_h_` namunasi xato edi (Univer'da bu jadval yo'q → real fail `relation "hemishe_h_position" does not exist`).

### 3.4 Routing + config

```php
// univer/config/web.php
'controllerMap' => [
    'hemis-callback' => 'app\controllers\HemisCallbackController',
],
'params' => [
    'hemisWebhookSecret' => $_ENV['HEMIS_WEBHOOK_SECRET'] ?? null,
],
```

### 3.5 Firewall (markaz IP whitelist)

```nginx
# nginx config — faqat markaz IP'lari
location /api/hemis-callback {
    allow 10.50.0.0/16;     # Markaz network
    allow 172.16.0.0/12;    # K8s pod network
    deny all;
    proxy_pass http://yii2-app;
}
```

---

## 4. Deploy checklist (OTM IT)

```bash
# 1. Yangi versiyani olish
cd /var/www/univer
git pull origin main   # yoki tar release
composer install --no-dev

# 2. DB migration
./yii migrate

# 3. .env yangilash (markazdan olingan plain secret)
echo "HEMIS_WEBHOOK_SECRET=whsec_XXXXXXXXXXXX" >> .env

# 4. Queue worker restart
sudo systemctl restart univer-queue-worker

# 5. Markaz admin paneldan webhook URL ro'yxatdan o'tkazish:
#    https://hemis.uz/admin/webhooks → "Yangi target" → URL kiriting
#    Markaz plain secret qaytaradi → .env'ga yozing → restart

# 6. Markaz sandbox test:
#    Admin panelda "Test webhook" tugmasini bosing → log'da event ko'rinishi kerak
```

---

## 5. Markazda admin uchun REST API

| URL | Maqsad |
|-----|--------|
| `POST /api/v1/web/admin/webhooks` | Yangi OTM target qo'shish (plain secret 1 marta qaytariladi) |
| `GET /api/v1/web/admin/webhooks` | Barcha target ro'yxati |
| `POST /api/v1/web/admin/webhooks/{id}/regenerate-secret` | Secret rotation |
| `POST /api/v1/web/admin/webhooks/{id}/test` | **Sandbox test event** |
| `GET /api/v1/web/admin/webhooks/{id}/deliveries` | Delivery tarix |
| `GET /api/v1/web/admin/webhooks/dlq` | Failed event'lar |

---

## 6. Event tiplari ro'yxati

| event_type | aggregate_type | data | Univer nima qiladi |
|------------|----------------|------|---------------------|
| `classifier.updated` | classifier | `{type, action, item}` | `h_*` jadval upsert (Univer-side, `hemishe_*` prefiksi YO'Q) |
| `rule.push` | rule | `{rule_key, value, effective_until}` | `hemishe_r_rule` jadval upsert |
| `otm.blocked` | university | `{block_reason}` | Univer login bekitish |
| `webhook.test` | webhook | `{message}` | Faqat log (sandbox) |

---

## 7. FAQ

| Savol | Javob |
|-------|-------|
| **Yangi tech stack?** | ❌ Yo'q — oddiy Yii2 controller |
| **30 sekund ichida 200 OK kerakmi?** | ✅ Ha — queue'ga qo'yib darhol javob |
| **Univer DB o'lik bo'lsa?** | Markaz 5 marta retry (1s, 5s, 30s, 5min, 1h) → DLQ |
| **Bir xil event 2 marta keladimi?** | Ha, "at-least-once" — `event_id` orqali idempotency |
| **Local test qanday?** | Markaz sandbox: `POST /api/v1/web/admin/webhooks/{id}/test` |
| **Secret qanday olamiz?** | Markaz admin panel → "Create webhook" → response'da plain secret |
| **Secret yo'qotsak?** | `regenerate-secret` endpoint — yangi qiymat |

---

## See Also

- [`docs/architecture/hemis-univer-integration-patterns.html`](../architecture/hemis-univer-integration-patterns.html)
- [`docs/adr/0012-webhook-outbound-infrastructure.md`](../adr/0012-webhook-outbound-infrastructure.md)
- ADR-0007 — Sync architecture evolution
- ADR-0010 — Employee sync outbox implementation
