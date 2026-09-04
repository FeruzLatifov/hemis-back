-- =====================================================
-- S045 ROLLBACK: remove the identity-block translations
-- =====================================================
-- Hard delete of both messages and their translation rows. Safe because this changeset created
-- them: no earlier seed defines either key, so nothing drops below its own baseline. Translations
-- go first — system_message_translation.message_id FKs into system_message.
-- =====================================================

DELETE FROM system_message_translation
 WHERE message_id IN (SELECT id FROM system_message
                       WHERE message_key IN ('Identity',
                                             'Identity data is set when the account is created and cannot be changed'));

DELETE FROM system_message
 WHERE message_key IN ('Identity',
                       'Identity data is set when the account is created and cannot be changed');
