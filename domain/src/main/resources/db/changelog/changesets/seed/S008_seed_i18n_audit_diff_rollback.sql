-- =====================================================
-- S008_seed_i18n_audit_diff_rollback.sql
-- =====================================================

DELETE FROM system_message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE message_key IN (
        'Changed Fields', 'Old Value', 'New Value', 'Field', 'Value',
        'Unchanged Fields', 'Entity History',
        'Today', 'Last 7 days', 'Last 30 days', 'All time',
        'Date from', 'Date to'
    )
);

DELETE FROM system_messages
WHERE message_key IN (
    'Changed Fields', 'Old Value', 'New Value', 'Field', 'Value',
    'Unchanged Fields', 'Entity History',
    'Today', 'Last 7 days', 'Last 30 days', 'All time',
    'Date from', 'Date to'
);
