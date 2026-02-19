-- =====================================================
-- S011: SEED CLASSIFIER I18N TRANSLATIONS (new keys only)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-14
-- Purpose: Translations for NEW classifier categories
--          and CRUD operation messages
-- Note: Keys already in S006 are NOT repeated here:
--        Classifiers, General data, Structure, Employees,
--        Education, Study process, Science classifiers,
--        Organizational, Code, Name, Name (Russian),
--        Name (English), Active, Category, Student classifiers
-- Strategy: IDEMPOTENT UPSERT using _seed_msg() helper
-- =====================================================

DO $$
BEGIN
    -- New category menu names (not in S006)
    PERFORM _seed_msg('menu', 'Financial', 'Moliyaviy', 'Молиявий', 'Финансовые');
    PERFORM _seed_msg('menu', 'Diploma', 'Diplom', 'Диплом', 'Дипломы');
    PERFORM _seed_msg('menu', 'Specialities', 'Mutaxassisliklar', 'Мутахассисликлар', 'Специальности');

    -- Classifier CRUD messages
    PERFORM _seed_msg('message', 'Classifier item created', 'Element muvaffaqiyatli yaratildi', 'Элемент муваффақиятли яратилди', 'Элемент успешно создан');
    PERFORM _seed_msg('message', 'Classifier item updated', 'Element muvaffaqiyatli yangilandi', 'Элемент муваффақиятли янгиланди', 'Элемент успешно обновлен');
    PERFORM _seed_msg('message', 'Classifier item deleted', 'Element muvaffaqiyatli o''chirildi', 'Элемент муваффақиятли ўчирилди', 'Элемент успешно удален');
    PERFORM _seed_msg('message', 'Classifier not found', 'Klasifikator topilmadi', 'Классификатор топилмади', 'Классификатор не найден');
    PERFORM _seed_msg('message', 'Item already exists', 'Bu kodli element allaqachon mavjud', 'Бу кодли элемент аллақачон мавжуд', 'Элемент с таким кодом уже существует');
    PERFORM _seed_msg('message', 'Read-only classifier', 'Bu klasifikator faqat o''qish uchun', 'Бу классификатор фақат ўқиш учун', 'Этот классификатор только для чтения');

    -- Classifier-specific labels (not in S006)
    PERFORM _seed_msg('label', 'Editable', 'Tahrirlanadigan', 'Таҳрирланадиган', 'Редактируемый');
    PERFORM _seed_msg('label', 'Read only', 'Faqat o''qish', 'Фақат ўқиш', 'Только чтение');
    PERFORM _seed_msg('label', 'Hierarchical', 'Ierarxik', 'Иерархик', 'Иерархический');
    PERFORM _seed_msg('label', 'Items count', 'Elementlar soni', 'Элементлар сони', 'Количество элементов');

    RAISE NOTICE 'S011: Classifier i18n translations seeded (13 new keys)';
END $$;
