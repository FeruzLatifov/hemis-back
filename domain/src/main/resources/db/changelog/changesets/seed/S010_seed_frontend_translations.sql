-- =====================================================
-- S010: SEED TRANSLATIONS — Frontend missing keys
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-05
-- Purpose: Translations for keys used in hemis-front but not yet seeded.
--   Source: audit of t() calls vs en.json (~260 keys × 4 languages).
--   Covers: Auth pages, Reports page, Translations page, Logs/Audit,
--           CommandPalette, Roles/Users/Permissions, Network/Errors,
--           Toast notifications, A11y labels, Validation messages.
-- Pattern: S006_seed_translations.sql (_seed_msg helper)
-- Safety: ON CONFLICT UPDATE — idempotent, no duplicates.
-- =====================================================

-- Helper _seed_msg() is defined in S006_seed_translations.sql (persistent, not dropped).

DO $$
BEGIN

-- ──────────────────────────────────────────────────────
-- ACTIONS (buttons, controls, links)
-- ──────────────────────────────────────────────────────
--                   category   key(en)                                  uz                                  oz                                  ru
PERFORM _seed_msg('action', 'Add to favorites',                       'Sevimlilarga qo''shish',          'Севимлиларга қўшиш',              'Добавить в избранное');
PERFORM _seed_msg('action', 'Remove from favorites',                  'Sevimlilardan olib tashlash',     'Севимлилардан олиб ташлаш',       'Удалить из избранного');
PERFORM _seed_msg('action', 'Back to login',                          'Loginga qaytish',                 'Логинга қайтиш',                  'Вернуться к входу');
PERFORM _seed_msg('action', 'Back to roles',                          'Rollarga qaytish',                'Ролларга қайтиш',                 'Вернуться к ролям');
PERFORM _seed_msg('action', 'Back to users',                          'Foydalanuvchilarga qaytish',      'Фойдаланувчиларга қайтиш',        'Вернуться к пользователям');
PERFORM _seed_msg('action', 'Close menu',                             'Menyuni yopish',                  'Менюни ёпиш',                     'Закрыть меню');
PERFORM _seed_msg('action', 'Open menu',                              'Menyuni ochish',                  'Менюни очиш',                     'Открыть меню');
PERFORM _seed_msg('action', 'Toggle theme',                           'Mavzuni almashtirish',            'Мавзуни алмаштириш',              'Переключить тему');
PERFORM _seed_msg('action', 'Show password',                          'Parolni ko''rsatish',             'Паролни кўрсатиш',                'Показать пароль');
PERFORM _seed_msg('action', 'Hide password',                          'Parolni yashirish',               'Паролни яшириш',                  'Скрыть пароль');
PERFORM _seed_msg('action', 'Show all',                               'Hammasini ko''rsatish',           'Ҳаммасини кўрсатиш',              'Показать все');
PERFORM _seed_msg('action', 'Show',                                   'Ko''rsatish',                     'Кўрсатиш',                        'Показать');
PERFORM _seed_msg('action', 'Try again',                              'Qaytadan urinish',                'Қайтадан уриниш',                 'Попробовать снова');
PERFORM _seed_msg('action', 'Go back',                                'Orqaga qaytish',                  'Орқага қайтиш',                   'Назад');
PERFORM _seed_msg('action', 'Go to Dashboard',                        'Boshqaruv paneliga o''tish',      'Бошқарув панелига ўтиш',          'Перейти к панели');
PERFORM _seed_msg('action', 'Send reset link',                        'Tiklash havolasini yuborish',     'Тиклаш ҳаволасини юбориш',        'Отправить ссылку');
PERFORM _seed_msg('action', 'Reset password',                         'Parolni tiklash',                 'Паролни тиклаш',                  'Сбросить пароль');
PERFORM _seed_msg('action', 'Set new password',                       'Yangi parol o''rnatish',          'Янги парол ўрнатиш',              'Установить новый пароль');
PERFORM _seed_msg('action', 'Resend email',                           'Xatni qayta yuborish',            'Хатни қайта юбориш',              'Отправить письмо повторно');
PERFORM _seed_msg('action', 'Request a new reset link',               'Yangi tiklash havolasini so''rash','Янги тиклаш ҳаволасини сўраш',    'Запросить новую ссылку');
PERFORM _seed_msg('action', 'Select language',                        'Tilni tanlang',                   'Тилни танланг',                   'Выберите язык');
PERFORM _seed_msg('action', 'Generate properties',                    'Properties yaratish',             'Properties яратиш',               'Сгенерировать properties');
PERFORM _seed_msg('action', 'Regenerate properties files',            'Properties fayllarini qayta yaratish','Properties файлларини қайта яратиш','Перегенерировать properties файлы');
PERFORM _seed_msg('action', 'Manage translations',                    'Tarjimalarni boshqarish',         'Таржималарни бошқариш',           'Управление переводами');
PERFORM _seed_msg('action', 'Create role',                            'Rol yaratish',                    'Рол яратиш',                      'Создать роль');
PERFORM _seed_msg('action', 'Edit role',                              'Rolni tahrirlash',                'Ролни таҳрирлаш',                 'Редактировать роль');
PERFORM _seed_msg('action', 'Delete role',                            'Rolni o''chirish',                'Ролни ўчириш',                    'Удалить роль');
PERFORM _seed_msg('action', 'Remove filter',                          'Filtrni olib tashlash',           'Филтрни олиб ташлаш',             'Сбросить фильтр');
PERFORM _seed_msg('action', 'Yes, clear',                             'Ha, tozalash',                    'Ҳа, тозалаш',                     'Да, очистить');

-- Network / connectivity toasts (api client interceptor — no longer silent on outage)
PERFORM _seed_msg('error',   'Request timed out',                     'So''rov vaqti tugadi',            'Сўров вақти тугади',              'Время запроса истекло');
PERFORM _seed_msg('error',   'Server is temporarily unavailable',     'Server vaqtincha ishlamayapti',   'Сервер вақтинча ишламаяпти',      'Сервер временно недоступен');
PERFORM _seed_msg('message', 'Please check your connection and try again', 'Ulanishni tekshiring va qayta urinib ko''ring', 'Уланишни текширинг ва қайта уриниб кўринг', 'Проверьте соединение и повторите попытку');
PERFORM _seed_msg('message', 'Please try again in a moment',          'Bir ozdan so''ng qayta urinib ko''ring', 'Бир оздан сўнг қайта уриниб кўринг', 'Повторите попытку через мгновение');
-- Pagination / navigation accessibility labels
PERFORM _seed_msg('action',  'Navigate',                              'Harakatlanish',                   'Ҳаракатланиш',                    'Навигация');
PERFORM _seed_msg('action',  'Previous page',                         'Oldingi sahifa',                  'Олдинги саҳифа',                  'Предыдущая страница');
PERFORM _seed_msg('action',  'Next page',                             'Keyingi sahifa',                  'Кейинги саҳифа',                  'Следующая страница');
PERFORM _seed_msg('action',  'Pagination',                            'Sahifalash',                      'Саҳифалаш',                       'Пагинация');
PERFORM _seed_msg('action',  'Toggle submenu',                        'Quyi menyuni ochish/yopish',      'Қуйи менюни очиш/ёпиш',           'Открыть/закрыть подменю');
PERFORM _seed_msg('action',  'Page {{number}}',                       '{{number}}-sahifa',               '{{number}}-саҳифа',               'Страница {{number}}');
PERFORM _seed_msg('action', 'Yes, generate',                          'Ha, yaratish',                    'Ҳа, яратиш',                      'Да, сгенерировать');

-- ──────────────────────────────────────────────────────
-- LABELS (form fields, page titles, section labels)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('label',  'Account lock',                           'Akkauntni qulflash',              'Аккаунтни қулфлаш',               'Блокировка аккаунта');
PERFORM _seed_msg('label',  'Account status',                         'Akkaunt holati',                  'Аккаунт ҳолати',                  'Статус аккаунта');
PERFORM _seed_msg('label',  'Activity',                               'Faoliyat',                        'Фаолият',                         'Активность');
PERFORM _seed_msg('label',  'Activity details',                       'Faoliyat tafsilotlari',           'Фаолият тафсилотлари',            'Детали активности');
PERFORM _seed_msg('label',  'All actions',                            'Barcha amallar',                  'Барча амаллар',                   'Все действия');
PERFORM _seed_msg('label',  'All events',                             'Barcha hodisalar',                'Барча ҳодисалар',                 'Все события');
PERFORM _seed_msg('label',  'All pages',                              'Barcha sahifalar',                'Барча саҳифалар',                 'Все страницы');
PERFORM _seed_msg('label',  'Recent',                                 'So''nggi',                        'Сўнгги',                          'Недавние');
PERFORM _seed_msg('label',  'Results',                                'Natijalar',                       'Натижалар',                       'Результаты');
PERFORM _seed_msg('label',  'Quick search',                           'Tezkor qidiruv',                  'Тезкор қидирув',                  'Быстрый поиск');
PERFORM _seed_msg('label',  'Search pages...',                        'Sahifalarni qidirish...',         'Саҳифаларни қидириш...',          'Поиск страниц...');
PERFORM _seed_msg('label',  'Search pages (Ctrl+K)',                  'Sahifalarni qidirish (Ctrl+K)',   'Саҳифаларни қидириш (Ctrl+K)',    'Поиск страниц (Ctrl+K)');
PERFORM _seed_msg('label',  'Search by code...',                      'Kod bo''yicha qidirish...',       'Код бўйича қидириш...',           'Поиск по коду...');
PERFORM _seed_msg('label',  'Search by PINFL...',                     'JSHSHIR bo''yicha qidirish...',   'ЖШШИР бўйича қидириш...',         'Поиск по ПИНФЛ...');
PERFORM _seed_msg('label',  'Search permissions...',                  'Ruxsatlarni qidirish...',         'Рухсатларни қидириш...',          'Поиск разрешений...');
PERFORM _seed_msg('label',  'Search roles...',                        'Rollarni qidirish...',            'Ролларни қидириш...',             'Поиск ролей...');
PERFORM _seed_msg('label',  'Key or text...',                         'Kalit yoki matn...',              'Калит ёки матн...',               'Ключ или текст...');
PERFORM _seed_msg('label',  'Breadcrumb',                             'Yo''nalish yo''li',               'Йўналиш йўли',                    'Хлебные крошки');
PERFORM _seed_msg('label',  'Sidebar',                                'Yon panel',                       'Ён панель',                       'Боковая панель');
PERFORM _seed_msg('label',  'Main header',                            'Asosiy sarlavha',                 'Асосий сарлавҳа',                 'Главный заголовок');
PERFORM _seed_msg('label',  'Main navigation',                        'Asosiy navigatsiya',              'Асосий навигация',                'Основная навигация');
PERFORM _seed_msg('label',  'User menu',                              'Foydalanuvchi menyusi',           'Фойдаланувчи менюси',             'Меню пользователя');
PERFORM _seed_msg('label',  'Notifications',                          'Bildirishnomalar',                'Билдиришномалар',                 'Уведомления');
PERFORM _seed_msg('label',  'Quick links',                            'Tezkor havolalar',                'Тезкор ҳаволалар',                'Быстрые ссылки');
PERFORM _seed_msg('label',  'Need help?',                             'Yordam kerakmi?',                 'Ёрдам керакми?',                  'Нужна помощь?');
PERFORM _seed_msg('label',  'Skip to main content',                   'Asosiy mazmunga o''tish',         'Асосий мазмунга ўтиш',            'Перейти к содержимому');
PERFORM _seed_msg('label',  'Light',                                  'Yorug''',                         'Ёруғ',                            'Светлая');
PERFORM _seed_msg('label',  'Dark',                                   'Qorong''i',                       'Қоронғи',                         'Тёмная');
PERFORM _seed_msg('label',  'Other languages',                        'Boshqa tillar',                   'Бошқа тиллар',                    'Другие языки');
PERFORM _seed_msg('label',  'Uzbek (latin)',                          'O''zbek (lotin)',                 'Ўзбек (лотин)',                   'Узбекский (латиница)');
PERFORM _seed_msg('label',  'Cyrillic text (oz-UZ)',                  'Kirill matni (oz-UZ)',            'Кирилл матни (oz-UZ)',            'Текст кириллицы (oz-UZ)');
PERFORM _seed_msg('label',  'Russian translation (ru-RU)',            'Ruscha tarjima (ru-RU)',          'Русча таржима (ru-RU)',           'Русский перевод (ru-RU)');
PERFORM _seed_msg('label',  'Primary text (uz-UZ)',                   'Asosiy matn (uz-UZ)',             'Асосий матн (uz-UZ)',             'Основной текст (uz-UZ)');
PERFORM _seed_msg('label',  'Primary language, required',             'Asosiy til, majburiy',            'Асосий тил, мажбурий',            'Основной язык, обязательно');
PERFORM _seed_msg('label',  'Optional, but recommended',              'Ixtiyoriy, lekin tavsiya etiladi','Ихтиёрий, лекин тавсия этилади',  'Необязательно, но рекомендуется');
PERFORM _seed_msg('label',  'Optional description',                   'Ixtiyoriy tavsif',                'Ихтиёрий тавсиф',                 'Необязательное описание');
PERFORM _seed_msg('label',  'For grouping translations (menu, button, label...)',
                                                                       'Tarjimalarni guruhlash uchun (menyu, tugma, yorliq...)',
                                                                       'Таржималарни гуруҳлаш учун (меню, тугма, ёрлиқ...)',
                                                                       'Для группировки переводов (меню, кнопки, метки...)');
PERFORM _seed_msg('label',  'Example: menu, button, label, error, validation',
                                                                       'Misol: menu, button, label, error, validation',
                                                                       'Мисол: menu, button, label, error, validation',
                                                                       'Пример: menu, button, label, error, validation');
PERFORM _seed_msg('label',  'If active, frontend will show the translation',
                                                                       'Faol bo''lsa, frontend tarjimani ko''rsatadi',
                                                                       'Фаол бўлса, frontend таржимани кўрсатади',
                                                                       'Если активен, frontend покажет перевод');
PERFORM _seed_msg('label',  'Used in code, cannot be changed',         'Kodda ishlatiladi, o''zgartirib bo''lmaydi','Кодда ишлатилади, ўзгартириб бўлмайди','Используется в коде, изменить нельзя');
PERFORM _seed_msg('label',  'View and edit translation key-value pairs','Tarjima kalit-qiymat juftlarini ko''rish va tahrirlash','Таржима калит-қиймат жуфтларини кўриш ва таҳрирлаш','Просмотр и редактирование пар ключ-значение');
PERFORM _seed_msg('label',  'Name (English)',                         'Nomi (Inglizcha)',                'Номи (Инглизча)',                 'Название (Английский)');
PERFORM _seed_msg('label',  'Role name',                              'Rol nomi',                        'Рол номи',                        'Название роли');
PERFORM _seed_msg('label',  'Role details',                           'Rol tafsilotlari',                'Рол тафсилотлари',                'Детали роли');
PERFORM _seed_msg('label',  'Permissions',                            'Ruxsatlar',                       'Рухсатлар',                       'Разрешения');
PERFORM _seed_msg('label',  'Current status',                         'Joriy holat',                     'Жорий ҳолат',                     'Текущий статус');
PERFORM _seed_msg('label',  'Changed fields',                         'O''zgartirilgan maydonlar',       'Ўзгартирилган майдонлар',         'Изменённые поля');
PERFORM _seed_msg('label',  'Created',                                'Yaratilgan',                      'Яратилган',                       'Создано');
PERFORM _seed_msg('label',  'Endpoint',                               'Endpoint',                        'Endpoint',                        'Эндпоинт');
PERFORM _seed_msg('label',  'Entity',                                 'Ob''ekt',                         'Объект',                          'Сущность');
PERFORM _seed_msg('label',  'Entity type',                            'Ob''ekt turi',                    'Объект тури',                     'Тип сущности');
PERFORM _seed_msg('label',  'Event',                                  'Hodisa',                          'Ҳодиса',                          'Событие');
PERFORM _seed_msg('label',  'Event type',                             'Hodisa turi',                     'Ҳодиса тури',                     'Тип события');
PERFORM _seed_msg('label',  'Errors',                                 'Xatolar',                         'Хатолар',                         'Ошибки');
PERFORM _seed_msg('label',  'Error type',                             'Xato turi',                       'Хато тури',                       'Тип ошибки');
PERFORM _seed_msg('label',  'Failure reason',                         'Muvaffaqiyatsizlik sababi',       'Муваффақиятсизлик сабаби',        'Причина сбоя');
PERFORM _seed_msg('label',  'Old value',                              'Eski qiymat',                     'Эски қиймат',                     'Старое значение');
PERFORM _seed_msg('label',  'New value',                              'Yangi qiymat',                    'Янги қиймат',                     'Новое значение');
PERFORM _seed_msg('label',  'Total activities',                       'Jami faoliyat',                   'Жами фаолият',                    'Всего активности');
PERFORM _seed_msg('label',  'Total errors',                           'Jami xatolar',                    'Жами хатолар',                    'Всего ошибок');
PERFORM _seed_msg('label',  'Total logins',                           'Jami kirishlar',                  'Жами киришлар',                   'Всего входов');
PERFORM _seed_msg('label',  'Top user',                               'Eng faol foydalanuvchi',          'Энг фаол фойдаланувчи',           'Самый активный пользователь');
PERFORM _seed_msg('label',  'Synced',                                 'Sinxronlandi',                    'Синхронланди',                    'Синхронизировано');
PERFORM _seed_msg('label',  'Group',                                  'Guruh',                           'Гуруҳ',                           'Группа');
PERFORM _seed_msg('label',  'Groups',                                 'Guruhlar',                        'Гуруҳлар',                        'Группы');
PERFORM _seed_msg('label',  'Group count',                            'Guruhlar soni',                   'Гуруҳлар сони',                   'Количество групп');
PERFORM _seed_msg('label',  'Group name',                             'Guruh nomi',                      'Гуруҳ номи',                      'Название группы');
PERFORM _seed_msg('label',  'Group ID',                               'Guruh ID',                        'Гуруҳ ID',                        'ID группы');
PERFORM _seed_msg('label',  'Per page',                               'Sahifada',                        'Саҳифада',                        'На странице');
PERFORM _seed_msg('label',  'Shown',                                  'Ko''rsatilgan',                   'Кўрсатилган',                     'Показано');
PERFORM _seed_msg('label',  'Gender',                                 'Jinsi',                           'Жинси',                           'Пол');
PERFORM _seed_msg('label',  'Education form',                         'Ta''lim shakli',                  'Таълим шакли',                    'Форма обучения');
PERFORM _seed_msg('label',  'Education year',                         'O''quv yili',                     'Ўқув йили',                       'Учебный год');
PERFORM _seed_msg('label',  'Enrollment records',                     'Ro''yxatga olish yozuvlari',      'Рўйхатга олиш ёзувлари',          'Записи о зачислении');
PERFORM _seed_msg('label',  'Living area',                            'Yashash maydoni',                 'Яшаш майдони',                    'Жилая площадь');
PERFORM _seed_msg('label',  'Object area',                            'Obyekt maydoni',                  'Обйект майдони',                  'Площадь объекта');
PERFORM _seed_msg('label',  'Utility area',                           'Yordamchi maydon',                'Ёрдамчи майдон',                  'Подсобная площадь');
PERFORM _seed_msg('label',  'Cadastral cost',                         'Kadastr qiymati',                 'Кадастр қиймати',                 'Кадастровая стоимость');
PERFORM _seed_msg('label',  'Share sum',                              'Ulush summasi',                   'Улуш суммаси',                    'Сумма доли');
PERFORM _seed_msg('label',  'Successor',                              'Vorisi',                          'Вориси',                          'Преемник');
PERFORM _seed_msg('label',  'Duplicate analysis',                     'Dublikat tahlili',                'Дубликат таҳлили',                'Анализ дубликатов');
PERFORM _seed_msg('label',  'Column settings',                        'Ustun sozlamalari',               'Устун созламалари',               'Настройки колонок');
PERFORM _seed_msg('label',  'Message',                                'Xabar',                           'Хабар',                           'Сообщение');
PERFORM _seed_msg('label',  'Text',                                   'Matn',                            'Матн',                            'Текст');
PERFORM _seed_msg('label',  'Version',                                'Versiya',                         'Версия',                          'Версия');
PERFORM _seed_msg('label',  'Security',                               'Xavfsizlik',                      'Хавфсизлик',                      'Безопасность');
PERFORM _seed_msg('label',  'University',                             'Universitet',                     'Университет',                     'Университет');
PERFORM _seed_msg('label',  'University Information',                 'Universitet ma''lumotlari',       'Университет маълумотлари',        'Информация об университете');
PERFORM _seed_msg('label',  'University directions',                  'Universitet yo''nalishlari',      'Университет йўналишлари',         'Направления университета');
PERFORM _seed_msg('label',  'User information',                       'Foydalanuvchi ma''lumotlari',     'Фойдаланувчи маълумотлари',       'Информация о пользователе');
PERFORM _seed_msg('label',  'Profile saved',                          'Profil saqlandi',                 'Профил сақланди',                 'Профиль сохранён');
PERFORM _seed_msg('label',  'Today, {{time}}',                        'Bugun, {{time}}',                 'Бугун, {{time}}',                 'Сегодня, {{time}}');

-- ──────────────────────────────────────────────────────
-- AUTH (login, password reset, lock)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('auth',   'Check your email',                       'Pochtangizni tekshiring',         'Почтангизни текширинг',           'Проверьте почту');
PERFORM _seed_msg('auth',   'Enter your email and we will send you a reset link',
                                                                       'Email manzilingizni kiriting, biz tiklash havolasini yuboramiz',
                                                                       'Email манзилингизни киритинг, биз тиклаш ҳаволасини юборамиз',
                                                                       'Введите email, мы отправим ссылку для сброса');
PERFORM _seed_msg('auth',   'Enter your new password below',          'Quyida yangi parolingizni kiriting','Қуйида янги паролингизни киритинг','Введите новый пароль ниже');
PERFORM _seed_msg('auth',   'Reset link sent to your email',          'Tiklash havolasi pochtangizga yuborildi','Тиклаш ҳаволаси почтангизга юборилди','Ссылка для сброса отправлена');
PERFORM _seed_msg('auth',   'Resend in {{seconds}}s',                 '{{seconds}} soniyadan keyin qayta yuborish','{{seconds}} сониядан кейин қайта юбориш','Повторить через {{seconds}}с');
PERFORM _seed_msg('auth',   'Password has been reset successfully',   'Parol muvaffaqiyatli tiklandi',   'Парол муваффақиятли тикланди',    'Пароль успешно сброшен');
PERFORM _seed_msg('auth',   'Session expired due to inactivity',      'Sessiya faol bo''lmaganligi sababli muddati tugadi','Сессия фаол бўлмаганлиги сабабли муддати тугади','Сессия истекла из-за бездействия');
PERFORM _seed_msg('auth',   'This account is currently locked',       'Bu akkaunt hozirda qulflangan',   'Бу аккаунт ҳозирда қулфланган',   'Этот аккаунт заблокирован');
PERFORM _seed_msg('auth',   'Checking...',                            'Tekshirilmoqda...',               'Текширилмоқда...',                'Проверка...');
PERFORM _seed_msg('auth',   'HEMIS. All rights reserved.',            'HEMIS. Barcha huquqlar himoyalangan.','HEMIS. Барча ҳуқуқлар ҳимояланган.','HEMIS. Все права защищены.');
PERFORM _seed_msg('auth',   'Ministry Portal',                        'Vazirlik portali',                'Вазирлик портали',                'Портал министерства');

-- ──────────────────────────────────────────────────────
-- VALIDATION (form errors, input rules)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('validation', 'Required',                                              'Majburiy',                        'Мажбурий',                        'Обязательно');
PERFORM _seed_msg('validation', 'Email already in use',                                  'Email allaqachon ishlatilgan',    'Email аллақачон ишлатилган',      'Email уже используется');
PERFORM _seed_msg('validation', 'Username already exists',                               'Foydalanuvchi nomi mavjud',       'Фойдаланувчи номи мавжуд',        'Имя пользователя уже существует');
PERFORM _seed_msg('validation', 'Invalid email format',                                  'Email formati noto''g''ri',       'Email формати нотўғри',           'Неверный формат email');
PERFORM _seed_msg('validation', 'Invalid or missing reset token',                        'Tiklash tokeni noto''g''ri yoki yo''q','Тиклаш токени нотўғри ёки йўқ','Недействительный или отсутствующий токен');
PERFORM _seed_msg('validation', 'Invalid reset link',                                    'Tiklash havolasi noto''g''ri',    'Тиклаш ҳаволаси нотўғри',         'Неверная ссылка для сброса');
PERFORM _seed_msg('validation', 'Invalid role name',                                     'Rol nomi noto''g''ri',            'Рол номи нотўғри',                'Неверное имя роли');
PERFORM _seed_msg('validation', 'Code must be uppercase letters, digits, and underscores','Kod katta harf, raqam va pastki chiziqdan iborat bo''lishi kerak','Код катта ҳарф, рақам ва пастки чизиқдан иборат бўлиши керак','Код должен содержать заглавные буквы, цифры и подчёркивания');
PERFORM _seed_msg('validation', 'Name must be 2-100 characters',                         'Nomi 2-100 belgi bo''lishi kerak','Номи 2-100 белги бўлиши керак',   'Имя должно быть 2-100 символов');
PERFORM _seed_msg('validation', 'Password must be at least 6 characters',                'Parol kamida 6 belgidan iborat bo''lishi kerak','Парол камида 6 белгидан иборат бўлиши керак','Пароль должен быть не менее 6 символов');
PERFORM _seed_msg('validation', 'Phone number must be in format +998XXXXXXXXX',          'Telefon raqami +998XXXXXXXXX formatida bo''lishi kerak','Телефон рақами +998XXXXXXXXX форматида бўлиши керак','Номер телефона в формате +998XXXXXXXXX');

-- ──────────────────────────────────────────────────────
-- MESSAGES (toasts, info messages, empty states)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('message', 'No data available',                                        'Ma''lumot mavjud emas',           'Маълумот мавжуд эмас',            'Нет данных');
PERFORM _seed_msg('message', 'No active records',                                        'Faol yozuvlar yo''q',             'Фаол ёзувлар йўқ',                'Нет активных записей');
PERFORM _seed_msg('message', 'No groups have been added yet',                            'Hali birorta guruh qo''shilmagan','Ҳали бирорта гуруҳ қўшилмаган',   'Пока не добавлено ни одной группы');
PERFORM _seed_msg('message', 'No duplicate translations found',                          'Dublikat tarjimalar topilmadi',   'Дубликат таржималар топилмади',   'Дубликаты переводов не найдены');
PERFORM _seed_msg('message', 'No faculties have been added yet',                         'Hali fakultetlar qo''shilmagan',  'Ҳали факультетлар қўшилмаган',    'Факультеты ещё не добавлены');
PERFORM _seed_msg('message', 'No permissions available',                                 'Ruxsatlar mavjud emas',           'Рухсатлар мавжуд эмас',           'Разрешения недоступны');
PERFORM _seed_msg('message', 'No permissions found',                                     'Ruxsatlar topilmadi',             'Рухсатлар топилмади',             'Разрешения не найдены');
PERFORM _seed_msg('message', 'No roles found',                                           'Rollar topilmadi',                'Роллар топилмади',                'Роли не найдены');
PERFORM _seed_msg('message', 'No roles have been created yet',                           'Hali rollar yaratilmagan',        'Ҳали роллар яратилмаган',         'Роли ещё не созданы');
PERFORM _seed_msg('message', 'No students found',                                        'Talabalar topilmadi',             'Талабалар топилмади',             'Студенты не найдены');
PERFORM _seed_msg('message', 'No translations found',                                    'Tarjimalar topilmadi',            'Таржималар топилмади',            'Переводы не найдены');
PERFORM _seed_msg('message', 'Cache cleared',                                            'Kesh tozalandi',                  'Кеш тозаланди',                   'Кеш очищен');
PERFORM _seed_msg('message', 'Cache cleared successfully',                               'Kesh muvaffaqiyatli tozalandi',   'Кеш муваффақиятли тозаланди',     'Кеш успешно очищен');
PERFORM _seed_msg('message', 'JSON files downloaded',                                    'JSON fayllari yuklab olindi',     'JSON файллари юклаб олинди',      'JSON файлы загружены');
PERFORM _seed_msg('message', 'JSON files downloading...',                                'JSON fayllari yuklanmoqda...',    'JSON файллари юкланмоқда...',     'Загрузка JSON файлов...');
PERFORM _seed_msg('message', 'External data synced successfully',                        'Tashqi ma''lumotlar muvaffaqiyatli sinxronlandi','Ташқи маълумотлар муваффақиятли синхронланди','Внешние данные успешно синхронизированы');
PERFORM _seed_msg('message', 'Lifecycle event added',                                    'Hayot davri hodisasi qo''shildi', 'Ҳаёт даври ҳодисаси қўшилди',     'Событие жизненного цикла добавлено');
PERFORM _seed_msg('message', 'Official appointed successfully',                          'Mansabdor muvaffaqiyatli tayinlandi','Мансабдор муваффақиятли тайинланди','Должностное лицо назначено');
PERFORM _seed_msg('message', 'Official removed',                                         'Mansabdor olib tashlandi',        'Мансабдор олиб ташланди',         'Должностное лицо удалено');
PERFORM _seed_msg('message', 'Translation activated',                                    'Tarjima faollashtirildi',         'Таржима фаоллаштирилди',          'Перевод активирован');
PERFORM _seed_msg('message', 'Translation deactivated',                                  'Tarjima faolsizlantirildi',       'Таржима фаолсизлантирилди',       'Перевод деактивирован');
PERFORM _seed_msg('message', 'University successfully created',                          'Universitet muvaffaqiyatli yaratildi','Университет муваффақиятли яратилди','Университет успешно создан');
PERFORM _seed_msg('message', 'University successfully deleted',                          'Universitet muvaffaqiyatli o''chirildi','Университет муваффақиятли ўчирилди','Университет успешно удалён');
PERFORM _seed_msg('message', 'University successfully updated',                          'Universitet muvaffaqiyatli yangilandi','Университет муваффақиятли янгиланди','Университет успешно обновлён');
PERFORM _seed_msg('message', 'Role successfully created',                                'Rol muvaffaqiyatli yaratildi',    'Рол муваффақиятли яратилди',      'Роль успешно создана');
PERFORM _seed_msg('message', 'Role successfully deleted',                                'Rol muvaffaqiyatli o''chirildi',  'Рол муваффақиятли ўчирилди',      'Роль успешно удалена');
PERFORM _seed_msg('message', 'Role successfully updated',                                'Rol muvaffaqiyatli yangilandi',   'Рол муваффақиятли янгиланди',     'Роль успешно обновлена');
PERFORM _seed_msg('message', 'Permissions and translations updated. Page reloading...',  'Ruxsatlar va tarjimalar yangilandi. Sahifa qayta yuklanmoqda...','Рухсатлар ва таржималар янгиланди. Саҳифа қайта юкланмоқда...','Разрешения и переводы обновлены. Перезагрузка...');
PERFORM _seed_msg('message', 'This page is under development',                           'Bu sahifa ishlab chiqilmoqda',    'Бу саҳифа ишлаб чиқилмоқда',      'Эта страница в разработке');
PERFORM _seed_msg('message', 'Teacher management features will be available once the backend API is ready','O''qituvchilarni boshqarish funksiyalari backend API tayyor bo''lganda mavjud bo''ladi','Ўқитувчиларни бошқариш функсиялари backend API тайёр бўлганда мавжуд бўлади','Управление преподавателями будет доступно после готовности backend API');
PERFORM _seed_msg('message', 'You have unsaved changes. Are you sure you want to leave?','Saqlanmagan o''zgarishlar bor. Sahifadan chiqmoqchimisiz?','Сақланмаган ўзгаришлар бор. Саҳифадан чиқмоқчимисиз?','Есть несохранённые изменения. Покинуть страницу?');
PERFORM _seed_msg('message', 'Connection restored',                                      'Aloqa tiklandi',                  'Алоқа тикланди',                  'Соединение восстановлено');
PERFORM _seed_msg('message', 'Your internet connection is back',                         'Internet aloqangiz tiklandi',     'Интернет алоқангиз тикланди',     'Интернет-соединение восстановлено');
PERFORM _seed_msg('message', 'No internet connection',                                   'Internet aloqasi yo''q',          'Интернет алоқаси йўқ',            'Нет интернет-соединения');
PERFORM _seed_msg('message', 'Please check your network connection',                     'Tarmoq aloqangizni tekshiring',   'Тармоқ алоқангизни текширинг',    'Проверьте сетевое соединение');
PERFORM _seed_msg('message', 'Please try again later',                                   'Iltimos, keyinroq urinib ko''ring','Илтимос, кейинроқ уриниб кўринг','Попробуйте позже');
PERFORM _seed_msg('message', 'Please try again after {{seconds}} seconds',               'Iltimos, {{seconds}} soniyadan keyin urinib ko''ring','Илтимос, {{seconds}} сониядан кейин уриниб кўринг','Попробуйте через {{seconds}} секунд');
PERFORM _seed_msg('message', 'Too many requests',                                        'Juda ko''p so''rovlar',           'Жуда кўп сўровлар',               'Слишком много запросов');

-- Reports page strings (categories, descriptions, parameter counts)
-- NOTE: 'Student reports', 'Teacher reports', 'University reports', 'Scientific activity',
--       'Scientific publications', 'Scientific projects', 'Intellectual property'
--       are already seeded by S006 (menu category) — not duplicated here.
PERFORM _seed_msg('label',   'Students report',                                          'Talabalar hisoboti',              'Талабалар ҳисоботи',              'Отчёт по студентам');
PERFORM _seed_msg('label',   'Teachers report',                                          'O''qituvchilar hisoboti',         'Ўқитувчилар ҳисоботи',            'Отчёт по преподавателям');
PERFORM _seed_msg('label',   'Institutions report',                                      'Tashkilotlar hisoboti',           'Ташкилотлар ҳисоботи',            'Отчёт по учреждениям');
PERFORM _seed_msg('label',   'Academic report',                                          'Akademik hisobot',                'Академик ҳисобот',                'Академический отчёт');
PERFORM _seed_msg('label',   'Scientific report',                                        'Ilmiy hisobot',                   'Илмий ҳисобот',                   'Научный отчёт');
PERFORM _seed_msg('label',   'Economic report',                                          'Iqtisodiy hisobot',               'Иқтисодий ҳисобот',               'Экономический отчёт');
PERFORM _seed_msg('label',   'Dissertation defenses',                                    'Dissertatsiya himoyalari',        'Диссертация ҳимоялари',           'Защиты диссертаций');
PERFORM _seed_msg('label',   'Patents, licenses',                                        'Patentlar, litsenziyalar',        'Патентлар, лицензиялар',          'Патенты, лицензии');
PERFORM _seed_msg('label',   'Scopus, Web of Science publications',                      'Scopus, Web of Science nashrlari','Scopus, Web of Science нашрлари', 'Публикации Scopus, Web of Science');
PERFORM _seed_msg('label',   'Local and international projects',                         'Mahalliy va xalqaro loyihalar',   'Маҳаллий ва халқаро лойиҳалар',   'Местные и международные проекты');
PERFORM _seed_msg('label',   'Candidate of Sciences, Doctor of Sciences',                'Fan nomzodi, Fan doktori',        'Фан номзоди, Фан доктори',        'Кандидат наук, Доктор наук');
PERFORM _seed_msg('label',   'Doctor of Science, PhD, DSc',                              'Fan doktori, PhD, DSc',           'Фан доктори, PhD, DSc',           'Доктор наук, PhD, DSc');
PERFORM _seed_msg('label',   'Bachelor, Master, PhD distribution',                       'Bakalavr, Magistr, PhD taqsimoti','Бакалавр, Магистр, PhD тақсимоти','Распределение бакалавр, магистр, PhD');
PERFORM _seed_msg('label',   'Total students count and distribution',                    'Talabalarning umumiy soni va taqsimoti','Талабаларнинг умумий сони ва тақсимоти','Общее число студентов и распределение');
PERFORM _seed_msg('label',   'Grant and contract students',                              'Grant va shartnoma asosidagi talabalar','Грант ва шартнома асосидаги талабалар','Грант и контрактные студенты');
PERFORM _seed_msg('label',   'Distribution by regions',                                  'Hududlar bo''yicha taqsimot',     'Ҳудудлар бўйича тақсимот',        'Распределение по регионам');
PERFORM _seed_msg('label',   'Department employees',                                     'Kafedra xodimlari',               'Кафедра ходимлари',               'Сотрудники кафедры');
PERFORM _seed_msg('label',   'Professor, Associate professor statistics',                'Professor, Dotsent statistikasi', 'Профессор, Доцент статистикаси',  'Статистика профессоров, доцентов');
PERFORM _seed_msg('label',   'General indicators',                                       'Umumiy ko''rsatkichlar',          'Умумий кўрсаткичлар',             'Общие показатели');
PERFORM _seed_msg('label',   'Main HEI statistics',                                      'OTMlar asosiy statistikasi',      'ОТМлар асосий статистикаси',      'Основная статистика вузов');
PERFORM _seed_msg('label',   'HEI rating and comparison',                                'OTMlar reytingi va taqqoslash',   'ОТМлар рейтинги ва таққослаш',    'Рейтинг и сравнение вузов');
PERFORM _seed_msg('label',   'Institute, University, Academy',                           'Institut, Universitet, Akademiya','Институт, Университет, Академия', 'Институт, Университет, Академия');
PERFORM _seed_msg('label',   'State, Private, Joint',                                    'Davlat, Xususiy, Qo''shma',       'Давлат, Хусусий, Қўшма',          'Государственный, Частный, Совместный');
PERFORM _seed_msg('label',   'Form of ownership',                                        'Mulkchilik shakli',               'Мулкчилик шакли',                 'Форма собственности');
PERFORM _seed_msg('label',   'By education type',                                        'Ta''lim turi bo''yicha',          'Таълим тури бўйича',              'По типу образования');
PERFORM _seed_msg('label',   'By payment type',                                          'To''lov turi bo''yicha',          'Тўлов тури бўйича',               'По типу оплаты');
PERFORM _seed_msg('label',   'By region',                                                'Hudud bo''yicha',                 'Ҳудуд бўйича',                    'По региону');
PERFORM _seed_msg('label',   'By scientific degrees',                                    'Ilmiy darajalar bo''yicha',       'Илмий даражалар бўйича',          'По научным степеням');
PERFORM _seed_msg('label',   'By academic titles',                                       'Ilmiy unvonlar bo''yicha',        'Илмий унвонлар бўйича',           'По учёным званиям');
PERFORM _seed_msg('label',   'By experience',                                            'Tajriba bo''yicha',               'Тажриба бўйича',                  'По опыту');
PERFORM _seed_msg('label',   'By work experience',                                       'Ish staji bo''yicha',             'Иш стажи бўйича',                 'По стажу работы');
PERFORM _seed_msg('label',   'By departments',                                           'Kafedralar bo''yicha',            'Кафедралар бўйича',               'По кафедрам');
PERFORM _seed_msg('label',   'By organizational form',                                   'Tashkiliy shakl bo''yicha',       'Ташкилий шакл бўйича',            'По организационной форме');
PERFORM _seed_msg('label',   'By rating',                                                'Reyting bo''yicha',               'Рейтинг бўйича',                  'По рейтингу');
-- Plural-aware count keys.
--
-- i18next picks the right variant by suffix at runtime: `_one`, `_few`,
-- `_many`, `_other`. Russian uses all four (CLDR), Uzbek/English use the
-- one/other split. Each variant is its own message_key in this catalog,
-- so the gettext-style helper still applies.
-- 6-arg forma: en EXPLICIT (toza) — suffiks UI'ga sizib chiqmaydi. Har plural
-- kalitning BASE qatori ham bor (gate un-suffiksli t('...') literalini qidiradi +
-- til-ichi fallback). uz/oz — {one, other} (invariant), ru — CLDR one/few/many/other.
PERFORM _seed_msg('label', '{{count}} parameters',        '{{count}} parameters',  '{{count}} parametr', '{{count}} параметр', '{{count}} параметров');
PERFORM _seed_msg('label', '{{count}} parameters_one',    '{{count}} parameter',   '{{count}} parametr', '{{count}} параметр', '{{count}} параметр');
PERFORM _seed_msg('label', '{{count}} parameters_few',    '{{count}} parameters',  '{{count}} parametr', '{{count}} параметр', '{{count}} параметра');
PERFORM _seed_msg('label', '{{count}} parameters_many',   '{{count}} parameters',  '{{count}} parametr', '{{count}} параметр', '{{count}} параметров');
PERFORM _seed_msg('label', '{{count}} parameters_other',  '{{count}} parameters',  '{{count}} parametr', '{{count}} параметр', '{{count}} параметров');
PERFORM _seed_msg('label', '{{count}} students found',       '{{count}} students found', '{{count}} ta talaba topildi', '{{count}} та талаба топилди', 'Найдено {{count}} студентов');
PERFORM _seed_msg('label', '{{count}} students found_one',   '{{count}} student found',  '{{count}} ta talaba topildi', '{{count}} та талаба топилди', 'Найден {{count}} студент');
PERFORM _seed_msg('label', '{{count}} students found_few',   '{{count}} students found', '{{count}} ta talaba topildi', '{{count}} та талаба топилди', 'Найдено {{count}} студента');
PERFORM _seed_msg('label', '{{count}} students found_many',  '{{count}} students found', '{{count}} ta talaba topildi', '{{count}} та талаба топилди', 'Найдено {{count}} студентов');
PERFORM _seed_msg('label', '{{count}} students found_other', '{{count}} students found', '{{count}} ta talaba topildi', '{{count}} та талаба топилди', 'Найдено {{count}} студентов');
PERFORM _seed_msg('label', '{{count}} groups found',       '{{count}} groups found', '{{count}} ta guruh topildi', '{{count}} та гуруҳ топилди', 'Найдено {{count}} групп');
PERFORM _seed_msg('label', '{{count}} groups found_one',   '{{count}} group found',  '{{count}} ta guruh topildi', '{{count}} та гуруҳ топилди', 'Найдена {{count}} группа');
PERFORM _seed_msg('label', '{{count}} groups found_few',   '{{count}} groups found', '{{count}} ta guruh topildi', '{{count}} та гуруҳ топилди', 'Найдено {{count}} группы');
PERFORM _seed_msg('label', '{{count}} groups found_many',  '{{count}} groups found', '{{count}} ta guruh topildi', '{{count}} та гуруҳ топилди', 'Найдено {{count}} групп');
PERFORM _seed_msg('label', '{{count}} groups found_other', '{{count}} groups found', '{{count}} ta guruh topildi', '{{count}} та гуруҳ топилди', 'Найдено {{count}} групп');
PERFORM _seed_msg('label', '{{count}} specialities found',       '{{count}} specialities found', '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди', 'Найдено {{count}} направлений');
PERFORM _seed_msg('label', '{{count}} specialities found_one',   '{{count}} speciality found',   '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди', 'Найдено {{count}} направление');
PERFORM _seed_msg('label', '{{count}} specialities found_few',   '{{count}} specialities found', '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди', 'Найдено {{count}} направления');
PERFORM _seed_msg('label', '{{count}} specialities found_many',  '{{count}} specialities found', '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди', 'Найдено {{count}} направлений');
PERFORM _seed_msg('label', '{{count}} specialities found_other', '{{count}} specialities found', '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди', 'Найдено {{count}} направлений');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters',       'Must be at least {{count}} characters', 'Kamida {{count}} belgi bo''lishi kerak', 'Камида {{count}} белги бўлиши керак', 'Должно быть не менее {{count}} символов');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_one',   'Must be at least {{count}} character',  'Kamida {{count}} belgi bo''lishi kerak', 'Камида {{count}} белги бўлиши керак', 'Должно быть не менее {{count}} символа');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_few',   'Must be at least {{count}} characters', 'Kamida {{count}} belgi bo''lishi kerak', 'Камида {{count}} белги бўлиши керак', 'Должно быть не менее {{count}} символов');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_many',  'Must be at least {{count}} characters', 'Kamida {{count}} belgi bo''lishi kerak', 'Камида {{count}} белги бўлиши керак', 'Должно быть не менее {{count}} символов');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_other', 'Must be at least {{count}} characters', 'Kamida {{count}} belgi bo''lishi kerak', 'Камида {{count}} белги бўлиши керак', 'Должно быть не менее {{count}} символов');
PERFORM _seed_msg('label',   'Certificates',                                             'Sertifikatlar',                   'Сертификатлар',                   'Сертификаты');
PERFORM _seed_msg('label',   'Scholarships',                                             'Stipendiyalar',                   'Стипендиялар',                    'Стипендии');
PERFORM _seed_msg('label',   'Qualifications',                                           'Malakalar',                       'Малакалар',                       'Квалификации');
PERFORM _seed_msg('label',   'GPA rating',                                               'GPA reyting',                     'GPA рейтинг',                     'Рейтинг GPA');

-- Page-not-found and error pages
PERFORM _seed_msg('error',   'Page not found',                                           'Sahifa topilmadi',                'Саҳифа топилмади',                'Страница не найдена');
PERFORM _seed_msg('error',   'Sorry, the page you''re looking for doesn''t exist or has been moved.','Kechirasiz, siz qidirayotgan sahifa mavjud emas yoki ko''chirilgan.','Кечирасиз, сиз қидираётган саҳифа мавжуд эмас ёки кўчирилган.','Извините, страница, которую вы ищете, не существует или была перемещена.');
PERFORM _seed_msg('error',   'If you believe this is an error, please contact the system administrator.','Agar bu xato deb hisoblasangiz, tizim administratoriga murojaat qiling.','Агар бу хато деб ҳисобласангиз, тизим администраторига мурожаат қилинг.','Если вы считаете это ошибкой, обратитесь к администратору.');
PERFORM _seed_msg('error',   'Access denied',                                            'Ruxsat berilmagan',               'Рухсат берилмаган',               'Доступ запрещён');
PERFORM _seed_msg('error',   'You don''t have permission to access this page.',           'Sizda bu sahifaga kirish uchun ruxsat yo''q.','Сизда бу саҳифага кириш учун рухсат йўқ.','У вас нет прав для доступа к этой странице.');
PERFORM _seed_msg('error',   'An error occurred while loading this page. Please try again.','Bu sahifani yuklashda xato yuz berdi. Iltimos, qayta urinib ko''ring.','Бу саҳифани юклашда хато юз берди. Илтимос, қайта уриниб кўринг.','Произошла ошибка при загрузке страницы. Попробуйте снова.');
PERFORM _seed_msg('error',   'Failed to add lifecycle event',                            'Hayot davri hodisasini qo''shib bo''lmadi','Ҳаёт даври ҳодисасини қўшиб бўлмади','Не удалось добавить событие жизненного цикла');
PERFORM _seed_msg('error',   'Failed to appoint official',                               'Mansabdor tayinlanmadi',          'Мансабдор тайинланмади',          'Не удалось назначить должностное лицо');
PERFORM _seed_msg('error',   'Failed to change password',                                'Parol o''zgartirilmadi',          'Парол ўзгартирилмади',            'Не удалось изменить пароль');
PERFORM _seed_msg('error',   'Failed to create role',                                    'Rol yaratilmadi',                 'Рол яратилмади',                  'Не удалось создать роль');
PERFORM _seed_msg('error',   'Failed to create user',                                    'Foydalanuvchi yaratilmadi',       'Фойдаланувчи яратилмади',         'Не удалось создать пользователя');
PERFORM _seed_msg('error',   'Failed to delete role',                                    'Rol o''chirilmadi',               'Рол ўчирилмади',                  'Не удалось удалить роль');
PERFORM _seed_msg('error',   'Failed to delete user',                                    'Foydalanuvchi o''chirilmadi',     'Фойдаланувчи ўчирилмади',         'Не удалось удалить пользователя');
PERFORM _seed_msg('error',   'Failed to load translations',                              'Tarjimalar yuklanmadi',           'Таржималар юкланмади',            'Не удалось загрузить переводы');
PERFORM _seed_msg('error',   'Failed to load university information',                    'Universitet ma''lumotlari yuklanmadi','Университет маълумотлари юкланмади','Не удалось загрузить информацию об университете');
PERFORM _seed_msg('error',   'Failed to remove official',                                'Mansabdor olib tashlanmadi',      'Мансабдор олиб ташланмади',       'Не удалось удалить должностное лицо');
PERFORM _seed_msg('error',   'Failed to save profile',                                   'Profil saqlanmadi',               'Профил сақланмади',               'Не удалось сохранить профиль');
PERFORM _seed_msg('error',   'Failed to sync external data',                             'Tashqi ma''lumotlar sinxronlanmadi','Ташқи маълумотлар синхронланмади','Не удалось синхронизировать внешние данные');
PERFORM _seed_msg('error',   'Failed to toggle active status',                           'Faol holatni almashtirib bo''lmadi','Фаол ҳолатни алмаштириб бўлмади','Не удалось переключить статус');
PERFORM _seed_msg('error',   'Failed to unlock account',                                 'Akkauntni ochib bo''lmadi',       'Аккаунтни очиб бўлмади',          'Не удалось разблокировать аккаунт');
PERFORM _seed_msg('error',   'Failed to update role',                                    'Rol yangilanmadi',                'Рол янгиланмади',                 'Не удалось обновить роль');
PERFORM _seed_msg('error',   'Failed to update status',                                  'Status yangilanmadi',             'Статус янгиланмади',              'Не удалось обновить статус');
PERFORM _seed_msg('error',   'Failed to update user',                                    'Foydalanuvchi yangilanmadi',      'Фойдаланувчи янгиланмади',        'Не удалось обновить пользователя');
PERFORM _seed_msg('error',   'Error checking duplicates',                                'Dublikatlarni tekshirishda xato', 'Дубликатларни текширишда хато',   'Ошибка проверки дубликатов');
PERFORM _seed_msg('error',   'Error clearing cache',                                     'Keshni tozalashda xato',          'Кешни тозалашда хато',            'Ошибка очистки кеша');
PERFORM _seed_msg('error',   'Error downloading JSON',                                   'JSON yuklab olishda xato',        'JSON юклаб олишда хато',          'Ошибка загрузки JSON');
PERFORM _seed_msg('error',   'Error generating properties',                              'Properties yaratishda xato',      'Properties яратишда хато',        'Ошибка генерации properties');
PERFORM _seed_msg('error',   'Role code already exists',                                 'Rol kodi allaqachon mavjud',      'Рол коди аллақачон мавжуд',       'Код роли уже существует');
PERFORM _seed_msg('error',   'Role not found',                                           'Rol topilmadi',                   'Рол топилмади',                   'Роль не найдена');
PERFORM _seed_msg('error',   'User not found',                                           'Foydalanuvchi topilmadi',         'Фойдаланувчи топилмади',          'Пользователь не найден');
PERFORM _seed_msg('error',   'Unknown error',                                            'Noma''lum xato',                  'Номаълум хато',                   'Неизвестная ошибка');
PERFORM _seed_msg('error',   'You do not have permission to perform this action',        'Bu amalni bajarish uchun ruxsatingiz yo''q','Бу амални бажариш учун рухсатингиз йўқ','У вас нет разрешения на это действие');

-- Confirmations (with interpolation)
PERFORM _seed_msg('confirm', 'Are you sure you want to {{action}} user',                 '{{action}} foydalanuvchini xohlaganingizga ishonchingiz komilmi','{{action}} фойдаланувчини хоҳлаганингизга ишончингиз комилми','Вы уверены, что хотите {{action}} пользователя');
PERFORM _seed_msg('confirm', 'Are you sure you want to unlock user',                     'Foydalanuvchini ochmoqchimisiz','Фойдаланувчини очмоқчимисиз',       'Вы уверены, что хотите разблокировать пользователя');

-- Social network labels (proper nouns — keep as-is across languages)
PERFORM _seed_msg('label',   'Facebook',                                                 'Facebook',                        'Facebook',                        'Facebook');
PERFORM _seed_msg('label',   'Instagram',                                                'Instagram',                       'Instagram',                       'Instagram');
PERFORM _seed_msg('label',   'Telegram',                                                 'Telegram',                        'Telegram',                        'Telegram');
PERFORM _seed_msg('label',   'Twitter',                                                  'Twitter',                         'Twitter',                         'Twitter');
PERFORM _seed_msg('label',   'LinkedIn',                                                 'LinkedIn',                        'LinkedIn',                        'LinkedIn');
PERFORM _seed_msg('label',   'YouTube',                                                  'YouTube',                         'YouTube',                         'YouTube');

-- Departments (Kafedralar) registry — kafedra = department_type 12
PERFORM _seed_msg('label',   'Department count',                                         'Kafedralar soni',                 'Кафедралар сони',                 'Количество кафедр');
PERFORM _seed_msg('label',   'Department type',                                          'Kafedra turi',                    'Кафедра тури',                    'Тип кафедры');
PERFORM _seed_msg('message', 'No departments have been added yet',                       'Hali birorta kafedra qo''shilmagan','Ҳали бирорта кафедра қўшилмаган', 'Кафедры еще не добавлены');

-- Attached specialities (Biriktirilgan mutaxassisliklar) registry — university speciality CRUD card
PERFORM _seed_msg('label',   'University specialities',                                  'Universitet mutaxassisliklari',   'Университет мутахассисликлари',   'Специальности университета');
PERFORM _seed_msg('action',  'Add attached speciality',                                  'Mutaxassislik biriktirish',       'Мутахассислик бириктириш',        'Прикрепить специальность');
PERFORM _seed_msg('action',  'Edit attached speciality',                                 'Biriktirilgan mutaxassislikni tahrirlash','Бириктирилган мутахассисликни таҳрирлаш','Редактировать прикреплённую специальность');
PERFORM _seed_msg('label',   'Education type',                                           'Ta''lim turi',                    'Таълим тури',                     'Тип образования');
PERFORM _seed_msg('label',   'Speciality level',                                         'Mutaxassislik darajasi',          'Мутахассислик даражаси',          'Уровень специальности');
PERFORM _seed_msg('label',   'Speciality',                                               'Mutaxassislik',                   'Мутахассислик',                   'Специальность');
PERFORM _seed_msg('label',   'Bachelor',                                                 'Bakalavr',                        'Бакалавр',                        'Бакалавр');
PERFORM _seed_msg('label',   'Master',                                                   'Magistr',                         'Магистр',                         'Магистр');
PERFORM _seed_msg('label',   'Ordinatura',                                               'Ordinatura',                      'Ординатура',                      'Ординатура');
PERFORM _seed_msg('label',   'Doctoral',                                                 'Doktorantura',                    'Докторантура',                    'Докторантура');
PERFORM _seed_msg('message', 'No attached specialities have been added yet',             'Hali birorta mutaxassislik biriktirilmagan','Ҳали бирорта мутахассислик бириктирилмаган','Пока не прикреплено ни одной специальности');
PERFORM _seed_msg('message', 'Attached speciality created',                              'Mutaxassislik biriktirildi',      'Мутахассислик бириктирилди',      'Специальность прикреплена');
PERFORM _seed_msg('message', 'Attached speciality updated',                              'Biriktirilgan mutaxassislik yangilandi','Бириктирилган мутахассислик янгиланди','Прикреплённая специальность обновлена');
PERFORM _seed_msg('message', 'Attached speciality deleted',                              'Biriktirilgan mutaxassislik o''chirildi','Бириктирилган мутахассислик ўчирилди','Прикреплённая специальность удалена');
PERFORM _seed_msg('confirm', 'Delete attached speciality?',                              'Biriktirilgan mutaxassislik o''chirilsinmi?','Бириктирилган мутахассислик ўчирилсинми?','Удалить прикреплённую специальность?');

-- Diploma blanks + Blank distribution registry cards (institutions module)
-- NOTE: 'Number' already seeded below (student registry) — reused, not duplicated.
PERFORM _seed_msg('label',   'Diploma blanks',                                           'Diplom blankalari',               'Диплом бланкалари',               'Дипломные бланки');
PERFORM _seed_msg('label',   'Blank distribution',                                       'Blank taqsimlash',                'Бланк тақсимлаш',                 'Распределение бланков');
PERFORM _seed_msg('label',   'Blank code',                                               'Blank kodi',                      'Бланк коди',                      'Код бланка');
PERFORM _seed_msg('label',   'Series',                                                   'Seriya',                          'Серия',                           'Серия');
PERFORM _seed_msg('label',   'Start number',                                             'Boshlang''ich raqam',             'Бошланғич рақам',                 'Начальный номер');
PERFORM _seed_msg('label',   'End number',                                               'Oxirgi raqam',                    'Охирги рақам',                    'Конечный номер');
PERFORM _seed_msg('label',   'Quantity',                                                 'Miqdor',                          'Миқдор',                          'Количество');
PERFORM _seed_msg('label',   'Blank category',                                           'Blank toifasi',                   'Бланк тоифаси',                   'Категория бланка');
PERFORM _seed_msg('label',   'Received date',                                            'Qabul qilingan sana',             'Қабул қилинган сана',             'Дата получения');
PERFORM _seed_msg('label',   'Distribution date',                                        'Taqsimlash sanasi',               'Тақсимлаш санаси',                'Дата распределения');
PERFORM _seed_msg('label',   'Supplier',                                                 'Yetkazib beruvchi',               'Етказиб берувчи',                 'Поставщик');
PERFORM _seed_msg('action',  'Add distribution',                                         'Taqsimlash qo''shish',            'Тақсимлаш қўшиш',                 'Добавить распределение');
PERFORM _seed_msg('action',  'Edit distribution',                                        'Taqsimlashni tahrirlash',         'Тақсимлашни таҳрирлаш',           'Редактировать распределение');
PERFORM _seed_msg('message', 'No diploma blanks have been added yet',                    'Hali birorta blank qo''shilmagan','Ҳали бирорта бланк қўшилмаган',   'Пока не добавлено ни одного бланка');
PERFORM _seed_msg('message', 'No distributions have been added yet',                     'Hali birorta taqsimlash qo''shilmagan','Ҳали бирорта тақсимлаш қўшилмаган','Пока не добавлено ни одного распределения');

-- Student registry cards — Diplomas, Scholarships, Certificates
PERFORM _seed_msg('label',   'Student',                                                  'Talaba',                          'Талаба',                          'Студент');
PERFORM _seed_msg('label',   'Diploma number',                                           'Diplom raqami',                   'Диплом рақами',                   'Номер диплома');
PERFORM _seed_msg('label',   'Register number',                                          'Reyestr raqami',                  'Реестр рақами',                   'Регистрационный номер');
PERFORM _seed_msg('label',   'Register date',                                            'Reyestr sanasi',                  'Реестр санаси',                   'Дата регистрации');
PERFORM _seed_msg('label',   'Graduation date',                                          'Bitirish sanasi',                 'Битириш санаси',                  'Дата выпуска');
PERFORM _seed_msg('label',   'Average grade',                                            'O''rtacha baho',                  'Ўртача баҳо',                     'Средний балл');
PERFORM _seed_msg('label',   'Total credit',                                             'Jami kredit',                     'Жами кредит',                     'Всего кредитов');
PERFORM _seed_msg('label',   'Admission year',                                           'Qabul yili',                      'Қабул йили',                      'Год поступления');
PERFORM _seed_msg('label',   'Verified',                                                 'Tasdiqlangan',                    'Тасдиқланган',                    'Подтверждён');
PERFORM _seed_msg('message', 'No diplomas have been added yet',                          'Hali birorta diplom qo''shilmagan','Ҳали бирорта диплом қўшилмаган',  'Пока не добавлено ни одного диплома');
PERFORM _seed_msg('label',   'Scholarship category',                                     'Stipendiya toifasi',              'Стипендия тоифаси',               'Категория стипендии');
PERFORM _seed_msg('label',   'Scholarship type',                                         'Stipendiya turi',                 'Стипендия тури',                  'Тип стипендии');
PERFORM _seed_msg('label',   'Payment form',                                             'To''lov shakli',                  'Тўлов шакли',                     'Форма оплаты');
PERFORM _seed_msg('label',   'Decree',                                                   'Buyruq',                          'Буйруқ',                          'Приказ');
PERFORM _seed_msg('label',   'Start date',                                               'Boshlanish sanasi',               'Бошланиш санаси',                 'Дата начала');
PERFORM _seed_msg('label',   'End date',                                                 'Tugash sanasi',                   'Тугаш санаси',                    'Дата окончания');
PERFORM _seed_msg('label',   'Semester',                                                 'Semestr',                         'Семестр',                         'Семестр');
PERFORM _seed_msg('label',   'Monthly amounts',                                          'Oylik miqdorlar',                 'Ойлик миқдорлар',                 'Ежемесячные суммы');
PERFORM _seed_msg('label',   'Amount',                                                   'Miqdor',                          'Миқдор',                          'Сумма');
PERFORM _seed_msg('message', 'No scholarships have been added yet',                      'Hali birorta stipendiya qo''shilmagan','Ҳали бирорта стипендия қўшилмаган','Пока не добавлено ни одной стипендии');
PERFORM _seed_msg('label',   'Certificate type',                                         'Sertifikat turi',                 'Сертификат тури',                 'Тип сертификата');
PERFORM _seed_msg('label',   'Certificate name',                                         'Sertifikat nomi',                 'Сертификат номи',                 'Название сертификата');
PERFORM _seed_msg('label',   'Certificate grade',                                        'Sertifikat darajasi',             'Сертификат даражаси',             'Уровень сертификата');
PERFORM _seed_msg('label',   'Certificate subject',                                      'Sertifikat fani',                 'Сертификат фани',                 'Предмет сертификата');
PERFORM _seed_msg('label',   'Serial number',                                            'Seriya raqami',                   'Серия рақами',                    'Серийный номер');
PERFORM _seed_msg('label',   'Issue date',                                               'Berilgan sana',                   'Берилган сана',                   'Дата выдачи');
PERFORM _seed_msg('label',   'Valid until',                                              'Amal qilish muddati',             'Амал қилиш муддати',              'Действителен до');
PERFORM _seed_msg('message', 'No certificates have been added yet',                      'Hali birorta sertifikat qo''shilmagan','Ҳали бирорта сертификат қўшилмаган','Пока не добавлено ни одного сертификата');

-- Science registry cards — Researchers, Scientific projects, Scientific publications, Methodical publications
-- NOTE: 'Scientific projects' and 'Scientific publications' are already seeded by S006 — not duplicated here.
PERFORM _seed_msg('label',   'Researchers',                                              'Tadqiqotchilar',                  'Тадқиқотчилар',                   'Исследователи');
PERFORM _seed_msg('label',   'Methodical publications',                                  'Metodik nashrlar',                'Методик нашрлар',                 'Методические публикации');
PERFORM _seed_msg('label',   'Full name',                                                'F.I.Sh.',                         'Ф.И.Ш.',                          'Ф.И.О.');
PERFORM _seed_msg('label',   'Student ID number',                                        'Talaba ID raqami',                'Талаба ID рақами',                'ID номер студента');
PERFORM _seed_msg('label',   'Science branch',                                           'Fan tarmog''i',                   'Фан тармоғи',                     'Отрасль науки');
PERFORM _seed_msg('label',   'Dissertation theme',                                       'Dissertatsiya mavzusi',           'Диссертация мавзуси',             'Тема диссертации');
PERFORM _seed_msg('label',   'Doctoral student type',                                    'Doktorant turi',                  'Докторант тури',                  'Тип докторанта');
PERFORM _seed_msg('label',   'Accepted date',                                            'Qabul sanasi',                    'Қабул санаси',                    'Дата приёма');
PERFORM _seed_msg('message', 'No researchers have been added yet',                       'Hali birorta tadqiqotchi qo''shilmagan','Ҳали бирорта тадқиқотчи қўшилмаган','Пока не добавлено ни одного исследователя');
PERFORM _seed_msg('label',   'Project number',                                           'Loyiha raqami',                   'Лойиҳа рақами',                   'Номер проекта');
PERFORM _seed_msg('label',   'Project type',                                             'Loyiha turi',                     'Лойиҳа тури',                     'Тип проекта');
PERFORM _seed_msg('label',   'Contract number',                                          'Shartnoma raqami',                'Шартнома рақами',                 'Номер договора');
PERFORM _seed_msg('label',   'Contract date',                                            'Shartnoma sanasi',                'Шартнома санаси',                 'Дата договора');
PERFORM _seed_msg('message', 'No scientific projects have been added yet',               'Hali birorta ilmiy loyiha qo''shilmagan','Ҳали бирорта илмий лойиҳа қўшилмаган','Пока не добавлено ни одного научного проекта');
PERFORM _seed_msg('label',   'Authors',                                                  'Mualliflar',                      'Муаллифлар',                      'Авторы');
PERFORM _seed_msg('label',   'Author count',                                             'Mualliflar soni',                 'Муаллифлар сони',                 'Количество авторов');
PERFORM _seed_msg('label',   'Source',                                                   'Manba',                           'Манба',                           'Источник');
PERFORM _seed_msg('label',   'Issue year',                                               'Nashr yili',                      'Нашр йили',                       'Год издания');
PERFORM _seed_msg('label',   'Publication type',                                         'Nashr turi',                      'Нашр тури',                       'Тип публикации');
PERFORM _seed_msg('message', 'No publications have been added yet',                      'Hali birorta nashr qo''shilmagan','Ҳали бирорта нашр қўшилмаган',    'Пока не добавлено ни одной публикации');
PERFORM _seed_msg('label',   'Publisher',                                                'Nashriyot',                       'Нашриёт',                         'Издательство');
PERFORM _seed_msg('message', 'No methodical works have been added yet',                  'Hali birorta metodik nashr qo''shilmagan','Ҳали бирорта методик нашр қўшилмаган','Пока не добавлено ни одной методической публикации');

-- Analytics report cards — KPIs, block titles, columns, filters
-- NOTE: report menu titles (Students/Teachers/Institutions/Academic/Scientific/Economic report)
--       and 'By education type','By region','Education year' already seeded above — reused, not duplicated.
-- Students report
PERFORM _seed_msg('label',   'Total students',                                           'Jami talabalar',                  'Жами талабалар',                  'Всего студентов');
PERFORM _seed_msg('label',   'Grant',                                                    'Grant',                           'Грант',                           'Грант');
PERFORM _seed_msg('label',   'Contract',                                                 'Kontrakt',                        'Контракт',                        'Контракт');
PERFORM _seed_msg('label',   'Male',                                                     'Erkak',                           'Эркак',                           'Мужской');
PERFORM _seed_msg('label',   'Female',                                                   'Ayol',                            'Аёл',                             'Женский');
PERFORM _seed_msg('label',   'By education form',                                        'Ta''lim shakli bo''yicha',        'Таълим шакли бўйича',             'По форме образования');
PERFORM _seed_msg('label',   'By gender',                                                'Jinsi bo''yicha',                 'Жинси бўйича',                    'По полу');
PERFORM _seed_msg('label',   'By payment form',                                          'To''lov shakli bo''yicha',        'Тўлов шакли бўйича',              'По форме оплаты');
PERFORM _seed_msg('label',   'Top universities',                                         'Yetakchi OTMlar',                 'Етакчи ОТМлар',                   'Ведущие вузы');
PERFORM _seed_msg('label',   'Students count',                                           'Talabalar soni',                  'Талабалар сони',                  'Количество студентов');
-- Institutions report
PERFORM _seed_msg('label',   'Total institutions',                                       'Jami muassasalar',                'Жами муассасалар',                'Всего учреждений');
PERFORM _seed_msg('label',   'Faculties',                                                'Fakultetlar',                     'Факультетлар',                    'Факультеты');
PERFORM _seed_msg('label',   'Cathedras',                                                'Kafedralar',                      'Кафедралар',                      'Кафедры');
PERFORM _seed_msg('label',   'By ownership',                                             'Mulkchilik bo''yicha',            'Мулкчилик бўйича',                'По собственности');
PERFORM _seed_msg('label',   'By university type',                                       'OTM turi bo''yicha',              'ОТМ тури бўйича',                 'По типу вуза');
PERFORM _seed_msg('label',   'University structure',                                     'OTM tuzilmasi',                   'ОТМ тузилмаси',                   'Структура вуза');
-- Scientific report
PERFORM _seed_msg('label',   'Total publications',                                       'Jami nashrlar',                   'Жами нашрлар',                    'Всего публикаций');
PERFORM _seed_msg('label',   'Total projects',                                           'Jami loyihalar',                  'Жами лойиҳалар',                  'Всего проектов');
PERFORM _seed_msg('label',   'Doctoral students',                                        'Doktorantlar',                    'Докторантлар',                    'Докторанты');
PERFORM _seed_msg('label',   'Publications by type',                                     'Nashrlar turi bo''yicha',         'Нашрлар тури бўйича',             'Публикации по типу');
PERFORM _seed_msg('label',   'Publications by university',                               'Nashrlar OTM bo''yicha',          'Нашрлар ОТМ бўйича',              'Публикации по вузам');
PERFORM _seed_msg('label',   'Projects by type',                                         'Loyihalar turi bo''yicha',        'Лойиҳалар тури бўйича',           'Проекты по типу');
PERFORM _seed_msg('label',   'Projects by university',                                   'Loyihalar OTM bo''yicha',         'Лойиҳалар ОТМ бўйича',            'Проекты по вузам');
PERFORM _seed_msg('label',   'Publications',                                             'Nashrlar',                        'Нашрлар',                         'Публикации');
PERFORM _seed_msg('label',   'Projects',                                                 'Loyihalar',                       'Лойиҳалар',                       'Проекты');
-- Teachers report
PERFORM _seed_msg('label',   'Total teachers',                                           'Jami o''qituvchilar',             'Жами ўқитувчилар',                'Всего преподавателей');
PERFORM _seed_msg('label',   'PhD holders',                                              'Ilmiy darajaga egalar',           'Илмий даражага эгалар',           'Со степенью');
PERFORM _seed_msg('label',   'Professors',                                               'Professorlar',                    'Профессорлар',                    'Профессора');
PERFORM _seed_msg('label',   'By academic degree',                                       'Ilmiy daraja bo''yicha',          'Илмий даража бўйича',             'По учёной степени');
PERFORM _seed_msg('label',   'By academic rank',                                         'Ilmiy unvon bo''yicha',           'Илмий унвон бўйича',              'По учёному званию');
PERFORM _seed_msg('label',   'By age',                                                   'Yosh bo''yicha',                  'Ёш бўйича',                       'По возрасту');
PERFORM _seed_msg('label',   'By university',                                            'OTM bo''yicha',                   'ОТМ бўйича',                      'По вузам');
PERFORM _seed_msg('label',   'Teachers count',                                           'O''qituvchilar soni',             'Ўқитувчилар сони',                'Количество преподавателей');
-- Empty state
PERFORM _seed_msg('message', 'No report data available',                                 'Hisobot ma''lumotlari yo''q',     'Ҳисобот маълумотлари йўқ',        'Нет данных отчёта');

-- Rating cards — ranking tables, KPIs (menu titles + University/Publications/Projects/
--   Doctoral students/Top universities/Total publications/Total projects/By university reused above)
PERFORM _seed_msg('label',   'Rank',                                                     'O''rin',                          'Ўрин',                            'Место');
PERFORM _seed_msg('label',   'Total',                                                    'Jami',                            'Жами',                            'Итого');
PERFORM _seed_msg('label',   'Indicators',                                               'Ko''rsatkichlar',                 'Кўрсаткичлар',                    'Показатели');
PERFORM _seed_msg('label',   'Universities ranked',                                      'Reytingdagi OTMlar',              'Рейтингдаги ОТМлар',              'Вузов в рейтинге');
PERFORM _seed_msg('label',   'Top university',                                            'Yetakchi OTM',                    'Етакчи ОТМ',                      'Лидер рейтинга');
PERFORM _seed_msg('label',   'Average score',                                            'O''rtacha ball',                  'Ўртача балл',                     'Средний балл');
PERFORM _seed_msg('label',   'Average GPA',                                               'O''rtacha GPA',                   'Ўртача GPA',                      'Средний GPA');
PERFORM _seed_msg('label',   'Debtors',                                                   'Qarzdorlar',                      'Қарздорлар',                      'Должники');
PERFORM _seed_msg('label',   'Students counted',                                          'Hisobga olingan talabalar',       'Ҳисобга олинган талабалар',       'Учтено студентов');

-- New registry cards — Employee jobs, Institution specialities, Dissertation defense,
--   Publication property/intellectual, Research activity.
-- NOTE: 'Employee jobs' and 'Scientific activity' (menu) owned by S006; 'Decree number' by S009;
--       'Speciality code'/'Speciality name' by S006 — not duplicated here.
PERFORM _seed_msg('label',   'Institution specialities',                                 'OTM mutaxassisliklari',           'ОТМ мутахассисликлари',           'Специальности вуза');
PERFORM _seed_msg('label',   'Dissertation defense',                                     'Dissertatsiya himoyasi',          'Диссертация ҳимояси',             'Защита диссертации');
PERFORM _seed_msg('label',   'Employee',                                                 'Xodim',                           'Ходим',                           'Сотрудник');
PERFORM _seed_msg('label',   'Employee type',                                            'Xodim turi',                      'Ходим тури',                      'Тип сотрудника');
PERFORM _seed_msg('label',   'Employee form',                                            'Shtat shakli',                    'Штат шакли',                      'Форма штата');
PERFORM _seed_msg('label',   'Job start date',                                           'Ish boshlangan sana',             'Иш бошланган сана',               'Дата начала работы');
PERFORM _seed_msg('label',   'Job end date',                                             'Ish tugagan sana',                'Иш тугаган сана',                 'Дата окончания работы');
PERFORM _seed_msg('label',   'Defense date',                                             'Himoya sanasi',                   'Ҳимоя санаси',                    'Дата защиты');
PERFORM _seed_msg('label',   'Defense place',                                            'Himoya joyi',                     'Ҳимоя жойи',                      'Место защиты');
PERFORM _seed_msg('label',   'Patent type',                                              'Patent turi',                     'Патент тури',                     'Тип патента');
PERFORM _seed_msg('label',   'Property date',                                             'Ro''yxatga olingan sana',         'Рўйхатга олинган сана',           'Дата регистрации');
PERFORM _seed_msg('label',   'Number',                                                   'Raqam',                           'Рақам',                           'Номер');
PERFORM _seed_msg('label',   'H-index',                                                  'H-indeks',                        'H-индекс',                        'H-индекс');
PERFORM _seed_msg('label',   'Scientific work count',                                    'Ilmiy ishlar soni',              'Илмий ишлар сони',                'Количество научных работ');
PERFORM _seed_msg('label',   'Reference count',                                          'Iqtiboslar soni',                'Иқтибослар сони',                 'Количество цитирований');
PERFORM _seed_msg('label',   'Scholar database',                                         'Ilmiy baza',                      'Илмий база',                      'Научная база');
PERFORM _seed_msg('label',   'Link',                                                     'Havola',                          'Ҳавола',                          'Ссылка');
PERFORM _seed_msg('message', 'No records have been added yet',                           'Hali birorta yozuv qo''shilmagan','Ҳали бирорта ёзув қўшилмаган',    'Пока не добавлено ни одной записи');

-- Academic + Economic reports — KPIs, chart titles, table headers.
-- NOTE: 'Average score','Debtors','University','By education type','By gender','Total',
--   'Top universities','Academic report','Economic report' reused (seeded above) — not duplicated.
PERFORM _seed_msg('label',   'Average attendance',                                       'O''rtacha davomat',               'Ўртача давомат',                  'Средняя посещаемость');
PERFORM _seed_msg('label',   'Universities covered',                                     'Qamrab olingan OTMlar',           'Қамраб олинган ОТМлар',           'Охваченные вузы');
PERFORM _seed_msg('label',   'Top universities by average score',                        'O''rtacha ball bo''yicha yetakchi OTMlar','Ўртача балл бўйича етакчи ОТМлар','Ведущие вузы по среднему баллу');
PERFORM _seed_msg('label',   'Per-university academic performance',                      'OTM bo''yicha o''zlashtirish',    'ОТМ бўйича ўзлаштириш',           'Успеваемость по вузам');
PERFORM _seed_msg('label',   'Absentee students',                                        'Davomati past talabalar',         'Давомати паст талабалар',         'Студенты с пропусками');
PERFORM _seed_msg('label',   'Total graduates',                                          'Jami bitiruvchilar',              'Жами битирувчилар',               'Всего выпускников');
PERFORM _seed_msg('label',   'Laboratories',                                             'Laboratoriyalar',                 'Лабораториялар',                  'Лаборатории');
PERFORM _seed_msg('label',   'ICT equipment',                                            'AKT jihozlari',                   'АКТ жиҳозлари',                   'ИКТ оборудование');
PERFORM _seed_msg('label',   'Graduates by year',                                        'Yillar bo''yicha bitiruvchilar',  'Йиллар бўйича битирувчилар',      'Выпускники по годам');
PERFORM _seed_msg('label',   'By workplace compatibility',                               'Ish joyi mosligi bo''yicha',      'Иш жойи мослиги бўйича',          'По соответствию месту работы');
PERFORM _seed_msg('label',   'Top universities by graduate count',                       'Bitiruvchilar soni bo''yicha yetakchi OTMlar','Битирувчилар сони бўйича етакчи ОТМлар','Ведущие вузы по числу выпускников');
PERFORM _seed_msg('label',   'Laboratories by university',                               'OTM bo''yicha laboratoriyalar',   'ОТМ бўйича лабораториялар',       'Лаборатории по вузам');

-- ──────────────────────────────────────────────────────
-- WAVE 4: hemis-front audit (2026-07-18) — missing t() keys
--   classifiers · science · webhooks · outbox · system · institutions
-- ──────────────────────────────────────────────────────
-- label
PERFORM _seed_msg('label', 'Education level', 'Ta''lim darajasi', 'Таълим даражаси', 'Уровень образования');
PERFORM _seed_msg('label', 'Hierarchy level', 'Ierarxiya darajasi', 'Иерархия даражаси', 'Уровень иерархии');
PERFORM _seed_msg('label', 'Level', 'Daraja', 'Даража', 'Уровень');
PERFORM _seed_msg('label', 'List', 'Ro''yxat', 'Рўйхат', 'Список');
PERFORM _seed_msg('label', 'Review status', 'Ko''rib chiqish holati', 'Кўриб чиқиш ҳолати', 'Статус проверки');
PERFORM _seed_msg('label', 'Search by name or code', 'Nomi yoki kodi bo''yicha qidirish', 'Номи ёки коди бўйича қидириш', 'Поиск по названию или коду');
PERFORM _seed_msg('label', 'Speciality classifier', 'Mutaxassislik klassifikatori', 'Мутахассислик классификатори', 'Классификатор специальностей');
PERFORM _seed_msg('label', 'Sub-specialities', 'Ichki mutaxassisliklar', 'Ички мутахассисликлар', 'Подспециальности');
PERFORM _seed_msg('label', 'Sync events', 'Sinxronizatsiya hodisalari', 'Синхронизация ҳодисалари', 'События синхронизации');
PERFORM _seed_msg('label', 'Tree', 'Daraxt', 'Дарахт', 'Дерево');
PERFORM _seed_msg('label', 'Unified bachelor and master speciality classifier', 'Bakalavr va magistr mutaxassisliklari yagona klassifikatori', 'Бакалавр ва магистр мутахассисликлари ягона классификатори', 'Единый классификатор специальностей бакалавриата и магистратуры');
PERFORM _seed_msg('label', 'Additional information', 'Qo''shimcha ma''lumot', 'Қўшимча маълумот', 'Дополнительная информация');
PERFORM _seed_msg('label', 'Batch number', 'Partiya raqami', 'Партия рақами', 'Номер партии');
PERFORM _seed_msg('label', 'Faculty', 'Fakultet', 'Факультет', 'Факультет');
PERFORM _seed_msg('label', 'Issued date', 'Berilgan sana', 'Берилган сана', 'Дата выдачи');
PERFORM _seed_msg('label', 'Status reason', 'Holat sababi', 'Ҳолат сабаби', 'Причина статуса');
PERFORM _seed_msg('label', 'Approved date', 'Tasdiqlangan sana', 'Тасдиқланган сана', 'Дата утверждения');
PERFORM _seed_msg('label', 'Country', 'Davlat', 'Давлат', 'Страна');
PERFORM _seed_msg('label', 'Currency', 'Valyuta', 'Валюта', 'Валюта');
PERFORM _seed_msg('label', 'Diploma given by whom', 'Diplom kim tomonidan berilgan', 'Диплом ким томонидан берилган', 'Кем выдан диплом');
PERFORM _seed_msg('label', 'Diploma given date', 'Diplom berilgan sana', 'Диплом берилган сана', 'Дата выдачи диплома');
PERFORM _seed_msg('label', 'Keywords', 'Kalit so''zlar', 'Калит сўзлар', 'Ключевые слова');
PERFORM _seed_msg('label', 'Locality', 'Aholi punkti', 'Аҳоли пункти', 'Населённый пункт');
PERFORM _seed_msg('label', 'Methodical', 'Uslubiy', 'Услубий', 'Методический');
PERFORM _seed_msg('label', 'Parameter', 'Parametr', 'Параметр', 'Параметр');
PERFORM _seed_msg('label', 'Publication', 'Nashr', 'Нашр', 'Публикация');
PERFORM _seed_msg('label', 'Publication database', 'Nashr bazasi', 'Нашр базаси', 'База публикаций');
PERFORM _seed_msg('label', 'Research activity', 'Tadqiqot faoliyati', 'Тадқиқот фаолияти', 'Научная деятельность');
PERFORM _seed_msg('label', 'Researcher', 'Tadqiqotchi', 'Тадқиқотчи', 'Исследователь');
PERFORM _seed_msg('label', 'Scientific project', 'Ilmiy loyiha', 'Илмий лойиҳа', 'Научный проект');
PERFORM _seed_msg('label', 'Student name', 'Talaba ismi', 'Талаба исми', 'Имя студента');
PERFORM _seed_msg('label', 'Certificate', 'Sertifikat', 'Сертификат', 'Сертификат');
PERFORM _seed_msg('label', 'Month', 'Oy', 'Ой', 'Месяц');
PERFORM _seed_msg('label', 'Aggregate', 'Agregat', 'Агрегат', 'Агрегат');
PERFORM _seed_msg('label', 'Aggregate ID', 'Agregat ID', 'Агрегат ID', 'ID агрегата');
PERFORM _seed_msg('label', 'Aggregate type', 'Agregat turi', 'Агрегат тури', 'Тип агрегата');
PERFORM _seed_msg('label', 'All aggregates', 'Barcha agregatlar', 'Барча агрегатлар', 'Все агрегаты');
PERFORM _seed_msg('label', 'All entities', 'Barcha obyektlar', 'Барча объектлар', 'Все сущности');
PERFORM _seed_msg('label', 'Attempt', 'Urinish', 'Уриниш', 'Попытка');
PERFORM _seed_msg('label', 'Callback URL', 'Callback URL', 'Callback URL', 'URL обратного вызова');
PERFORM _seed_msg('label', 'Causation ID', 'Sabab ID', 'Сабаб ID', 'ID причины');
PERFORM _seed_msg('label', 'Correlation ID', 'Korrelyatsiya ID', 'Корреляция ID', 'ID корреляции');
PERFORM _seed_msg('label', 'Delivery log', 'Yetkazish jurnali', 'Етказиш журнали', 'Журнал доставки');
PERFORM _seed_msg('label', 'Duration', 'Davomiylik', 'Давомийлик', 'Длительность');
PERFORM _seed_msg('label', 'Employee sync from Univer', 'Univerdan xodim sinxronizatsiyasi', 'Univerдан ходим синхронизацияси', 'Синхронизация сотрудников из Univer');
PERFORM _seed_msg('label', 'Last error', 'Oxirgi xato', 'Охирги хато', 'Последняя ошибка');
PERFORM _seed_msg('label', 'Max retries', 'Maksimal urinishlar', 'Максимал уринишлар', 'Макс. попыток');
PERFORM _seed_msg('label', 'OTM Code', 'OTM kodi', 'ОТМ коди', 'Код вуза');
PERFORM _seed_msg('label', 'Occurred', 'Yuz berdi', 'Юз берди', 'Произошло');
PERFORM _seed_msg('label', 'Oldest pending', 'Eng eski kutilayotgan', 'Энг эски кутилаётган', 'Самое старое в ожидании');
PERFORM _seed_msg('label', 'Outbox event', 'Outbox hodisasi', 'Outbox ҳодисаси', 'Событие outbox');
PERFORM _seed_msg('label', 'Payload', 'Payload', 'Payload', 'Полезная нагрузка');
PERFORM _seed_msg('label', 'Quick filters', 'Tezkor filtrlar', 'Тезкор филтрлар', 'Быстрые фильтры');
PERFORM _seed_msg('label', 'Reason (optional)', 'Sabab (ixtiyoriy)', 'Сабаб (ихтиёрий)', 'Причина (необязательно)');
PERFORM _seed_msg('label', 'Retries', 'Urinishlar', 'Уринишлар', 'Попытки');
PERFORM _seed_msg('label', 'Routing', 'Marshrutlash', 'Маршрутлаш', 'Маршрутизация');
PERFORM _seed_msg('label', 'Schema version', 'Sxema versiyasi', 'Схема версияси', 'Версия схемы');
PERFORM _seed_msg('label', 'Search by university code or URL...', 'OTM kodi yoki URL bo''yicha qidirish...', 'ОТМ коди ёки URL бўйича қидириш...', 'Поиск по коду вуза или URL...');
PERFORM _seed_msg('label', 'Timeline', 'Vaqt jadvali', 'Вақт жадвали', 'Хронология');
PERFORM _seed_msg('label', 'Timeout', 'Timeout', 'Timeout', 'Таймаут');
PERFORM _seed_msg('label', 'Timeout (ms)', 'Timeout (ms)', 'Timeout (ms)', 'Таймаут (мс)');
PERFORM _seed_msg('label', 'Topic', 'Mavzu', 'Мавзу', 'Топик');
PERFORM _seed_msg('label', 'Total targets', 'Jami manzillar', 'Жами манзиллар', 'Всего целей');
PERFORM _seed_msg('label', 'Webhook secret — copy now', 'Webhook siri — hozir nusxalang', 'Webhook сири — ҳозир нусхаланг', 'Секрет webhook — скопируйте сейчас');
PERFORM _seed_msg('label', 'menu, button...', 'menyu, tugma...', 'меню, тугма...', 'меню, кнопка...');
PERFORM _seed_msg('label', 'min', 'daq', 'дақ', 'мин');
PERFORM _seed_msg('label', 'Decree date', 'Buyruq sanasi', 'Буйруқ санаси', 'Дата приказа');
PERFORM _seed_msg('label', 'Rate', 'Stavka', 'Ставка', 'Ставка');
PERFORM _seed_msg('label', 'sq.m.', 'kv.m', 'кв.м', 'кв.м');
-- {{count}} found — plural (noun-less, invariant); base + CLDR suffixes
PERFORM _seed_msg('label', '{{count}} found', '{{count}} found', '{{count}} ta topildi', '{{count}} та топилди', 'Найдено: {{count}}');
PERFORM _seed_msg('label', '{{count}} found_one', '{{count}} found', '{{count}} ta topildi', '{{count}} та топилди', 'Найдено: {{count}}');
PERFORM _seed_msg('label', '{{count}} found_few', '{{count}} found', '{{count}} ta topildi', '{{count}} та топилди', 'Найдено: {{count}}');
PERFORM _seed_msg('label', '{{count}} found_many', '{{count}} found', '{{count}} ta topildi', '{{count}} та топилди', 'Найдено: {{count}}');
PERFORM _seed_msg('label', '{{count}} found_other', '{{count}} found', '{{count}} ta topildi', '{{count}} та топилди', 'Найдено: {{count}}');
-- message
PERFORM _seed_msg('message', 'Successfully created', 'Muvaffaqiyatli yaratildi', 'Муваффақиятли яратилди', 'Успешно создано');
PERFORM _seed_msg('message', 'Outbox event discarded', 'Outbox hodisasi bekor qilindi', 'Outbox ҳодисаси бекор қилинди', 'Событие outbox отклонено');
PERFORM _seed_msg('message', 'Outbox event re-queued for publish', 'Outbox hodisasi nashr uchun qayta navbatga qo''yildi', 'Outbox ҳодисаси нашр учун қайта навбатга қўйилди', 'Событие outbox повторно поставлено в очередь на публикацию');
PERFORM _seed_msg('message', 'Saved successfully', 'Muvaffaqiyatli saqlandi', 'Муваффақиятли сақланди', 'Успешно сохранено');
PERFORM _seed_msg('message', 'files created', 'fayl yaratildi', 'файл яратилди', 'файлов создано');
PERFORM _seed_msg('message', 'Secret regenerated — update Univer .env immediately', 'Sir qayta yaratildi — Univer .env''ni darhol yangilang', 'Сир қайта яратилди — Univer .env''ни дарҳол янгиланг', 'Секрет перевыпущен — немедленно обновите Univer .env');
PERFORM _seed_msg('message', 'Test event dispatched', 'Test hodisasi yuborildi', 'Тест ҳодисаси юборилди', 'Тестовое событие отправлено');
PERFORM _seed_msg('message', 'Webhook target created — save the plain secret!', 'Webhook manzili yaratildi — ochiq sirni saqlang!', 'Webhook манзили яратилди — очиқ сирни сақланг!', 'Цель webhook создана — сохраните открытый секрет!');
PERFORM _seed_msg('message', 'Webhook target deleted', 'Webhook manzili o''chirildi', 'Webhook манзили ўчирилди', 'Цель webhook удалена');
PERFORM _seed_msg('message', 'Webhook target updated', 'Webhook manzili yangilandi', 'Webhook манзили янгиланди', 'Цель webhook обновлена');
PERFORM _seed_msg('message', 'No data', 'Ma''lumot yo''q', 'Маълумот йўқ', 'Нет данных');
PERFORM _seed_msg('message', 'View sync events for classifiers (outbox queue)', 'Klassifikatorlar uchun sinxronizatsiya hodisalarini ko''rish (outbox navbati)', 'Классификаторлар учун синхронизация ҳодисаларини кўриш (outbox навбати)', 'Просмотр событий синхронизации классификаторов (очередь outbox)');
PERFORM _seed_msg('message', 'CSV file downloaded', 'CSV fayl yuklab olindi', 'CSV файл юклаб олинди', 'CSV-файл загружен');
PERFORM _seed_msg('message', 'Callback URL is derived from hemishe_e_university.student_url — set it in the university registry first.', 'Callback URL hemishe_e_university.student_url''dan olinadi — avval universitet registrida o''rnating.', 'Callback URL hemishe_e_university.student_url''дан олинади — аввал университет регистрида ўрнатинг.', 'Callback URL берётся из hemishe_e_university.student_url — сначала задайте его в реестре вузов.');
PERFORM _seed_msg('message', 'Event not found', 'Hodisa topilmadi', 'Ҳодиса топилмади', 'Событие не найдено');
PERFORM _seed_msg('message', 'Event will be marked published_at=now without sending to Kafka. Use for poison pills or obsolete events.', 'Hodisa Kafka''ga yuborilmasdan published_at=now deb belgilanadi. Zararli yoki eskirgan hodisalar uchun ishlating.', 'Ҳодиса Kafka''га юборилмасдан published_at=now деб белгиланади. Зарарли ёки эскирган ҳодисалар учун ишлатинг.', 'Событие будет помечено published_at=now без отправки в Kafka. Используйте для «отравленных» или устаревших событий.');
PERFORM _seed_msg('message', 'Inspect pending events, retry failures, discard poison pills.', 'Kutilayotgan hodisalarni tekshiring, xatolarni qayta urinib ko''ring, zararlilarini bekor qiling.', 'Кутилаётган ҳодисаларни текширинг, хатоларни қайта уриниб кўринг, зарарлиларини бекор қилинг.', 'Проверяйте ожидающие события, повторяйте неудачные, отклоняйте «отравленные».');
PERFORM _seed_msg('message', 'Manage 224 OTM Univer webhook URLs, secrets and delivery logs', '224 OTM Univer webhook URL''lari, sirlari va yetkazish jurnallarini boshqaring', '224 ОТМ Univer webhook URL''лари, сирлари ва етказиш журналларини бошқаринг', 'Управление webhook-URL, секретами и журналами доставки 224 вузов Univer');
PERFORM _seed_msg('message', 'No delivery attempts yet', 'Hali yetkazish urinishlari yo''q', 'Ҳали етказиш уринишлари йўқ', 'Пока нет попыток доставки');
PERFORM _seed_msg('message', 'No outbox events match the current filters', 'Joriy filtrlarga mos outbox hodisalari yo''q', 'Жорий филтрларга мос outbox ҳодисалари йўқ', 'Нет событий outbox по текущим фильтрам');
PERFORM _seed_msg('message', 'No webhook targets configured yet', 'Hali webhook manzillari sozlanmagan', 'Ҳали webhook манзиллари созланмаган', 'Цели webhook ещё не настроены');
PERFORM _seed_msg('message', 'No webhook targets found for the search', 'Qidiruv bo''yicha webhook manzillari topilmadi', 'Қидирув бўйича webhook манзиллари топилмади', 'По поиску цели webhook не найдены');
PERFORM _seed_msg('message', 'OTM code and callback URL cannot be changed here.', 'OTM kodi va callback URL bu yerda o''zgartirilmaydi.', 'ОТМ коди ва callback URL бу ерда ўзгартирилмайди.', 'Код вуза и callback URL здесь изменить нельзя.');
PERFORM _seed_msg('message', 'OTM {{code}} will stop receiving webhooks. Soft delete — restorable.', 'OTM {{code}} webhooklarni qabul qilishni to''xtatadi. Yumshoq o''chirish — tiklanadi.', 'ОТМ {{code}} webhookларни қабул қилишни тўхтатади. Юмшоқ ўчириш — тикланади.', 'Вуз {{code}} перестанет получать webhook. Мягкое удаление — восстановимо.');
PERFORM _seed_msg('message', 'OTM {{code}} — this plain secret is shown only once. Save it to Univer .env as HEMIS_WEBHOOK_SECRET.', 'OTM {{code}} — bu ochiq sir faqat bir marta ko''rsatiladi. Uni Univer .env''ga HEMIS_WEBHOOK_SECRET sifatida saqlang.', 'ОТМ {{code}} — бу очиқ сир фақат бир марта кўрсатилади. Уни Univer .env''га HEMIS_WEBHOOK_SECRET сифатида сақланг.', 'Вуз {{code}} — этот открытый секрет показывается только один раз. Сохраните его в Univer .env как HEMIS_WEBHOOK_SECRET.');
PERFORM _seed_msg('message', 'Old secret will be invalidated immediately. New secret must be deployed to Univer .env before next event.', 'Eski sir darhol bekor qilinadi. Yangi sir keyingi hodisadan oldin Univer .env''ga joylanishi kerak.', 'Эски сир дарҳол бекор қилинади. Янги сир кейинги ҳодисадан олдин Univer .env''га жойланиши керак.', 'Старый секрет будет аннулирован немедленно. Новый секрет нужно развернуть в Univer .env до следующего события.');
PERFORM _seed_msg('message', 'Retry count will be reset to 0 so the next OutboxPoller cycle will pick it up and try Kafka publish again.', 'Urinishlar soni 0 ga tushiriladi, shunda keyingi OutboxPoller sikli uni olib Kafka''ga qayta yuborishga urinadi.', 'Уринишлар сони 0 га туширилади, шунда кейинги OutboxPoller сикли уни олиб Kafka''га қайта юборишга уринади.', 'Счётчик попыток сбросится в 0, чтобы следующий цикл OutboxPoller снова опубликовал в Kafka.');
PERFORM _seed_msg('message', 'duplicate groups found', 'dublikat guruh topildi', 'дубликат гуруҳ топилди', 'дублирующих групп найдено');
PERFORM _seed_msg('message', 'translations with same text but different keys', 'matni bir xil, lekin kaliti har xil tarjimalar', 'матни бир хил, лекин калити ҳар хил таржималар', 'переводы с одинаковым текстом, но разными ключами');
-- action
PERFORM _seed_msg('action', 'Export to CSV', 'CSV''ga eksport', 'CSV''га экспорт', 'Экспорт в CSV');
PERFORM _seed_msg('action', 'Add Webhook Target', 'Webhook manzili qo''shish', 'Webhook манзили қўшиш', 'Добавить цель webhook');
PERFORM _seed_msg('action', 'Discard', 'Bekor qilish', 'Бекор қилиш', 'Отклонить');
PERFORM _seed_msg('action', 'Edit Webhook Target', 'Webhook manzilini tahrirlash', 'Webhook манзилини таҳрирлаш', 'Изменить цель webhook');
PERFORM _seed_msg('action', 'I saved it — close', 'Saqladim — yopish', 'Сақладим — ёпиш', 'Я сохранил — закрыть');
PERFORM _seed_msg('action', 'Inspect', 'Tekshirish', 'Текшириш', 'Проверить');
PERFORM _seed_msg('action', 'Prev', 'Oldingi', 'Олдинги', 'Назад');
PERFORM _seed_msg('action', 'Re-queue', 'Qayta navbatga', 'Қайта навбатга', 'В очередь повторно');
PERFORM _seed_msg('action', 'Regenerate secret', 'Sirni qayta yaratish', 'Сирни қайта яратиш', 'Перевыпустить секрет');
PERFORM _seed_msg('action', 'Send test event', 'Test hodisasi yuborish', 'Тест ҳодисаси юбориш', 'Отправить тестовое событие');
-- confirm
PERFORM _seed_msg('confirm', 'Are you sure you want to delete role "{{name}}"? This action cannot be undone.', '"{{name}}" rolini ochirishga ishonchingiz komilmi? Bu amalni bekor qilib bolmaydi.', '"{{name}}" ролини ўчиришга ишончингиз комилми? Бу амални бекор қилиб бўлмайди.', 'Вы уверены, что хотите удалить роль "{{name}}"? Это действие необратимо.');
PERFORM _seed_msg('confirm', 'Are you sure you want to delete user "{{username}}"? This action cannot be undone.', '"{{username}}" foydalanuvchisini ochirishga ishonchingiz komilmi? Bu amalni bekor qilib bolmaydi.', '"{{username}}" фойдаланувчисини ўчиришга ишончингиз комилми? Бу амални бекор қилиб бўлмайди.', 'Вы уверены, что хотите удалить пользователя "{{username}}"? Это действие необратимо.');
PERFORM _seed_msg('confirm', 'Are you sure you want to unlock user "{{username}}"?', '"{{username}}" foydalanuvchisini blokdan chiqarishga ishonchingiz komilmi?', '"{{username}}" фойдаланувчисини блокдан чиқаришга ишончингиз комилми?', 'Вы уверены, что хотите разблокировать пользователя "{{username}}"?');
PERFORM _seed_msg('confirm', 'Are you sure you want to {{action}} user "{{username}}"?', '"{{username}}" foydalanuvchisini {{action}} qilishga ishonchingiz komilmi?', '"{{username}}" фойдаланувчисини {{action}} қилишга ишончингиз комилми?', 'Вы уверены, что хотите {{action}} пользователя "{{username}}"?');
PERFORM _seed_msg('confirm', 'Delete webhook target?', 'Webhook manzili o''chirilsinmi?', 'Webhook манзили ўчирилсинми?', 'Удалить цель webhook?');
PERFORM _seed_msg('confirm', 'Discard this event?', 'Bu hodisa bekor qilinsinmi?', 'Бу ҳодиса бекор қилинсинми?', 'Отклонить это событие?');
PERFORM _seed_msg('confirm', 'Re-queue this event?', 'Bu hodisa qayta navbatga qo''yilsinmi?', 'Бу ҳодиса қайта навбатга қўйилсинми?', 'Поставить событие в очередь повторно?');
PERFORM _seed_msg('confirm', 'Regenerate webhook secret?', 'Webhook siri qayta yaratilsinmi?', 'Webhook сири қайта яратилсинми?', 'Перевыпустить секрет webhook?');
PERFORM _seed_msg('confirm', 'Do you want to clear translations cache? This will force reload translations from backend.', 'Tarjimalar keshini tozalamoqchimisiz? Bu tarjimalarni backenddan qayta yuklashga majbur qiladi.', 'Таржималар кешини тозаламоқчимисиз? Бу таржималарни backendдан қайта юклашга мажбур қилади.', 'Очистить кэш переводов? Это принудительно перезагрузит переводы с бэкенда.');
PERFORM _seed_msg('confirm', 'Do you want to regenerate properties files for all languages? This may take a few seconds.', 'Barcha tillar uchun properties fayllarini qayta yaratmoqchimisiz? Bu bir necha soniya olishi mumkin.', 'Барча тиллар учун properties файлларини қайта яратмоқчимисиз? Бу бир неча сония олиши мумкин.', 'Перегенерировать properties-файлы для всех языков? Это может занять несколько секунд.');
-- status
PERFORM _seed_msg('status', 'Needs review', 'Ko''rib chiqish kerak', 'Кўриб чиқиш керак', 'Требует проверки');
PERFORM _seed_msg('status', 'Checked', 'Tekshirilgan', 'Текширилган', 'Проверено');
PERFORM _seed_msg('status', 'Applied', 'Qo''llanildi', 'Қўлланилди', 'Применено');
PERFORM _seed_msg('status', 'Apply failed', 'Qo''llash amalga oshmadi', 'Қўллаш амалга ошмади', 'Ошибка применения');
PERFORM _seed_msg('status', 'Dispatched', 'Yuborilgan', 'Юборилган', 'Отправлено');
PERFORM _seed_msg('status', 'Failed', 'Muvaffaqiyatsiz', 'Муваффақиятсиз', 'Ошибка');
PERFORM _seed_msg('status', 'No ack', 'Tasdiq yo''q', 'Тасдиқ йўқ', 'Нет подтверждения');
PERFORM _seed_msg('status', 'Not yet', 'Hali emas', 'Ҳали эмас', 'Ещё нет');
-- error
PERFORM _seed_msg('error', 'Failed to discard outbox event', 'Outbox hodisasini bekor qilib bo''lmadi', 'Outbox ҳодисасини бекор қилиб бўлмади', 'Не удалось отклонить событие outbox');
PERFORM _seed_msg('error', 'Failed to retry outbox event', 'Outbox hodisasini qayta urinib bo''lmadi', 'Outbox ҳодисасини қайта уриниб бўлмади', 'Не удалось повторить событие outbox');
PERFORM _seed_msg('error', 'Failed to create webhook target', 'Webhook manzilini yaratib bo''lmadi', 'Webhook манзилини яратиб бўлмади', 'Не удалось создать цель webhook');
PERFORM _seed_msg('error', 'Failed to delete webhook target', 'Webhook manzilini o''chirib bo''lmadi', 'Webhook манзилини ўчириб бўлмади', 'Не удалось удалить цель webhook');
PERFORM _seed_msg('error', 'Failed to dispatch test event', 'Test hodisasini yuborib bo''lmadi', 'Тест ҳодисасини юбориб бўлмади', 'Не удалось отправить тестовое событие');
PERFORM _seed_msg('error', 'Failed to regenerate secret', 'Sirni qayta yaratib bo''lmadi', 'Сирни қайта яратиб бўлмади', 'Не удалось перевыпустить секрет');
PERFORM _seed_msg('error', 'Failed to update webhook target', 'Webhook manzilini yangilab bo''lmadi', 'Webhook манзилини янгилаб бўлмади', 'Не удалось обновить цель webhook');
PERFORM _seed_msg('error', 'You do not have permission to view this page', 'Bu sahifani ko''rish uchun ruxsatingiz yo''q', 'Бу саҳифани кўриш учун рухсатингиз йўқ', 'У вас нет прав для просмотра этой страницы');
-- validation
PERFORM _seed_msg('validation', 'Description must be ≤ 255 chars', 'Tavsif ≤ 255 belgidan iborat bo''lishi kerak', 'Тавсиф ≤ 255 белгидан иборат бўлиши керак', 'Описание не более 255 символов');
PERFORM _seed_msg('validation', 'Max retries must be 0–10', 'Maksimal urinishlar 0–10 bo''lishi kerak', 'Максимал уринишлар 0–10 бўлиши керак', 'Макс. попыток должно быть 0–10');
PERFORM _seed_msg('validation', 'Timeout must be 1000–60000 ms', 'Timeout 1000–60000 ms bo''lishi kerak', 'Timeout 1000–60000 ms бўлиши керак', 'Таймаут должен быть 1000–60000 мс');
PERFORM _seed_msg('validation', 'University code must be 3–10 digits', 'Universitet kodi 3–10 raqamdan iborat bo''lishi kerak', 'Университет коди 3–10 рақамдан иборат бўлиши керак', 'Код вуза должен содержать 3–10 цифр');
-- auth
PERFORM _seed_msg('auth', 'We sent a password reset link to your email address. The link will expire in 15 minutes.', 'Parolni tiklash havolasini e-pochta manzilingizga yubordik. Havola 15 daqiqada tugaydi.', 'Паролни тиклаш ҳаволасини э-почта манзилингизга юбордик. Ҳавола 15 дақиқада тугайди.', 'Мы отправили ссылку для сброса пароля на вашу почту. Ссылка истечёт через 15 минут.');
-- pagination
PERFORM _seed_msg('pagination', 'Showing page {{cur}} of {{total}} ({{count}} total)', '{{total}} dan {{cur}}-sahifa ko''rsatilmoqda (jami {{count}})', '{{total}}дан {{cur}}-саҳифа кўрсатилмоқда (жами {{count}})', 'Страница {{cur}} из {{total}} (всего {{count}})');
-- acronyms (technical; not translated except currency)
PERFORM _seed_msg('label', 'DOI', 'DOI', 'DOI', 'DOI');
PERFORM _seed_msg('label', 'HTTP', 'HTTP', 'HTTP', 'HTTP');
PERFORM _seed_msg('label', 'DLQ', 'DLQ', 'DLQ', 'DLQ');
PERFORM _seed_msg('label', 'UZS', 'so''m', 'сўм', 'сум');

-- 'Years' — jadval sarlavhasi (ko'plik 'Yillar'); unit 'years' ('yil')dan farqli
PERFORM _seed_msg('label', 'Years', 'Yillar', 'Йиллар', 'Годы');
PERFORM _seed_msg('label', 'e.g. university full name', 'masalan: universitet to''liq nomi', 'масалан: университет тўлиқ номи', 'например: полное название вуза');

-- Speciality classifier — year-filter labels (2026-07)
PERFORM _seed_msg('label',  'Year',                                   'Yil',                             'Йил',                             'Год');
PERFORM _seed_msg('label',  'All years',                              'Barcha yillar',                   'Барча йиллар',                    'Все годы');

-- Speciality classifier — hierarchy-level taxonomy names (2026-07)
PERFORM _seed_msg('label',  'Field of knowledge',                     'Bilim sohasi',                    'Билим соҳаси',                    'Область знаний');
PERFORM _seed_msg('label',  'Field of education',                     'Ta''lim sohasi',                  'Таълим соҳаси',                   'Область образования');
PERFORM _seed_msg('label',  'Direction',                              'Yo''nalish',                      'Йўналиш',                         'Направление');
PERFORM _seed_msg('label',  'Sub-direction',                          'Ichki yo''nalish',                'Ички йўналиш',                    'Поднаправление');

-- Speciality classifier — manual create form (2026-07)
PERFORM _seed_msg('label',  'Add speciality',                         'Mutaxassislik qo''shish',          'Мутахассислик қўшиш',             'Добавить специальность');
PERFORM _seed_msg('label',  'Parent speciality',                      'Yuqori bo''lim',                   'Юқори бўлим',                     'Родительская специальность');
PERFORM _seed_msg('label',  'Top level',                              'Eng yuqori daraja',                'Энг юқори даража',                'Верхний уровень');
PERFORM _seed_msg('label',  'Search parent',                          'Yuqori bo''limni qidirish',        'Юқори бўлимни қидириш',           'Поиск родителя');
PERFORM _seed_msg('label',  'Speciality created',                     'Mutaxassislik qo''shildi',         'Мутахассислик қўшилди',           'Специальность добавлена');
PERFORM _seed_msg('label',  'Failed to create speciality',            'Mutaxassislik qo''shib bo''lmadi', 'Мутахассислик қўшиб бўлмади',      'Не удалось добавить специальность');
PERFORM _seed_msg('label',  'Refine your search to see more',         'Ko''proq ko''rish uchun qidiruvni aniqlashtiring', 'Кўпроқ кўриш учун қидирувни аниқлаштиринг', 'Уточните поиск, чтобы увидеть больше');

-- Speciality classifier — duplicate warning (2026-07)
PERFORM _seed_msg('label',  'This code already exists',               'Bu kod allaqachon mavjud',         'Бу код аллақачон мавжуд',         'Этот код уже существует');
PERFORM _seed_msg('label',  'This name already exists',               'Bu nom allaqachon mavjud',         'Бу ном аллақачон мавжуд',         'Это название уже существует');
PERFORM _seed_msg('label',  'This code and name already exist',       'Bu kod va nom allaqachon mavjud',  'Бу код ва ном аллақачон мавжуд',  'Этот код и название уже существуют');
PERFORM _seed_msg('label',  'Same parent',                            'Aynan shu bo''lim ostida',         'Айнан шу бўлим остида',           'В том же разделе');
PERFORM _seed_msg('label',  'You can still create it',                'Baribir qo''shishingiz mumkin',    'Барибир қўшишингиз мумкин',       'Всё равно можно добавить');
PERFORM _seed_msg('label',  'Cannot add a duplicate',                 'Dublikat qo''shib bo''lmaydi',     'Дубликат қўшиб бўлмайди',         'Нельзя добавить дубликат');
PERFORM _seed_msg('label',  'The year will be added to the existing entry', 'Yil mavjud yozuvga qo''shiladi', 'Йил мавжуд ёзувга қўшилади',  'Год будет добавлен к существующей записи');
PERFORM _seed_msg('label',  'Select years',                           'Yillarni tanlang',                 'Йилларни танланг',                'Выберите годы');

-- Speciality classifier — Excel export (2026-07)
PERFORM _seed_msg('label',  'Download Excel',                         'Excelga yuklab olish',             'Excelга юклаб олиш',              'Скачать Excel');
PERFORM _seed_msg('label',  'Export completed',                       'Eksport tayyor',                   'Экспорт тайёр',                   'Экспорт завершён');
PERFORM _seed_msg('label',  'Export failed',                          'Eksport amalga oshmadi',           'Экспорт амалга ошмади',           'Не удалось экспортировать');
PERFORM _seed_msg('label',  'Whole classifier',                       'Butun klassifikator',              'Бутун классификатор',             'Весь классификатор');
PERFORM _seed_msg('label',  'Current view',                           'Joriy ko''rinish',                 'Жорий кўриниш',                   'Текущий вид');
PERFORM _seed_msg('label',  'Parent code',                            'Ota kodi',                         'Ота коди',                        'Код родителя');
PERFORM _seed_msg('label',  'Generated',                              'Yaratilgan',                       'Яратилган',                       'Создано');
PERFORM _seed_msg('label',  'Filters',                                'Filtrlar',                         'Фильтрлар',                       'Фильтры');
PERFORM _seed_msg('label',  'No filter',                              'Filtrsiz',                         'Фильтрсиз',                       'Без фильтра');

-- ──────────────────────────────────────────────────────
-- SPECIALITY CLASSIFIER — mandatory admission years + edit-change confirm
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('validation', 'At least one year is required',      'Kamida bitta yil tanlanishi shart', 'Камида битта йил танланиши шарт', 'Требуется хотя бы один год');
PERFORM _seed_msg('label',  'Change admission years?',                'Qabul yillari o''zgartirilsinmi?', 'Қабул йиллари ўзгартирилсинми?',  'Изменить годы приёма?');
PERFORM _seed_msg('label',  'The current years will be replaced with the selected ones.', 'Joriy yillar tanlangan yillar bilan almashtiriladi.', 'Жорий йиллар танланган йиллар билан алмаштирилади.', 'Текущие годы будут заменены выбранными.');
PERFORM _seed_msg('label',  'Added',                                  'Qo''shiladi',                      'Қўшилади',                        'Добавляются');
PERFORM _seed_msg('label',  'Removed',                                'O''chiriladi',                     'Ўчирилади',                       'Удаляются');
END $$;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE _count INT;
BEGIN
    SELECT COUNT(*) INTO _count FROM system_message
    WHERE message_key IN (
        'Add to favorites', 'Reset password', 'Page not found',
        'Quick search', '{{count}} parameters', 'Skip to main content'
    );
    RAISE NOTICE 'S010: Sample frontend translation keys present: % / 6', _count;
END $$;
