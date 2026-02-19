-- Rollback S013: Remove duplicate detection translations
DELETE FROM system_message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE message_key IN (
        'Duplicates',
        'Students with duplicate PINFL analysis',
        'Total duplicate PINFLs',
        'Cross university',
        'Same university',
        'Multi level',
        'Normal',
        'records',
        'universities',
        '{{count}} groups found',
        'No duplicate records found'
    )
);

DELETE FROM system_messages
WHERE message_key IN (
    'Duplicates',
    'Students with duplicate PINFL analysis',
    'Total duplicate PINFLs',
    'Cross university',
    'Same university',
    'Multi level',
    'Normal',
    'records',
    'universities',
    '{{count}} groups found',
    'No duplicate records found'
);
