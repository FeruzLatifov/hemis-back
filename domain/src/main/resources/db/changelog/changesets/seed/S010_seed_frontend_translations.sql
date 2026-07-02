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
PERFORM _seed_msg('label',   '{{count}} parameters',                                     '{{count}} parametr',              '{{count}} параметр',              '{{count}} параметров');
PERFORM _seed_msg('label',   '{{count}} parameters_one',                                 '{{count}} parametr',              '{{count}} параметр',              '{{count}} параметр');
PERFORM _seed_msg('label',   '{{count}} parameters_few',                                 '{{count}} parametr',              '{{count}} параметр',              '{{count}} параметра');
PERFORM _seed_msg('label',   '{{count}} parameters_many',                                '{{count}} parametr',              '{{count}} параметр',              '{{count}} параметров');
PERFORM _seed_msg('label',   '{{count}} parameters_other',                               '{{count}} parametr',              '{{count}} параметр',              '{{count}} параметров');
PERFORM _seed_msg('label',   '{{count}} students found',                                 '{{count}} ta talaba topildi',     '{{count}} та талаба топилди',     'Найдено {{count}} студентов');
PERFORM _seed_msg('label',   '{{count}} students found_one',                             '{{count}} ta talaba topildi',     '{{count}} та талаба топилди',     'Найден {{count}} студент');
PERFORM _seed_msg('label',   '{{count}} students found_few',                             '{{count}} ta talaba topildi',     '{{count}} та талаба топилди',     'Найдено {{count}} студента');
PERFORM _seed_msg('label',   '{{count}} students found_many',                            '{{count}} ta talaba topildi',     '{{count}} та талаба топилди',     'Найдено {{count}} студентов');
PERFORM _seed_msg('label',   '{{count}} students found_other',                           '{{count}} ta talaba topildi',     '{{count}} та талаба топилди',     'Найдено {{count}} студентов');
PERFORM _seed_msg('label',   '{{count}} groups found_one',                               '{{count}} ta guruh topildi',      '{{count}} та гуруҳ топилди',      'Найдена {{count}} группа');
PERFORM _seed_msg('label',   '{{count}} groups found_few',                               '{{count}} ta guruh topildi',      '{{count}} та гуруҳ топилди',      'Найдено {{count}} группы');
PERFORM _seed_msg('label',   '{{count}} groups found_many',                              '{{count}} ta guruh topildi',      '{{count}} та гуруҳ топилди',      'Найдено {{count}} групп');
PERFORM _seed_msg('label',   '{{count}} groups found_other',                             '{{count}} ta guruh topildi',      '{{count}} та гуруҳ топилди',      'Найдено {{count}} групп');
PERFORM _seed_msg('label',   '{{count}} specialities found_one',                         '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди',    'Найдено {{count}} направление');
PERFORM _seed_msg('label',   '{{count}} specialities found_few',                         '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди',    'Найдено {{count}} направления');
PERFORM _seed_msg('label',   '{{count}} specialities found_many',                        '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди',    'Найдено {{count}} направлений');
PERFORM _seed_msg('label',   '{{count}} specialities found_other',                       '{{count}} ta yo''nalish topildi', '{{count}} та йўналиш топилди',    'Найдено {{count}} направлений');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_one',             'Kamida {{count}} belgi bo''lishi kerak','Камида {{count}} белги бўлиши керак','Должно быть не менее {{count}} символа');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_few',             'Kamida {{count}} belgi bo''lishi kerak','Камида {{count}} белги бўлиши керак','Должно быть не менее {{count}} символов');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_many',            'Kamida {{count}} belgi bo''lishi kerak','Камида {{count}} белги бўлиши керак','Должно быть не менее {{count}} символов');
PERFORM _seed_msg('validation', 'Must be at least {{count}} characters_other',           'Kamida {{count}} belgi bo''lishi kerak','Камида {{count}} белги бўлиши керак','Должно быть не менее {{count}} символов');
PERFORM _seed_msg('label',   'Certificates',                                             'Sertifikatlar',                   'Сертификатлар',                   'Сертификаты');
PERFORM _seed_msg('label',   'Scholarships',                                             'Stipendiyalar',                   'Стипендиялар',                    'Стипендии');
PERFORM _seed_msg('label',   'Qualifications',                                           'Malakalar',                       'Малакалар',                       'Квалификации');
PERFORM _seed_msg('label',   'GPA rating',                                               'GPA reyting',                     'GPA рейтинг',                     'Рейтинг GPA');

-- Page-not-found and error pages
PERFORM _seed_msg('error',   'Page not found',                                           'Sahifa topilmadi',                'Саҳифа топилмади',                'Страница не найдена');
PERFORM _seed_msg('error',   'Sorry, the page you''re looking for doesn''t exist or has been moved.','Kechirasiz, siz qidirayotgan sahifa mavjud emas yoki ko''chirilgan.','Кечирасиз, сиз қидираётган саҳифа мавжуд эмас ёки кўчирилган.','Извините, страница, которую вы ищете, не существует или была перемещена.');
PERFORM _seed_msg('error',   'If you believe this is an error, please contact the system administrator.','Agar bu xato deb hisoblasangiz, tizim administratoriga murojaat qiling.','Агар бу хато деб ҳисобласангиз, тизим администраторига мурожаат қилинг.','Если вы считаете это ошибкой, обратитесь к администратору.');
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
