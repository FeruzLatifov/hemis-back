-- =====================================================
-- S043 ROLLBACK: remove the 'Residency' and 'Unified speciality classifier' translations
-- =====================================================
-- Hard delete of the message and its four translation rows. Safe because this changeset created
-- them: no earlier seed defines 'Residency', so nothing is left below its own baseline. The
-- translations go first — system_message_translation.message_id FKs into system_message.
-- S010's 'Unified bachelor and master speciality classifier' is untouched: this changeset never
-- deleted it, only stopped the page from rendering it, so the rollback has nothing to restore.
-- =====================================================

DELETE FROM system_message_translation
 WHERE message_id IN (SELECT id FROM system_message
                       WHERE message_key IN ('Residency', 'Unified speciality classifier'));

DELETE FROM system_message
 WHERE message_key IN ('Residency', 'Unified speciality classifier');
