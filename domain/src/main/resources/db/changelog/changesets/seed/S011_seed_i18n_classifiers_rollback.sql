-- =====================================================
-- S011_seed_i18n_classifiers_rollback.sql
-- =====================================================

DELETE FROM system_message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE message_key IN (
        'Financial', 'Diploma', 'Specialities',
        'Classifier item created', 'Classifier item updated',
        'Classifier item deleted', 'Classifier not found',
        'Item already exists', 'Read-only classifier',
        'Editable', 'Read only', 'Hierarchical', 'Items count'
    )
);

DELETE FROM system_messages
WHERE message_key IN (
    'Financial', 'Diploma', 'Specialities',
    'Classifier item created', 'Classifier item updated',
    'Classifier item deleted', 'Classifier not found',
    'Item already exists', 'Read-only classifier',
    'Editable', 'Read only', 'Hierarchical', 'Items count'
);
