# JWT Secret Rotation — Runbook

> **Maqsad:** `JWT_SECRET` ENV ni xavfsiz aylantirish (90-day rotation policy yoki incident response).
>
> **Ma'lumotnoma:** [`security/CLAUDE.md`](../../security/CLAUDE.md) "Secrets Management" + [ADR-0009](../adr/0009-jwt-ttl-and-refresh-rotation.md).
>
> **Hozirgi vaziyat (2026-05):** kod `kid` (key ID) header yozadi (`TokenService.java:84-85`, `SecurityConfig.java:353`), lekin **bitta secret saqlaydi** (`ImmutableJWKSet` single key). Demak rotation = barcha live token'lar bekor (mass-logout). Multi-key (overlap window) — kelajakdagi sprint (ADR-0009 DEFERRED).

---

## 0. Qachon rotation kerak

| Trigger | Yondashuv |
|---------|-----------|
| 90-day calendar policy | Plan rotation (bo'sh trafik vaqti, 02:00-04:00) |
| Suspected leak (k8s-secret.env diskdan exfiltrate) | **Darhol** rotation (downtime acceptable) |
| Developer departure (laptop sync) | Darhol rotation |
| Sentry'da `Weak legacy OAuth credentials detected` ko'rinsa | OAUTH credentials alohida (bu runbook'ga aloqasi yo'q) |

---

## 1. Pre-flight checklist

```bash
# 1. Hozirgi secret config tasdiqlash
kubectl get secret <existingSecret-name> -o jsonpath='{.data.JWT_SECRET}' | base64 -d | wc -c
# Kutilgan: ≥ 32 bayt (HS256 minimal 256-bit kalit)

# 2. Live token sonini bilish (Sentry yoki application metric)
# - Hozirgi active session: foydalanuvchi rotation oqibati ko'radi
# - Refresh token TTL: 7 days (ADR-0009) — eski refresh ham bekor

# 3. Kommunikatsiya
# - Admin'larga 1 soat oldindan eslatma: "Profil sahifasidan qayta login qiling"
# - Univer OAuth client'lar — TA'SIR YO'Q (alohida OAUTH_CLIENT_SECRET, bu runbook'da emas)
```

---

## 2. Yangi secret generate qilish

```bash
# Cryptographically secure, 64 bayt base64 (512-bit kalit)
NEW_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "$NEW_SECRET" | wc -c
# Kutilgan: ~88 belgi (64 baytdan base64'lash = 88 chars)

# Maxfiy lokal saqlash (deploy oxirida o'chiriladi)
echo "$NEW_SECRET" > /tmp/new-jwt-secret.txt && chmod 600 /tmp/new-jwt-secret.txt
```

**TAQIQ:**
- ❌ `openssl rand -hex 32` (faqat hex characters — entropy past)
- ❌ Plaintext '/tmp' yoki '~/Downloads' da uzoq vaqt saqlash
- ❌ Slack/email/Telegram orqali secret yuborish

---

## 3. K8s Secret yangilash

### Variant A — Manual kubectl

```bash
# Faqat JWT_SECRET'ni yangilaymiz, qolgan secret'lar tegmaydi
kubectl create secret generic <existingSecret-name> \
    --from-literal=JWT_SECRET="$NEW_SECRET" \
    --dry-run=client -o yaml | \
kubectl patch secret <existingSecret-name> --patch-file=/dev/stdin

# Yoki single-value update:
kubectl patch secret <existingSecret-name> --type='json' \
    -p='[{"op":"replace","path":"/data/JWT_SECRET","value":"'$(echo -n "$NEW_SECRET" | base64 -w0)'"}]'
```

### Variant B — Sealed-Secrets (agar integration qilingan bo'lsa)

```bash
# Yangi SealedSecret manifest yarating, eski'ni almashtiring
echo -n "$NEW_SECRET" | kubeseal --raw --namespace=hemis --name=hemis-back-env \
    --from-file=/dev/stdin > sealed-jwt-secret.txt
# Helm chart values'da JWT_SECRET'ni shu encrypted blob bilan almashtiring
helm upgrade hemis-back ./helm/hemis-back --reuse-values \
    --set "sealedSecrets.JWT_SECRET=$(cat sealed-jwt-secret.txt)"
```

> **Diqqat:** Sealed-Secrets hozir integration qilinmagan (audit P1-C, deploy-level qoldiq). Variant A standart.

---

## 4. Pod restart (graceful)

```bash
# RollingUpdate strategiya (helm/.../values.yaml: maxSurge: 1, maxUnavailable: 0)
kubectl rollout restart deployment/hemis-back -n hemis

# Status kuzatish (yangi podlar Ready bo'lguncha)
kubectl rollout status deployment/hemis-back -n hemis --timeout=180s

# Verify: yangi pod log'ida secret yuklandi degan eslatma yo'q (faqat health log)
kubectl logs -l app=hemis-back --tail=20 -n hemis | grep -iE "JWT|secret|key"
```

`server.shutdown: graceful` + `lifecycle.timeout-per-shutdown-phase: 30s` — in-flight request'lar yakunlanadi (`application.yml`).

---

## 5. Validatsiya — yangi secret ishlamoqda

```bash
# 1. Yangi login → yangi token
curl -X POST https://hemis.uz/api/v1/web/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test-admin","password":"<password>"}' \
    -c /tmp/cookies.txt -s | jq

# 2. Token bilan protected endpoint
curl https://hemis.uz/api/v1/web/me \
    -b /tmp/cookies.txt -s | jq
# Kutilgan: 200 + user info

# 3. JWT decode (lokal) — yangi kid bo'lishi mumkin (agar keyId ham o'zgargan bo'lsa)
cat /tmp/cookies.txt | grep accessToken | awk '{print $7}' | \
    cut -d. -f1 | base64 -d 2>/dev/null | jq
# Kutilgan: {"alg":"HS256","kid":"hemis-jwt-key","typ":"JWT"}
```

---

## 6. Eski active session'lar — qabul qilinadi yoki bekor

**Hozirgi vaziyat (single-key)**: eski token'lar **darhol bekor** bo'ladi. Foydalanuvchi har sahifa o'tishida 401 oladi → re-login.

**Yumshatish (admin pre-warn)**:
- 1 soat oldin admin Slack/email: "JWT rotation 03:00 da. Qayta login qiling, ish yo'qotmaslik uchun draft saqlang"
- Mass-logout endpoint kerak emas — Redis blacklist faqat **explicit logout**ga qarshi (rotation'da ishlatilmaydi)

**Kelajak (ADR-0009 multi-key — DEFERRED)**:
- `JWKSource` `JWKSet([active, previous])` bilan ishlasin
- Eski token 7 kun overlap window davomida ishlaydi (kid orqali eski key tanlanadi)
- Yangi token faqat active key bilan sign qilinadi
- Cleanup: 7 kun keyin `previous` olib tashlanadi

---

## 7. Post-rotation — secret tozalash

```bash
# Lokal disk'dan eski secret artefakt'larini o'chir
shred -u /tmp/new-jwt-secret.txt 2>/dev/null || rm -f /tmp/new-jwt-secret.txt

# Bash history'dan secret literal'larni olib tashlash
history -d $(history | grep -n 'JWT_SECRET=' | tail -1 | cut -d: -f1) 2>/dev/null

# Eslatma: k8s-secret.env developer mashinasida hali eski secret bo'ladi.
# Sync qilish:  kubectl get secret <name> -o yaml > k8s-secret.env  (yoki SOPS)
```

---

## 8. Incident response — leaked secret bo'lsa

Agar JWT_SECRET tashqi joyda paydo bo'lsa (Git public push, Slack leak, Sentry breadcrumb):

1. **Darhol** Variant A bilan rotate qilish (3-5 daqiqa)
2. Sentry'ga incident yozish (manual `Sentry.captureMessage`)
3. Auditda **`activity_log` tahlili**: rotation vaqtidan oldin yarim soat ichida shubhali login (boshqa IP, no user-agent change) — manual review
4. `TokenBlacklistService.clearAllBlacklist()` — kerak emas (mass-logout chunki secret rotation o'zi shuni qiladi)
5. Post-mortem: leak qanday bo'lganini aniqlash, oldini olish (gitleaks rules, slack DLP)

---

## 9. Rotation tarixini saqlash

```bash
# Audit DB'da (hemis_audit.rotation_log — agar yaratilgan bo'lsa)
# Yoki oddiy fayl:
cat >> /opt/hemis/runtime/rotation-log.csv <<EOF
$(date -Iseconds),JWT_SECRET,rotated,<operator-username>
EOF
chmod 600 /opt/hemis/runtime/rotation-log.csv
```

90-day kalendar: oxirgi rotation `+ 90 days` keyingisi.

---

## Ma'lumotnoma fayllar

- `security/src/main/java/uz/hemis/security/service/TokenService.java:76-86` — `kid` header (rotation primitive)
- `security/src/main/java/uz/hemis/security/config/SecurityConfig.java:329-359` — `jwtEncoder()` JWK construction (hozir single-key)
- `app/src/main/resources/application.yml` — `hemis.security.jwt.secret`, `hemis.security.jwt.key-id`
- `k8s-secret.env` — ENV manbai (lokal dev)
- `helm/hemis-back/values.yaml` — `existingSecret: <name>` (prod)
- ADR-0009 — JWT TTL + rotation arxitekturasi
- Audit P1-4/5 — secret hygiene (gitleaks, Sealed-Secrets DEFERRED)
