-- =====================================================
-- Rollback S030: "maxfiy kalit" -> "sir" (uz/oz atamasini qaytarish)
-- =====================================================
-- S030 kalitlarni O'CHIRMAYDI — ular S010/S029 ga tegishli va tizim ularsiz buziladi.
-- Shuning uchun rollback ham DELETE emas: S010 dagi ASLIY o'zbekcha matnlarni qayta yozadi.
-- Ruscha/inglizcha qiymatlar hech qachon o'zgarmagan, shu holicha qoladi.
-- =====================================================

DO $$
BEGIN

PERFORM _seed_msg('message', 'Webhook target created — save the plain secret!',
    'Webhook manzili yaratildi — ochiq sirni saqlang!',
    'Webhook манзили яратилди — очиқ сирни сақланг!',
    'Цель webhook создана — сохраните открытый секрет!');
PERFORM _seed_msg('message', 'Manage 224 OTM Univer webhook URLs, secrets and delivery logs',
    '224 OTM Univer webhook URL''lari, sirlari va yetkazish jurnallarini boshqaring',
    '224 ОТМ Univer webhook URL''лари, сирлари ва етказиш журналларини бошқаринг',
    'Управление webhook-URL, секретами и журналами доставки 224 вузов Univer');
PERFORM _seed_msg('message', 'Old secret will be invalidated immediately. New secret must be deployed to Univer .env before next event.',
    'Eski sir darhol bekor qilinadi. Yangi sir keyingi hodisadan oldin Univer .env''ga joylanishi kerak.',
    'Эски сир дарҳол бекор қилинади. Янги сир кейинги ҳодисадан олдин Univer .env''га жойланиши керак.',
    'Старый секрет будет аннулирован немедленно. Новый секрет нужно развернуть в Univer .env до следующего события.');
PERFORM _seed_msg('message', 'OTM {{code}} — this plain secret is shown only once. Save it to Univer .env as HEMIS_WEBHOOK_SECRET.',
    'OTM {{code}} — bu ochiq sir faqat bir marta ko''rsatiladi. Uni Univer .env''ga HEMIS_WEBHOOK_SECRET sifatida saqlang.',
    'ОТМ {{code}} — бу очиқ сир фақат бир марта кўрсатилади. Уни Univer .env''га HEMIS_WEBHOOK_SECRET сифатида сақланг.',
    'Вуз {{code}} — этот открытый секрет показывается только один раз. Сохраните его в Univer .env как HEMIS_WEBHOOK_SECRET.');
PERFORM _seed_msg('action', 'Regenerate secret',
    'Sirni qayta yaratish',
    'Сирни қайта яратиш',
    'Перевыпустить секрет');
PERFORM _seed_msg('message', 'Secret regenerated — update Univer .env immediately',
    'Sir qayta yaratildi — Univer .env''ni darhol yangilang',
    'Сир қайта яратилди — Univer .env''ни дарҳол янгиланг',
    'Секрет перевыпущен — немедленно обновите Univer .env');
PERFORM _seed_msg('label', 'Webhook secret — copy now',
    'Webhook siri — hozir nusxalang',
    'Webhook сири — ҳозир нусхаланг',
    'Секрет webhook — скопируйте сейчас');
PERFORM _seed_msg('confirm', 'Regenerate webhook secret?',
    'Webhook siri qayta yaratilsinmi?',
    'Webhook сири қайта яратилсинми?',
    'Перевыпустить секрет webhook?');
PERFORM _seed_msg('error', 'Failed to regenerate secret',
    'Sirni qayta yaratib bo''lmadi',
    'Сирни қайта яратиб бўлмади',
    'Не удалось перевыпустить секрет');
PERFORM _seed_msg('label', 'Client secret',
    'Maxfiy kalit (parol)',
    'Махфий калит (парол)',
    'Секретный ключ (пароль)');

END $$;
