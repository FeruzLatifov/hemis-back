-- =====================================================
-- S030: SEED TRANSLATIONS — "sir" -> "maxfiy kalit" (uz/oz atama birligi)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-21
-- Purpose:
--   OTM integratsiya hisobining kredensiali uchun o'zbekcha atamani birxillashtirish.
--
--   NEGA o'zgardi: `client_secret` so'zma-so'z "sir" deb tarjima qilingan edi. Bu OAuth2
--   spetsifikatsiyasiga (RFC 6749) mos, LEKIN o'zbekchada "sir" kundalik ma'noda "yashirin gap"
--   degani — "Sirni almashtirish" g'aliz eshitiladi. "Maxfiy kalit" tabiiy o'zbekcha, UI'dagi
--   kalit belgisiga mos va odam paroli bilan chalkashmaydi (bu mashina kredensiali: bir marta
--   OTM `.env` fayliga yoziladi, hech kim uni eslab qolmaydi).
--   "Parol" ataylab TANLANMADI — u adminni eslab qoladigan qiymat tanlashga undaydi.
--
--   NEGA YANGI seed: bu kalitlar S010 da (webhook UI) va u PRODDA ALLAQACHON QO'LLANGAN —
--   qo'llangan changeset hech qachon tahrirlanmaydi. `_seed_msg` ichida
--   ON CONFLICT (message_key) DO UPDATE bor, shuning uchun yangi seed mavjud kalitlarni
--   xavfsiz qayta yozadi. S029 (oauth rotatsiyasi) o'z kalitlarini o'zi tashiydi.
--
--   Faqat uz/oz o'zgaradi: ruscha "секрет" va inglizcha "secret" o'z tillarida to'g'ri.
-- Pattern: S021/S022/S024/S026/S027/S028/S029 (_seed_msg helper, S006 da aniqlangan).
-- Safety: ON CONFLICT (message_key) UPDATE — idempotent.
-- =====================================================

DO $$
BEGIN

PERFORM _seed_msg('message', 'Webhook target created — save the plain secret!',
    'Webhook manzili yaratildi — ochiq maxfiy kalitni saqlang!',
    'Webhook манзили яратилди — очиқ махфий калитни сақланг!',
    'Цель webhook создана — сохраните открытый секрет!');
PERFORM _seed_msg('message', 'Manage 224 OTM Univer webhook URLs, secrets and delivery logs',
    '224 OTM Univer webhook URL''lari, maxfiy kalitlari va yetkazish jurnallarini boshqaring',
    '224 ОТМ Univer webhook URL''лари, махфий калитлари ва етказиш журналларини бошқаринг',
    'Управление webhook-URL, секретами и журналами доставки 224 вузов Univer');
PERFORM _seed_msg('message', 'Old secret will be invalidated immediately. New secret must be deployed to Univer .env before next event.',
    'Eski maxfiy kalit darhol bekor qilinadi. Yangi maxfiy kalit keyingi hodisadan oldin Univer .env''ga joylanishi kerak.',
    'Эски махфий калит дарҳол бекор қилинади. Янги махфий калит кейинги ҳодисадан олдин Univer .env''га жойланиши керак.',
    'Старый секрет будет аннулирован немедленно. Новый секрет нужно развернуть в Univer .env до следующего события.');
PERFORM _seed_msg('message', 'OTM {{code}} — this plain secret is shown only once. Save it to Univer .env as HEMIS_WEBHOOK_SECRET.',
    'OTM {{code}} — bu ochiq maxfiy kalit faqat bir marta ko''rsatiladi. Uni Univer .env''ga HEMIS_WEBHOOK_SECRET sifatida saqlang.',
    'ОТМ {{code}} — бу очиқ махфий калит фақат бир марта кўрсатилади. Уни Univer .env''га HEMIS_WEBHOOK_SECRET сифатида сақланг.',
    'Вуз {{code}} — этот открытый секрет показывается только один раз. Сохраните его в Univer .env как HEMIS_WEBHOOK_SECRET.');
PERFORM _seed_msg('action', 'Regenerate secret',
    'Maxfiy kalitni qayta yaratish',
    'Махфий калитни қайта яратиш',
    'Перевыпустить секрет');
PERFORM _seed_msg('message', 'Secret regenerated — update Univer .env immediately',
    'Maxfiy kalit qayta yaratildi — Univer .env''ni darhol yangilang',
    'Махфий калит қайта яратилди — Univer .env''ни дарҳол янгиланг',
    'Секрет перевыпущен — немедленно обновите Univer .env');
PERFORM _seed_msg('label', 'Webhook secret — copy now',
    'Webhook maxfiy kaliti — hozir nusxalang',
    'Webhook махфий калити — ҳозир нусхаланг',
    'Секрет webhook — скопируйте сейчас');
PERFORM _seed_msg('confirm', 'Regenerate webhook secret?',
    'Webhook maxfiy kaliti qayta yaratilsinmi?',
    'Webhook махфий калити қайта яратилсинми?',
    'Перевыпустить секрет webhook?');
PERFORM _seed_msg('error', 'Failed to regenerate secret',
    'Maxfiy kalitni qayta yaratib bo''lmadi',
    'Махфий калитни қайта яратиб бўлмади',
    'Не удалось перевыпустить секрет');
PERFORM _seed_msg('label', 'Client secret',
    'Maxfiy kalit',
    'Махфий калит',
    'Секретный ключ (пароль)');
END $$;
