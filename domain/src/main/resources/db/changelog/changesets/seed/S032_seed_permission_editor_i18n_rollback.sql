-- =====================================================
-- Rollback S032: remove the role permission editor (RBAC) i18n keys
-- =====================================================
-- Deletes only the keys S032 INTRODUCED (by explicit message_key list), guarding
-- for table existence (S006/S010 own the same 'action'/'label'/'menu' categories).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Access',
        'Approve',
        'Sync',
        'Manage',
        'Buildings',
        'Views data (read-only)',
        'Grants access to the section',
        'Adds new records',
        'Edits existing records',
        'Approves & publishes records',
        'Syncs with external system',
        'Imports data from a file',
        'Exports data to a file',
        'Full control (all actions)',
        'Deletes records (irreversible)',
        'Full access',
        'Can edit',
        'View only',
        'Expand all',
        'Collapse all',
        'Clear all',
        'Select all view permissions',
        'No permissions granted',
        'No permissions match your search',
        '{{count}} of {{total}} selected'
    ];
    _deleted_translations BIGINT := 0;
    _deleted_messages BIGINT := 0;
BEGIN
    IF to_regclass('public.system_message_translation') IS NOT NULL THEN
        DELETE FROM system_message_translation
        WHERE message_id IN (SELECT id FROM system_message WHERE message_key = ANY(_keys));
        GET DIAGNOSTICS _deleted_translations = ROW_COUNT;
    END IF;

    IF to_regclass('public.system_message') IS NOT NULL THEN
        DELETE FROM system_message WHERE message_key = ANY(_keys);
        GET DIAGNOSTICS _deleted_messages = ROW_COUNT;
    END IF;

    RAISE NOTICE 'S032 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
