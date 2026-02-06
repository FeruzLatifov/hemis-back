-- =====================================================
-- S007_seed_i18n_audit_logs.sql
-- Audit Logs sahifasi tarjimalari
-- =====================================================

DO $$ BEGIN

-- Sahifa sarlavhalari
PERFORM _seed_msg('menu', 'Audit Logs', 'Audit loglar', 'Аудит логлар', 'Журнал аудита');
PERFORM _seed_msg('label', 'Activity Log', 'Faoliyat logi', 'Фаолият логи', 'Журнал действий');
PERFORM _seed_msg('label', 'Request Log', 'So''rovlar logi', 'Сўровлар логи', 'Журнал запросов');
PERFORM _seed_msg('label', 'Error Log', 'Xatolar logi', 'Хатолар логи', 'Журнал ошибок');
PERFORM _seed_msg('label', 'Login Log', 'Kirish logi', 'Кириш логи', 'Журнал входов');

-- Jadval ustunlari
PERFORM _seed_msg('table', 'User', 'Foydalanuvchi', 'Фойдаланувчи', 'Пользователь');
PERFORM _seed_msg('table', 'Action', 'Harakat', 'Ҳаракат', 'Действие');
PERFORM _seed_msg('table', 'Entity Type', 'Ob''yekt turi', 'Объект тури', 'Тип объекта');
PERFORM _seed_msg('table', 'Entity Name', 'Ob''yekt nomi', 'Объект номи', 'Название объекта');
PERFORM _seed_msg('table', 'IP Address', 'IP manzil', 'IP манзил', 'IP адрес');
PERFORM _seed_msg('table', 'Method', 'Metod', 'Метод', 'Метод');
PERFORM _seed_msg('table', 'URI', 'URI', 'URI', 'URI');
PERFORM _seed_msg('table', 'Status Code', 'Status kodi', 'Статус коди', 'Код статуса');
PERFORM _seed_msg('table', 'Response Time', 'Javob vaqti', 'Жавоб вақти', 'Время ответа');
PERFORM _seed_msg('table', 'Error Type', 'Xato turi', 'Хато тури', 'Тип ошибки');
PERFORM _seed_msg('table', 'Error Message', 'Xato xabari', 'Хато хабари', 'Сообщение ошибки');
PERFORM _seed_msg('table', 'Event Type', 'Hodisa turi', 'Ҳодиса тури', 'Тип события');
PERFORM _seed_msg('table', 'Failure Reason', 'Xatolik sababi', 'Хатолик сабаби', 'Причина ошибки');
PERFORM _seed_msg('table', 'Timestamp', 'Vaqt', 'Вақт', 'Время');
PERFORM _seed_msg('table', 'User Agent', 'User Agent', 'User Agent', 'User Agent');
PERFORM _seed_msg('table', 'Session ID', 'Sessiya ID', 'Сессия ID', 'ID сессии');
PERFORM _seed_msg('table', 'Request ID', 'So''rov ID', 'Сўров ID', 'ID запроса');

-- Tafsilot
PERFORM _seed_msg('label', 'Old Value', 'Eski qiymat', 'Эски қиймат', 'Старое значение');
PERFORM _seed_msg('label', 'New Value', 'Yangi qiymat', 'Янги қиймат', 'Новое значение');
PERFORM _seed_msg('label', 'Changed Fields', 'O''zgargan maydonlar', 'Ўзгарган майдонлар', 'Изменённые поля');
PERFORM _seed_msg('label', 'Stack Trace', 'Stack trace', 'Stack trace', 'Трассировка стека');
PERFORM _seed_msg('label', 'Request Body', 'So''rov tanasi', 'Сўров танаси', 'Тело запроса');
PERFORM _seed_msg('label', 'Response Body', 'Javob tanasi', 'Жавоб танаси', 'Тело ответа');
PERFORM _seed_msg('label', 'Query String', 'So''rov parametrlari', 'Сўров параметрлари', 'Параметры запроса');

-- Harakatlar
PERFORM _seed_msg('action', 'View Details', 'Tafsilotlarni ko''rish', 'Тафсилотларни кўриш', 'Просмотр деталей');
PERFORM _seed_msg('action', 'Export Logs', 'Loglarni eksport qilish', 'Логларни экспорт қилиш', 'Экспорт журналов');
PERFORM _seed_msg('action', 'Refresh', 'Yangilash', 'Янгилаш', 'Обновить');

-- Filtrlar
PERFORM _seed_msg('label', 'Filter by user', 'Foydalanuvchi bo''yicha filtrlash', 'Фойдаланувчи бўйича фильтрлаш', 'Фильтр по пользователю');
PERFORM _seed_msg('label', 'Filter by action', 'Harakat bo''yicha filtrlash', 'Ҳаракат бўйича фильтрлаш', 'Фильтр по действию');
PERFORM _seed_msg('label', 'Filter by date', 'Sana bo''yicha filtrlash', 'Сана бўйича фильтрлаш', 'Фильтр по дате');
PERFORM _seed_msg('label', 'Date range', 'Sana oralig''i', 'Сана оралиғи', 'Диапазон дат');

-- Statistika
PERFORM _seed_msg('label', 'Statistics', 'Statistika', 'Статистика', 'Статистика');
PERFORM _seed_msg('label', 'Top Users', 'Eng faol foydalanuvchilar', 'Энг фаол фойдаланувчилар', 'Топ пользователей');
PERFORM _seed_msg('label', 'Top Endpoints', 'Eng ko''p so''rovlar', 'Энг кўп сўровлар', 'Топ эндпоинтов');
PERFORM _seed_msg('label', 'Error Rate', 'Xatolar darajasi', 'Хатолар даражаси', 'Частота ошибок');
PERFORM _seed_msg('label', 'Total Activities', 'Jami faoliyatlar', 'Жами фаолиятлар', 'Всего действий');
PERFORM _seed_msg('label', 'Total Requests', 'Jami so''rovlar', 'Жами сўровлар', 'Всего запросов');
PERFORM _seed_msg('label', 'Total Errors', 'Jami xatolar', 'Жами хатолар', 'Всего ошибок');
PERFORM _seed_msg('label', 'Total Logins', 'Jami kirishlar', 'Жами киришлар', 'Всего входов');

-- Login event types
PERFORM _seed_msg('status', 'Login Success', 'Muvaffaqiyatli kirish', 'Муваффақиятли кириш', 'Успешный вход');
PERFORM _seed_msg('status', 'Login Failed', 'Muvaffaqiyatsiz kirish', 'Муваффақиятсиз кириш', 'Неудачный вход');
PERFORM _seed_msg('status', 'Logout', 'Chiqish', 'Чиқиш', 'Выход');
PERFORM _seed_msg('status', 'Token Refresh', 'Token yangilash', 'Токен янгилаш', 'Обновление токена');
PERFORM _seed_msg('status', 'Session Expired', 'Sessiya tugadi', 'Сессия тугади', 'Сессия истекла');

END $$;
