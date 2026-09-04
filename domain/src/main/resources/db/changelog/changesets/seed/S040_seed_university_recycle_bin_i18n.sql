-- =====================================================
-- S040: SEED TRANSLATIONS — university recycle bin (deleted universities + restore)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-02
-- Purpose:
--   /institutions/universities gets the same recycle bin /classifiers/speciality has had since
--   S036: a "Deleted" button that opens the list of soft-deleted rows (University carries
--   @SQLRestriction("delete_ts IS NULL"), so a deleted OTM is invisible to every ordinary query)
--   and a Restore action that brings one back. Four NEW keys:
--     • 'Deleted universities' — the dialog title.
--     • 'The university is hidden everywhere and can be restored later' — the dialog subtitle and
--       the delete dialog's explanation, the university-shaped twin of the speciality line S036
--       seeded. It says what a soft delete actually does: the row stays, every list stops showing
--       it, and the bin is the way back. uz/oz follow S036 word for word: PASSIVE 'yashiriladi' /
--       'яширилади' ("is hidden"), not the reflexive 'yashirinadi' ("hides itself") an earlier
--       revision carried, plus the ablative 'hamma joydan'.
--     • 'University restored' — the success toast.
--     • 'Are you sure you want to delete this university? It will be hidden everywhere and can be
--       restored later.' — the confirm text. S009 seeded "…This action cannot be undone." and that
--       sentence is now FALSE: the delete is reversible from the bin, and a confirm dialog that
--       overstates the damage teaches people to distrust confirm dialogs. The old S009 key is left
--       untouched (an applied changeset owns its rows) but nothing renders it any more.
--
--   No key for the button itself HERE: the button label is 'Deleted items', seeded by S041. An
--   earlier revision of this changeset reused S039's 'Deleted' (the audit-log action chip) for it,
--   which reads right in uz/oz ("O'chirilgan") but wrong in ru — 'Удалено' is a short passive
--   participle ("it was deleted"), correct stamped on a log row, wrong on a button that means
--   "the deleted ones". S041 owns the separate short button key; 'Deleted' stays the audit chip.
--   The other twelve strings the two dialogs need — Total, Loading..., Failed to load data,
--   Nothing deleted yet, Code, Name, Deleted at, Deleted by, Actions, Restore, Close, INN — are
--   already seeded (S006/S009/S010/S036) and are reused verbatim.
--
--   NEW seed: S006/S009/S010/S032..S039 are applied in production (central_hemis) and applied
--   changesets are never edited. system_message is the single source of truth — `sync:translations`
--   rewrites the frontend JSONs (en/oz/ru/uz) from it, so an unseeded key disappears silently at
--   the next sync.
--   ru register: 'университет', NOT 'вуз' — the S009 strings that surround these in the same flow
--   ("Удалить университет", "Университет успешно удалён") already say 'университет', and one
--   interaction must not name the same thing two ways.
-- Pattern: S039 (5-argument _seed_msg helper defined in S006; en-US = the key itself).
-- Safety: _seed_msg does ON CONFLICT (message_key) DO UPDATE — idempotent, runOnChange.
-- =====================================================

DO $$
BEGIN

-- ── The recycle-bin dialog ──
PERFORM _seed_msg('label', 'Deleted universities', 'O''chirilgan universitetlar', 'Ўчирилган университетлар', 'Удалённые университеты');
PERFORM _seed_msg('label', 'The university is hidden everywhere and can be restored later', 'Universitet hamma joydan yashiriladi, keyinchalik tiklash mumkin', 'Университет ҳамма жойдан яширилади, кейинчалик тиклаш мумкин', 'Университет скрывается везде и может быть восстановлен позже');
PERFORM _seed_msg('label', 'University restored', 'Universitet tiklandi', 'Университет тикланди', 'Университет восстановлен');

-- ── The delete confirmation — "cannot be undone" is no longer true ──
PERFORM _seed_msg('confirm', 'Are you sure you want to delete this university? It will be hidden everywhere and can be restored later.', 'Universitetni o''chirmoqchimisiz? U hamma joydan yashiriladi, keyinchalik "O''chirilgan" bo''limidan tiklash mumkin.', 'Университетни ўчирмоқчимисиз? У ҳамма жойдан яширилади, кейинчалик "Ўчирилган" бўлимидан тиклаш мумкин.', 'Удалить этот университет? Он будет скрыт везде, позже его можно восстановить из раздела "Удалённые".');

END $$;
