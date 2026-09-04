-- =====================================================
-- S039 ROLLBACK: remove the history-dialog translation keys
-- =====================================================
DELETE FROM system_message
 WHERE message_key IN ('Who did what, and when',
                       'Attached, edited and detached specialities',
                       'Updated',
                       'Deleted',
                       'Restored',
                       'Viewed',
                       'Exported',
                       'Imported',
                       'Name (Uzbek Cyrillic)',
                       'Any edit returns an approved speciality to Needs review and withdraws it from the OTMs',
                       'Expand',
                       'Collapse',
                       'Client ID',
                       'Last used');
