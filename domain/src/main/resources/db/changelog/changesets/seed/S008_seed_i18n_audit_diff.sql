-- =====================================================
-- S008_seed_i18n_audit_diff.sql
-- Audit Trail — Old/New Value Diff + Entity History tarjimalari
-- =====================================================

DO $$ BEGIN

-- Diff Viewer labels
PERFORM _seed_msg('label', 'Changed Fields', 'O''zgargan maydonlar', 'Ўзгарган майдонлар', 'Измененные поля');
PERFORM _seed_msg('label', 'Old Value', 'Eski qiymat', 'Эски қиймат', 'Старое значение');
PERFORM _seed_msg('label', 'New Value', 'Yangi qiymat', 'Янги қиймат', 'Новое значение');
PERFORM _seed_msg('label', 'Field', 'Maydon', 'Майдон', 'Поле');
PERFORM _seed_msg('label', 'Value', 'Qiymat', 'Қиймат', 'Значение');
PERFORM _seed_msg('label', 'Unchanged Fields', 'O''zgarmagan maydonlar', 'Ўзгармаган майдонлар', 'Неизмененные поля');

-- Entity History
PERFORM _seed_msg('label', 'Entity History', 'O''zgarishlar tarixi', 'Ўзгаришлар тарихи', 'История изменений');

-- Date Range Filter
PERFORM _seed_msg('label', 'Today', 'Bugun', 'Бугун', 'Сегодня');
PERFORM _seed_msg('label', 'Last 7 days', 'Oxirgi 7 kun', 'Охирги 7 кун', 'Последние 7 дней');
PERFORM _seed_msg('label', 'Last 30 days', 'Oxirgi 30 kun', 'Охирги 30 кун', 'Последние 30 дней');
PERFORM _seed_msg('label', 'All time', 'Barcha vaqt', 'Барча вақт', 'Все время');
PERFORM _seed_msg('label', 'Date from', 'Dan', 'Дан', 'С');
PERFORM _seed_msg('label', 'Date to', 'Gacha', 'Гача', 'По');

END $$;
