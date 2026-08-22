-- =====================================================
-- S029: SEED TRANSLATIONS — OTM API-client maxfiy kalit rotatsiyasi
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-21
-- Purpose:
--   Eight NEW i18n keys for the OAuth client secret rotation UI
--   (/system/oauth-clients -> "Maxfiy kalitni almashtirish" action).
--
--   Why the feature exists: oauth_client had create / toggle-status / soft-delete but NO way to
--   change a secret. Combined with the (now fixed, M012) full UNIQUE on client_id over
--   soft-deleted rows, the only workaround was to delete the client and recreate it under a
--   DIFFERENT client_id — which forces the OTM to change its own configuration. Production shows
--   the scar: a client renamed to "jidu(yopilgan)" purely to free the name.
--
--   Manual entry is guarded by an entropy-based strength meter (secretStrength.ts) plus a confirm
--   field: a mistyped machine secret breaks an OTM's integration silently until someone notices.
--   The strength rules are entropy-first, NOT composition rules ("one uppercase, one digit") —
--   NIST SP 800-63B: composition rules push people toward predictable patterns like Parol123!.
--
--   Two of these keys carry real operational meaning and must NOT be shortened by translators:
--     * 'New secret — copy now'  — the plaintext is returned ONCE, never recoverable.
--     * 'Already-issued tokens...' — NEITHER rotation NOR disabling revokes live JWTs. is_active is
--       checked only when a token is ISSUED; the JWT filter never re-reads oauth_client. Machine TTL
--       is hemis.security.oauth.client-token-expiration (default 86400s = 24h), NOT the per-client
--       access_token_ttl_seconds column, which token issuance ignores.
--
--   NEW seed because S006/S010/S024/S026/S027/S028 are already applied in production
--   (central_hemis) — applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021/S022/S024/S026/S027/S028 (_seed_msg helper, persistent — defined in S006).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category  key(en)                     uz                        oz                          ru
PERFORM _seed_msg('label', 'Rotate secret',           'Maxfiy kalitni almashtirish',     'Махфий калитни алмаштириш',        'Сменить секрет');
PERFORM _seed_msg('label', 'Generate automatically',  'Avtomatik generatsiya',  'Автоматик генерация',     'Сгенерировать автоматически');
PERFORM _seed_msg('label', 'Enter manually',          'Qo''lda kiritish',       'Қўлда киритиш',           'Ввести вручную');
PERFORM _seed_msg('label', 'New secret',              'Yangi maxfiy kalit',              'Янги махфий калит',                'Новый секрет');
PERFORM _seed_msg('label', 'At least {{n}} characters', 'Kamida {{n}} belgi',    'Камида {{n}} белги',      'Не менее {{n}} символов');
PERFORM _seed_msg('label', 'Repeat new secret',       'Yangi maxfiy kalitni takrorlang', 'Янги махфий калитни такрорланг',   'Повторите новый секрет');
PERFORM _seed_msg('label', 'Secrets do not match',    'Maxfiy kalitlar mos kelmadi',     'Махфий калитлар мос келмади',      'Секреты не совпадают');
PERFORM _seed_msg('label', 'Weak',                    'Zaif',                   'Заиф',                    'Слабый');
PERFORM _seed_msg('label', 'Fair',                    'O''rtacha',              'Ўртача',                  'Средний');
PERFORM _seed_msg('label', 'Strong',                  'Kuchli',                 'Кучли',                   'Надёжный');
PERFORM _seed_msg('label', 'Estimated entropy: {{bits}} bits', 'Taxminiy entropiya: {{bits}} bit', 'Тахминий энтропия: {{bits}} бит', 'Примерная энтропия: {{bits}} бит');
PERFORM _seed_msg('label', 'Must not contain the Client ID', 'Client ID ni o''z ichiga olmasligi kerak', 'Client ID ни ўз ичига олмаслиги керак', 'Не должен содержать Client ID');
PERFORM _seed_msg('label', 'Too many repeated characters', 'Takrorlanuvchi belgilar juda ko''p', 'Такрорланувчи белгилар жуда кўп', 'Слишком много повторяющихся символов');
PERFORM _seed_msg('label', 'Avoid sequences like abcd or 1234', 'abcd yoki 1234 kabi ketma-ketlikdan saqlaning', 'abcd ёки 1234 каби кетма-кетликдан сақланинг', 'Избегайте последовательностей вида abcd или 1234');
PERFORM _seed_msg('label', 'Avoid guessable words like admin, password or test', 'admin, parol, test kabi taxmin qilinadigan so''zlardan saqlaning', 'admin, парол, test каби тахмин қилинадиган сўзлардан сақланинг', 'Избегайте угадываемых слов вроде admin, password или test');
PERFORM _seed_msg('label', 'Not complex enough — make it longer or more random', 'Yetarlicha murakkab emas — uzunroq yoki tasodifiyroq qiling', 'Етарлича мураккаб эмас — узунроқ ёки тасодифийроқ қилинг', 'Недостаточно сложный — сделайте длиннее или случайнее');
PERFORM _seed_msg('label', 'Mix letters, digits and symbols', 'Harf, raqam va belgilarni aralashtiring', 'Ҳарф, рақам ва белгиларни аралаштиринг', 'Смешивайте буквы, цифры и символы');
PERFORM _seed_msg('label', 'New secret — copy now',   'Yangi maxfiy kalit — hoziroq nusxalang', 'Янги махфий калит — ҳозироқ нусхаланг', 'Новый секрет — скопируйте сейчас');
PERFORM _seed_msg('label', 'Secret changed',          'Maxfiy kalit almashtirildi',      'Махфий калит алмаштирилди',        'Секрет изменён');

PERFORM _seed_msg('label',
    'Already-issued tokens stay valid for up to 24 hours. Rotating or disabling only blocks new tokens.',
    'Allaqachon berilgan tokenlar 24 soatgacha amal qiladi. Maxfiy kalitni almashtirish ham, hisobni o''chirish ham faqat yangi tokenlarni to''xtatadi.',
    'Аллақачон берилган токенлар 24 соатгача амал қилади. Махфий калитни алмаштириш ҳам, ҳисобни ўчириш ҳам фақат янги токенларни тўхтатади.',
    'Уже выданные токены действуют до 24 часов. Смена секрета и отключение аккаунта блокируют только новые токены.');

END $$;
