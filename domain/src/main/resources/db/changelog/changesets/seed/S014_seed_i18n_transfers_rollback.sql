-- Rollback S014: Remove transfer detection translations
DELETE FROM system_message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE message_key IN (
        'Internal transfer',
        'External transfer',
        'Speciality'
    )
);

DELETE FROM system_messages
WHERE message_key IN (
    'Internal transfer',
    'External transfer',
    'Speciality'
);
