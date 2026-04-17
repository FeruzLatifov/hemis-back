-- =====================================================
-- S009: SEED TRANSLATIONS — Universities module
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-14
-- Purpose: Translations for /institutions/universities pages
--   (Detail, Form, List, Filters, Profile tab, Officials tab, etc.)
-- Pattern: S006_seed_translations.sql (_seed_msg helper)
-- Safety: ON CONFLICT UPDATE — idempotent, no duplicates
-- =====================================================

-- Helper _seed_msg() is defined in S006_seed_translations.sql (persistent, not dropped).

DO $$
BEGIN

-- ──────────────────────────────────────────────────────
-- LABELS (form fields, sections, columns)
-- ──────────────────────────────────────────────────────
--                   category   key(en)                                uz                              oz                              ru
PERFORM _seed_msg('label',  'General',                             'Umumiy',                      'Умумий',                      'Общее');
PERFORM _seed_msg('label',  'Legal',                               'Yuridik',                     'Юридик',                      'Юридическое');
PERFORM _seed_msg('label',  'Officials',                           'Rahbariyat',                  'Раҳбарият',                   'Руководство');
PERFORM _seed_msg('label',  'Property',                            'Mol-mulk',                    'Мол-мулк',                    'Имущество');
PERFORM _seed_msg('label',  'History',                             'Tarix',                       'Тарих',                       'История');
PERFORM _seed_msg('label',  'Location',                            'Joylashuv',                   'Жойлашув',                    'Расположение');
PERFORM _seed_msg('label',  'Contacts',                            'Aloqa',                       'Алоқа',                       'Контакты');
PERFORM _seed_msg('label',  'Documents',                           'Hujjatlar',                   'Ҳужжатлар',                   'Документы');
PERFORM _seed_msg('label',  'Document',                            'Hujjat',                      'Ҳужжат',                      'Документ');
PERFORM _seed_msg('label',  'Document title',                      'Hujjat nomi',                 'Ҳужжат номи',                 'Название документа');
PERFORM _seed_msg('label',  'Social links',                        'Ijtimoiy tarmoqlar',          'Ижтимоий тармоқлар',          'Социальные сети');
PERFORM _seed_msg('label',  'Website',                             'Veb-sayt',                    'Веб-сайт',                    'Веб-сайт');
PERFORM _seed_msg('label',  'Feature flags',                       'Funksional bayroqlar',        'Функсионал байроқлар',        'Функциональные флаги');
PERFORM _seed_msg('label',  'HEMIS version',                       'HEMIS versiyasi',             'HEMIS версияси',              'Версия HEMIS');
PERFORM _seed_msg('label',  'HEMIS configuration',                 'HEMIS konfiguratsiyasi',      'HEMIS конфигурацияси',        'Конфигурация HEMIS');
-- URLs section — simplified labels (role-based, no redundant "URL"/"portal")
PERFORM _seed_msg('label',  'URLs',                                'Manzillar',                   'Манзиллар',                   'Адреса');
PERFORM _seed_msg('label',  'University site',                     'OTM sayti',                   'ОТМ сайти',                   'Сайт вуза');
PERFORM _seed_msg('label',  'Students',                            'Talabalar',                   'Талабалар',                   'Студенты');
PERFORM _seed_msg('label',  'Teachers',                            'O''qituvchilar',              'Ўқитувчилар',                 'Преподаватели');
PERFORM _seed_msg('label',  'UZBMB',                               'UZBMB',                       'UZBMB',                       'UZBMB');

-- Map location
PERFORM _seed_msg('label',  'Map location',                        'Xaritadagi joylashuv',        'Харитадаги жойлашув',         'Местоположение на карте');
PERFORM _seed_msg('label',  'Map URL',                             'Xarita manzili',              'Харита манзили',              'Ссылка на карту');
PERFORM _seed_msg('label',  'Latitude',                            'Kenglik',                     'Кенглик',                     'Широта');
PERFORM _seed_msg('label',  'Longitude',                           'Uzunlik',                     'Узунлик',                     'Долгота');
PERFORM _seed_msg('action', 'Extract from URL',                    'URL''dan olish',              'URL''дан олиш',               'Извлечь из URL');
PERFORM _seed_msg('action', 'Open in map',                         'Xaritada ochish',             'Харитада очиш',               'Открыть на карте');
PERFORM _seed_msg('action', 'Get directions',                      'Yo''l ko''rsatish',           'Йўл кўрсатиш',                'Проложить маршрут');
PERFORM _seed_msg('message','Paste Google Maps or Yandex Maps link', 'Google Maps yoki Yandex Maps havolasini joylashtiring', 'Google Maps ёки Яндекс Maps ҳаволасини жойлаштиринг', 'Вставьте ссылку Google Maps или Яндекс.Карт');
PERFORM _seed_msg('label',  'District',                            'Tuman',                       'Туман',                       'Район');
PERFORM _seed_msg('label',  'Neighborhood',                        'Mahalla',                     'Маҳалла',                     'Махалля');
PERFORM _seed_msg('label',  'Postcode',                            'Pochta indeksi',              'Почта индекси',               'Почтовый индекс');
PERFORM _seed_msg('label',  'SOATO',                               'SOATO',                       'СОАТО',                       'СОАТО');
PERFORM _seed_msg('label',  'INN',                                 'INN',                         'ИНН',                         'ИНН');
PERFORM _seed_msg('label',  'PINFL',                               'JShShIR',                     'ЖШШИР',                       'ЖШШИР');
PERFORM _seed_msg('label',  'Passport',                            'Pasport',                     'Паспорт',                     'Паспорт');
PERFORM _seed_msg('label',  'OKED',                                'OKED',                        'ОКЕД',                        'ОКЭД');
PERFORM _seed_msg('label',  'First name',                          'Ism',                         'Исм',                         'Имя');
PERFORM _seed_msg('label',  'Last name',                           'Familiya',                    'Фамилия',                     'Фамилия');
PERFORM _seed_msg('label',  'Middle name',                         'Otasining ismi',              'Отасининг исми',              'Отчество');
PERFORM _seed_msg('label',  'Birth date',                          'Tug''ilgan sana',             'Туғилган сана',               'Дата рождения');
PERFORM _seed_msg('label',  'Note',                                'Izoh',                        'Изоҳ',                        'Примечание');
PERFORM _seed_msg('label',  'Period',                              'Davr',                        'Давр',                        'Период');
PERFORM _seed_msg('label',  'Share',                               'Ulush',                       'Улуш',                        'Доля');
PERFORM _seed_msg('label',  'Individual',                          'Jismoniy shaxs',              'Жисмоний шахс',               'Физическое лицо');
PERFORM _seed_msg('label',  'Owners',                              'Egalari',                     'Эгалари',                     'Владельцы');
PERFORM _seed_msg('label',  'Rector',                              'Rektor',                      'Ректор',                      'Ректор');
PERFORM _seed_msg('label',  'Accountant',                          'Bosh hisobchi',               'Бош ҳисобчи',                 'Главный бухгалтер');
PERFORM _seed_msg('label',  'Account',                             'Hisob raqam',                 'Ҳисоб рақам',                 'Счёт');
PERFORM _seed_msg('label',  'Opened',                              'Ochilgan',                    'Очилган',                     'Открыт');
PERFORM _seed_msg('label',  'Director (legal)',                    'Direktor (yuridik)',          'Директор (юридик)',           'Директор (юр.)');
PERFORM _seed_msg('label',  'Director (legal representative)',     'Direktor (yuridik vakil)',    'Директор (юридик вакил)',     'Директор (юр. представитель)');
PERFORM _seed_msg('label',  'Legal form',                          'Tashkiliy-huquqiy shakl',     'Ташкилий-ҳуқуқий шакл',       'Орг.-правовая форма');
PERFORM _seed_msg('label',  'Ownership form',                      'Mulkchilik shakli',           'Мулкчилик шакли',             'Форма собственности');
PERFORM _seed_msg('label',  'Legal entity',                        'Yuridik shaxs',               'Юридик шахс',                 'Юридическое лицо');
PERFORM _seed_msg('label',  'Legal address',                       'Yuridik manzil',              'Юридик манзил',               'Юридический адрес');
PERFORM _seed_msg('label',  'Registration number',                 'Ro''yxat raqami',             'Рўйхат рақами',               'Регистрационный номер');
PERFORM _seed_msg('label',  'Registration date',                   'Ro''yxatdan o''tgan sana',    'Рўйхатдан ўтган сана',        'Дата регистрации');
PERFORM _seed_msg('label',  'Re-registration date',                'Qayta ro''yxatdan o''tgan sana', 'Қайта рўйхатдан ўтган сана', 'Дата перерегистрации');
PERFORM _seed_msg('label',  'Average employees',                   'O''rtacha xodimlar soni',     'Ўртача ходимлар сони',        'Среднее число сотрудников');
PERFORM _seed_msg('label',  'Last synced',                         'Oxirgi sinxronlash',          'Охирги синхронлаш',           'Последняя синхронизация');
PERFORM _seed_msg('label',  'Bank accounts',                       'Bank hisoblari',              'Банк ҳисоблари',              'Банковские счета');
PERFORM _seed_msg('label',  'Bank info',                           'Bank ma''lumotlari',          'Банк маълумотлари',           'Банковская информация');
PERFORM _seed_msg('label',  'Founders',                            'Ta''sischilar',               'Таъсисчилар',                 'Учредители');
PERFORM _seed_msg('label',  'Lifecycle',                           'Hayot sikli',                 'Ҳаёт сикли',                  'Жизненный цикл');
PERFORM _seed_msg('label',  'Decree',                              'Farmon',                      'Фармон',                      'Указ');
PERFORM _seed_msg('label',  'Decree number',                       'Farmon raqami',               'Фармон рақами',               'Номер указа');
PERFORM _seed_msg('label',  'Effective date',                      'Kuchga kirish sanasi',        'Кучга кириш санаси',          'Дата вступления в силу');
PERFORM _seed_msg('label',  'Successor university code',           'Huquqiy vorisi OTM kodi',     'Ҳуқуқий вориси ОТМ коди',     'Код ВУЗа-правопреемника');
PERFORM _seed_msg('label',  'Land area',                           'Yer maydoni',                 'Ер майдони',                  'Площадь земли');
PERFORM _seed_msg('label',  'Building area',                       'Bino maydoni',                'Бино майдони',                'Площадь здания');
PERFORM _seed_msg('label',  'Cadastre value',                      'Kadastr qiymati',             'Кадастр қиймати',             'Кадастровая стоимость');
PERFORM _seed_msg('label',  'Real estate',                         'Ko''chmas mulk',              'Кўчмас мулк',                 'Недвижимость');
PERFORM _seed_msg('label',  'Valid from',                          'Amal qilish boshi',           'Амал қилиш боши',             'Действует с');
PERFORM _seed_msg('label',  'Valid to',                            'Amal qilish oxiri',           'Амал қилиш охири',            'Действует до');
PERFORM _seed_msg('label',  'Grading system',                      'Baholash tizimi',             'Баҳолаш тизими',              'Система оценивания');
PERFORM _seed_msg('label',  'Allow transfer outside',              'Tashqariga o''tkazishga ruxsat', 'Ташқарига ўтказишга рухсат', 'Разрешить перевод наружу');
PERFORM _seed_msg('label',  'Accreditation details',               'Akkreditatsiya tafsilotlari', 'Аккредитация тафсилотлари',   'Детали аккредитации');
PERFORM _seed_msg('label',  'Additional details',                  'Qo''shimcha tafsilotlar',     'Қўшимча тафсилотлар',         'Дополнительные детали');
PERFORM _seed_msg('label',  'Short description of the university', 'Universitet haqida qisqacha', 'Университет ҳақида қисқача',  'Краткое описание университета');
PERFORM _seed_msg('label',  'e.g. 301',                            'masalan: 301',                'масалан: 301',                'например: 301');
PERFORM _seed_msg('label',  'Current',                             'Joriy',                       'Жорий',                       'Текущий');
PERFORM _seed_msg('label',  'Closed',                              'Yopilgan',                    'Ёпилган',                     'Закрыт');
PERFORM _seed_msg('label',  'Restricted',                          'Cheklangan',                  'Чекланган',                   'Ограничено');
PERFORM _seed_msg('label',  'selected',                            'tanlandi',                    'танланди',                    'выбрано');

-- University activity statuses (hardcoded enum on the frontend; no classifier table).
-- Kept in-app rather than in hemishe_h_university_activity_status so we can drop that
-- old-hemis table once the integration is decommissioned.
PERFORM _seed_msg('label',  'Merged',                              'Birlashtirilgan',             'Бирлаштирилган',              'Объединён');
PERFORM _seed_msg('label',  'License revoked',                     'Litsenziya bekor qilingan',   'Лицензия бекор қилинган',     'Лицензия отозвана');
PERFORM _seed_msg('label',  'Suspended',                           'To''xtatilgan',               'Тўхтатилган',                 'Приостановлен');
PERFORM _seed_msg('label',  'Reorganized',                         'Qayta tashkil etilgan',       'Қайта ташкил этилган',        'Реорганизован');

-- ──────────────────────────────────────────────────────
-- ACTIONS (buttons)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('action', 'Appoint',                             'Tayinlash',                   'Тайинлаш',                    'Назначить');
PERFORM _seed_msg('action', 'Appoint official',                    'Rahbar tayinlash',            'Раҳбар тайинлаш',             'Назначить руководителя');
PERFORM _seed_msg('action', 'Dismiss',                             'Bo''shatish',                 'Бўшатиш',                     'Уволить');
PERFORM _seed_msg('action', 'Remove',                              'Olib tashlash',               'Олиб ташлаш',                 'Удалить');
PERFORM _seed_msg('action', 'Save profile',                        'Profilni saqlash',            'Профилни сақлаш',             'Сохранить профиль');
PERFORM _seed_msg('action', 'Add document',                        'Hujjat qo''shish',            'Ҳужжат қўшиш',                'Добавить документ');
PERFORM _seed_msg('action', 'Back to list',                        'Ro''yxatga qaytish',          'Рўйхатга қайтиш',             'Назад к списку');
PERFORM _seed_msg('action', 'Delete university',                   'Universitetni o''chirish',    'Университетни ўчириш',        'Удалить университет');
PERFORM _seed_msg('action', 'Export selected',                     'Tanlanganlarni eksport qilish','Танланганларни экспорт қилиш','Экспорт выбранных');
PERFORM _seed_msg('action', 'Search in external database',         'Tashqi bazada qidirish',      'Ташқи базада қидириш',        'Поиск во внешней базе');
PERFORM _seed_msg('action', 'Sync external data',                  'Tashqi ma''lumotlarni sinxronlash','Ташқи маълумотларни синхронлаш','Синхронизировать внешние данные');
PERFORM _seed_msg('action', 'Confirm dismissal',                   'Bo''shatishni tasdiqlash',    'Бўшатишни тасдиқлаш',         'Подтвердить увольнение');
PERFORM _seed_msg('action', 'Select row',                          'Qatorni tanlash',             'Қаторни танлаш',              'Выбрать строку');
PERFORM _seed_msg('action', 'Select district first',               'Avval tumanni tanlang',       'Аввал туманни танланг',       'Сначала выберите район');
PERFORM _seed_msg('action', 'Compact view',                        'Ixcham ko''rinish',           'Ихчам кўриниш',               'Компактный вид');
PERFORM _seed_msg('action', 'Comfortable view',                    'Qulay ko''rinish',            'Қулай кўриниш',               'Удобный вид');
PERFORM _seed_msg('action', 'Click to copy',                       'Nusxa olish uchun bosing',    'Нусха олиш учун босинг',      'Нажмите чтобы скопировать');

-- ──────────────────────────────────────────────────────
-- MESSAGES (status, empty states, info)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('message', 'Searching...',                       'Qidirilmoqda...',             'Қидирилмоқда...',             'Поиск...');
PERFORM _seed_msg('message', 'Syncing...',                         'Sinxronlanmoqda...',          'Синхронланмоқда...',          'Синхронизация...');
PERFORM _seed_msg('message', 'Person found',                       'Shaxs topildi',               'Шахс топилди',                'Человек найден');
PERFORM _seed_msg('message', 'Copied',                             'Nusxa olindi',                'Нусха олинди',                'Скопировано');
PERFORM _seed_msg('message', 'Data refreshed',                     'Ma''lumotlar yangilandi',     'Маълумотлар янгиланди',       'Данные обновлены');
PERFORM _seed_msg('message', 'Excel file downloading...',          'Excel fayl yuklanmoqda...',   'Excel файл юкланмоқда...',    'Excel файл загружается...');
PERFORM _seed_msg('message', 'University not found',               'Universitet topilmadi',       'Университет топилмади',       'Университет не найден');
PERFORM _seed_msg('message', 'No lifecycle events',                'Hayot sikli hodisalari yo''q','Ҳаёт сикли ҳодисалари йўқ',   'События жизненного цикла отсутствуют');
PERFORM _seed_msg('message', 'No documents yet',                   'Hujjatlar hali qo''shilmagan','Ҳужжатлар ҳали қўшилмаган',   'Документы ещё не добавлены');
PERFORM _seed_msg('message', 'No universities have been added yet','Universitetlar hali qo''shilmagan','Университетлар ҳали қўшилмаган','Университеты ещё не добавлены');
PERFORM _seed_msg('message', 'No data. Use Sync in Edit page.',    'Ma''lumot yo''q. Tahrir sahifasida Sinxronlash tugmasidan foydalaning.', 'Маълумот йўқ. Таҳрир саҳифасида Синхронлаш тугмасидан фойдаланинг.', 'Нет данных. Используйте Синхронизацию на странице редактирования.');
PERFORM _seed_msg('message', 'No data. Use Edit page to add contacts, social links, and documents.', 'Ma''lumot yo''q. Aloqa, ijtimoiy tarmoqlar va hujjatlar qo''shish uchun tahrir sahifasidan foydalaning.', 'Маълумот йўқ. Алоқа, ижтимоий тармоқлар ва ҳужжатлар қўшиш учун таҳрир саҳифасидан фойдаланинг.', 'Нет данных. Используйте страницу редактирования для добавления контактов, соц. сетей и документов.');
PERFORM _seed_msg('message', 'No officials. Use Edit page to appoint.', 'Rahbarlar yo''q. Tayinlash uchun tahrir sahifasidan foydalaning.', 'Раҳбарлар йўқ. Тайинлаш учун таҳрир саҳифасидан фойдаланинг.', 'Руководителей нет. Используйте страницу редактирования для назначения.');
PERFORM _seed_msg('message', 'Person not found locally. Enter document or birth date to search external database.', 'Shaxs lokal bazada topilmadi. Tashqi bazada qidirish uchun hujjat yoki tug''ilgan sanani kiriting.', 'Шахс локал базада топилмади. Ташқи базада қидириш учун ҳужжат ёки туғилган санани киритинг.', 'Человек не найден в локальной базе. Введите документ или дату рождения для поиска во внешней базе.');
PERFORM _seed_msg('message', 'Source: university sync. Use Edit to appoint via ministry.', 'Manba: universitet sinxronlash. Vazirlik orqali tayinlash uchun Tahrirdan foydalaning.', 'Манба: университет синхронлаш. Вазирлик орқали тайинлаш учун Таҳрирдан фойдаланинг.', 'Источник: синхронизация университета. Используйте Редактирование для назначения через министерство.');
PERFORM _seed_msg('message', 'Status is being changed. Please provide details:', 'Status o''zgartirilmoqda. Iltimos, tafsilotlarni kiriting:', 'Статус ўзгартирилмоқда. Илтимос, тафсилотларни киритинг:', 'Статус изменяется. Пожалуйста, укажите детали:');

-- ──────────────────────────────────────────────────────
-- CONFIRM (dialogs)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('confirm', 'Dismissal confirmation',              'Bo''shatishni tasdiqlash',   'Бўшатишни тасдиқлаш',         'Подтверждение увольнения');
PERFORM _seed_msg('confirm', 'Are you sure you want to delete this university? This action cannot be undone.', 'Bu universitetni o''chirishga ishonchingiz komilmi? Bu amalni qaytarib bo''lmaydi.', 'Бу университетни ўчиришга ишончингиз комилми? Бу амални қайтариб бўлмайди.', 'Вы уверены, что хотите удалить этот университет? Это действие нельзя отменить.');

-- ──────────────────────────────────────────────────────
-- FEATURE FLAGS (toggle labels on Form / Detail pages)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('label', 'OneID login',                             'OneID orqali kirish',         'OneID орқали кириш',          'Вход через OneID');
PERFORM _seed_msg('label', 'Add foreign student',                     'Xorijiy talaba qo''shish',    'Хорижий талаба қўшиш',        'Добавить иностранного студента');
PERFORM _seed_msg('label', 'Add transfer student',                    'O''tkazma talaba qo''shish',  'Ўтказма талаба қўшиш',        'Добавить переводного студента');
PERFORM _seed_msg('label', 'Add academic mobile student',             'Akademik mobil talaba qo''shish','Академик мобил талаба қўшиш','Добавить студента академической мобильности');

-- ──────────────────────────────────────────────────────
-- DOCUMENT TYPES (rendered as t(code) on Form / Detail)
-- ──────────────────────────────────────────────────────
PERFORM _seed_msg('label', 'LICENSE',                                 'Litsenziya',                  'Лицензия',                    'Лицензия');
PERFORM _seed_msg('label', 'ACCREDITATION',                           'Akkreditatsiya',              'Аккредитация',                'Аккредитация');
PERFORM _seed_msg('label', 'CHARTER',                                 'Nizom',                       'Низом',                       'Устав');
PERFORM _seed_msg('label', 'OTHER',                                   'Boshqa',                      'Бошқа',                       'Другое');

END $$;
