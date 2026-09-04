-- =====================================================
-- S038: ACCESS CONTROL MODEL (role tiers + dashboard access)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-29
-- Purpose: One changeset for one decision — who can do what. It replaces the two seeds this work
--          was first written as (S038 dashboard access, S039 role model); neither had reached any
--          environment beyond a developer database, so they are folded into this single seed
--          instead of shipping a repair on top of a repair.
--
-- Three human tiers:
--     SUPER_ADMIN      — everything. Break-glass + ownership of the platform itself.
--     ADMIN            — "Administrator": the day-to-day admin (code renamed from MINISTRY_ADMIN
--                        here). Everything EXCEPT the platform/security-critical actions listed
--                        below (~85% of the codes).
--     TECH_STAFF       — "Texnik xodim": technical staff, a NEW role. Classifier operator — adds
--                        and corrects rows, attaches them to OTMs; cannot approve, delete or
--                        restore. CLASSIFIER_MANAGER is a different job and is left untouched.
--
-- Why this boundary and not a percentage: the actions kept for SUPER_ADMIN are the ones whose
-- misuse cannot be undone from inside the app — granting yourself permissions (roles.*,
-- permissions.manage), rotating the 224-OTM integration secrets (webhook.*, oauth-clients.manage),
-- rewriting the platform's structure for everyone (settings.edit, menus), reading raw PINFL,
-- discarding queued events (outbox.manage) and deleting registry rows the OTMs will not resend
-- (students, teachers). Everything an administrator needs for daily work stays with the
-- administrator — including deleting a university, which is soft and reversible (see
-- universities.restore below).
-- This is the standard split in GCP/AWS/GitHub: an org admin runs the org, only the owner can
-- change what "admin" means.
--
-- Maker/checker: TECH_STAFF holds classifiers.speciality.create/edit but NOT .approve — and
-- HSpecialityService.update refuses to promote a row to APPROVED without that authority, so an
-- operator can prepare a speciality while only an administrator publishes it to 230 OTMs. Same for
-- deletion: classifiers.delete (new here) and classifiers.speciality.delete (which also gates
-- restore + the deleted list) stay with the administrator. universities.delete and its new
-- counterpart universities.restore are both administrator-level for the same reason: the delete is
-- soft, and the bin is the way back.
--
-- Dashboard: menu.code='dashboard' was the ONLY row seeded with permission = NULL (S011), and
-- MenuService.hasPermission(null, ...) returns TRUE for a NULL requirement — so "Bosh sahifa" was
-- rendered for EVERY authenticated user while GET /api/v1/web/dashboard/stats stayed gated by
-- hasAuthority('dashboard.view'). Both ends are fixed at the end of this seed; M014 then makes
-- menu.permission NOT NULL so a NULL can never re-open the hole, and therefore runs AFTER this.
--
-- Role CODES: S001/S004/S016/S020/S023 and M004/M005 are applied in production and are never
-- edited, so they still create and grant MINISTRY_ADMIN — they all run before this seed, which
-- renames it. Staff see role.name.
--
-- Strategy: IDEMPOTENT, and additive where duties live. Grants are INSERT ... ON CONFLICT DO NOTHING.
-- A re-run re-asserts the SECURITY boundary (the SUPER_ADMIN-only list stays off ADMIN and
-- TECH_STAFF; audit.view stays off everyone else) but does NOT strip what an administrator has since
-- assigned in the role editor: widening an operator's duties is that screen's whole purpose, and a
-- deploy silently undoing it is a bug, not a policy. The verification block follows the same rule:
-- it RAISEs only where this seed itself re-asserts the state unconditionally (SUPER_ADMIN holds
-- everything; the SUPER_ADMIN-only list stays off ADMIN; audit.view stays off everyone else) and
-- merely NOTICEs when Texnik xodim's duties have drifted from the seeded starting point — a seed
-- that aborts the deploy over a duty change would punish the workflow it was written to allow.
-- =====================================================

-- ---------- New permission: classifiers.delete ----------
-- Split out of classifiers.edit so a role can add/correct classifier rows without being able to
-- remove one. DELETE /api/v1/web/classifiers/{apiKey}/{code} now requires this code
-- (ClassifierWebController); create/update keep classifiers.edit.
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('classifiers', 'delete', 'classifiers.delete', 'Delete Classifier Item',
        'Delete (soft) a classifier item — separate from classifiers.edit so operators can add and correct without removing',
        'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- ---------- New permission: audit.entity.view ----------
-- Reading the whole audit journal and reading what happened to the row in front of you are two
-- different capabilities. audit.view is the first — every user, every IP, every before/after
-- snapshot — and stays with the two admin tiers. This is the second: one record's history (and one
-- owner's, for hard-deleted link rows), limited to the registries an operator curates. Without it a
-- classifier operator cannot answer "who changed this row" about their own work.
-- resource is `audit.history`, not `audit`: the role editor groups chips by RESOURCE and labels the
-- card from its last segment, so sharing `audit` with audit.view produced one card carrying two
-- identical "Ko'rish" chips. Its own resource gives it its own card ("Tarix"), the same way
-- classifiers.speciality is separate from classifiers.
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('audit.history', 'view', 'audit.history.view', 'View Record History',
        'See the audit trail of a single record (or one owner) in the registries the holder curates — not the full audit log',
        'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- The pre-release name of the same capability. It existed only in developer databases, so it is
-- removed rather than carried forever as an alias.
DELETE FROM role_permission WHERE permission_id IN (SELECT id FROM permission WHERE code = 'audit.entity.view');
DELETE FROM permission WHERE code = 'audit.entity.view';

-- ---------- New permission: universities.restore ----------
-- Split out of universities.delete, and NOT folded into it, because the two carry opposite risk:
-- delete takes an OTM out of every list, restore puts it back. A role can legitimately be trusted
-- with one and not the other — an operator who may repair an accidental deletion is not thereby
-- allowed to cause one, and the reverse split (delete without restore) is what a maker/checker
-- arrangement looks like. Sharing one code would have made that choice impossible to express.
-- Gates the two recycle-bin endpoints: GET /api/v1/web/registry/universities/deleted and
-- POST /api/v1/web/registry/universities/{code}/restore.
-- `restore` is a NEW permission.action value: M015 (registered directly before this seed) widens
-- chk_permission_action to accept it, and PermissionAction.RESTORE + isWritePermission() ship with
-- it — without the enum entry the converter throws while loading this permission at LOGIN.
-- Who holds it: SUPER_ADMIN (tier 1 takes everything) and ADMIN — automatically, because ADMIN is
-- granted every code NOT in s038_super_admin_only and this one is deliberately absent from that
-- list. It is NOT in the TECH_STAFF list: that role is "no approve, no delete, no restore".
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'restore', 'universities.restore', 'Restore University',
        'Open the deleted-universities bin and bring a soft-deleted OTM back — separate from universities.delete, which is the opposite risk',
        'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- ---------- The two lists this seed is built from ----------
-- Kept in temp tables so the boundary is written once and the grants, the strips and the
-- verification all read the same list (a second copy is a second chance to get it wrong).
DROP TABLE IF EXISTS s038_super_admin_only;
CREATE TEMP TABLE s038_super_admin_only (code VARCHAR(255) PRIMARY KEY);
INSERT INTO s038_super_admin_only (code) VALUES
    -- Privilege escalation: an admin must not be able to widen "admin".
    ('roles.create'), ('roles.edit'), ('roles.delete'), ('roles.manage'), ('permissions.manage'),
    -- Integration keys and the channel itself: HMAC secrets and webhook URLs for 224 OTMs.
    ('webhook.create'), ('webhook.update'), ('webhook.delete'), ('webhook.manage'),
    ('oauth-clients.manage'),
    -- The platform's own structure. Translations are NOT here: they are content, an administrator
    -- edits them, and the translations page shows its write controls to anyone who can open it.
    ('settings.edit'), ('system.menus.manage'),
    -- Raw national ID (IAM P1 read-gate; S004 already strips it from every other role).
    ('pinfl.view'),
    -- Dropping a queued event loses data that no OTM will resend.
    ('outbox.manage'),
    -- Registry deletions: the center aggregates these rows, the OTM owns them, and no OTM will
    -- resend a student or a teacher the center dropped — so those two stay here.
    -- universities.delete does NOT: deleting an OTM is now a SOFT delete (delete_ts), reversible
    -- from the recycle bin by universities.restore. The reason this list exists — "misuse cannot be
    -- undone from inside the app" — simply does not apply to it any more, and an administrator who
    -- runs the university registry every day should not need break-glass to remove a duplicate row.
    ('students.delete'), ('teachers.delete');

DROP TABLE IF EXISTS s038_tech_staff;
CREATE TEMP TABLE s038_tech_staff (code VARCHAR(255) PRIMARY KEY);
INSERT INTO s038_tech_staff (code) VALUES
    -- Navigation + landing page (the dashboard section below grants every human role dashboard.view).
    ('dashboard.view'), ('system.menu.view'),
    -- Classifiers: see everything, add and correct.
    ('classifiers.view'), ('classifiers.edit'),
    ('classifiers.diploma.view'), ('classifiers.education.view'), ('classifiers.employee.view'),
    ('classifiers.financial.view'), ('classifiers.general.view'), ('classifiers.science.view'),
    ('classifiers.structure.view'), ('classifiers.student.view'), ('classifiers.study.view'),
    ('classifiers.speciality.view'), ('classifiers.speciality.create'), ('classifiers.speciality.edit'),
    -- Attaching a speciality to an OTM is classifier work; detaching (…delete) is not granted.
    ('institutions.view'),
    ('institutions.speciality-attachments.view'), ('institutions.speciality-attachments.create'),
    -- The history of the rows they curate — their own work, not the ministry-wide journal.
    ('audit.history.view');

-- ---------- Role code: MINISTRY_ADMIN -> ADMIN ----------
-- The ministry runs one admin tier, so the code says ADMIN. S001 (create), S004/S016/S020/S023 and
-- M004/M005 (grants) still speak of MINISTRY_ADMIN — they are applied in production and applied
-- changesets are never edited — but every one of them runs BEFORE this seed, so on a fresh database
-- the row is created under the old code and renamed here, and on an existing database it is renamed
-- in place (the id, and therefore every user_role row, is untouched).
--
-- Merge-safe: if both rows exist (S001 is runOnChange — editing it re-creates MINISTRY_ADMIN after
-- a previous rename), the legacy row's users are moved to ADMIN and the legacy row is soft-deleted,
-- which also frees the unique code index (it is partial: ... WHERE deleted_at IS NULL). Its grants
-- are dropped rather than merged: ADMIN's set is re-established below, from the policy, not from
-- whatever the legacy row happened to hold.
UPDATE user_role ur
   SET role_id = (SELECT id FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL)
 WHERE ur.role_id = (SELECT id FROM role WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL)
   AND EXISTS (SELECT 1 FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL)
   AND NOT EXISTS (
        SELECT 1 FROM user_role dup
         WHERE dup.user_id = ur.user_id
           AND dup.role_id = (SELECT id FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL));

-- Users who already held ADMIN keep it; their duplicate legacy link is dropped.
DELETE FROM user_role
 WHERE role_id = (SELECT id FROM role WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL)
   AND EXISTS (SELECT 1 FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL);

DELETE FROM role_permission
 WHERE role_id = (SELECT id FROM role WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL)
   AND EXISTS (SELECT 1 FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL);

UPDATE role SET
    active = FALSE,
    deleted_at = CURRENT_TIMESTAMP,
    deleted_by = 'system'
 WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL
   AND EXISTS (SELECT 1 FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL);

-- The normal path: no ADMIN row yet, so the legacy row simply becomes it.
UPDATE role SET
    code = 'ADMIN',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
 WHERE code = 'MINISTRY_ADMIN' AND deleted_at IS NULL
   AND NOT EXISTS (SELECT 1 FROM role WHERE code = 'ADMIN' AND deleted_at IS NULL);

-- ---------- Role identity ----------
UPDATE role SET
    name = 'Administrator',
    description = 'Vazirlik administratori — kundalik boshqaruv. SUPER_ADMIN''da qoladigan platforma/xavfsizlik amallaridan tashqari barcha huquqlar (rol/ruxsat tahriri, integratsiya sirlari, tizim sozlamalari, PINFL, reyestr o''chirish).',
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system'
WHERE code = 'ADMIN' AND deleted_at IS NULL;

-- Tier 3 is a NEW role, not a re-labelled CLASSIFIER_MANAGER: the two jobs are different.
-- CLASSIFIER_MANAGER stays exactly as it is (its own name, its own grants) and is not touched by
-- this seed at all — technical staff get their own role, which starts narrow and widens one
-- permission at a time as duties are assigned.
INSERT INTO role (id, code, name, description, role_type, active, created_by)
VALUES (
    gen_random_uuid(),
    'TECH_STAFF',
    'Texnik xodim',
    'Texnik xodim (operator) — klassifikatorlarni ko''radi, qo''shadi va tahrirlaydi, OTM''ga biriktiradi. Tasdiqlay olmaydi (tarqatish), o''chira va tiklay olmaydi. Vazifalari kengaygan sari ruxsat qo''shiladi.',
    'CUSTOM',
    TRUE,
    'system'
)
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    role_type = EXCLUDED.role_type,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- ---------- Tier 1: SUPER_ADMIN — everything ----------
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'SUPER_ADMIN' AND r.deleted_at IS NULL AND p.deleted_at IS NULL
ON CONFLICT DO NOTHING;

-- ---------- Tier 2: ADMIN — everything except the reserved list ----------
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'ADMIN' AND r.deleted_at IS NULL AND p.deleted_at IS NULL
  AND p.code NOT IN (SELECT code FROM s038_super_admin_only)
ON CONFLICT DO NOTHING;

DELETE FROM role_permission rp
USING role r, permission p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.code = 'ADMIN'
  AND p.code IN (SELECT code FROM s038_super_admin_only);

-- ---------- Tier 3: TECH_STAFF ("Texnik xodim") — exactly its list ----------
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'TECH_STAFF' AND r.deleted_at IS NULL AND p.deleted_at IS NULL
  AND p.code IN (SELECT code FROM s038_tech_staff)
ON CONFLICT DO NOTHING;

-- The list is a STARTING POINT for this role, not a cage.
--
-- It used to be re-asserted with a DELETE of everything outside it, which meant a duties change made
-- in the role editor — the screen that exists for exactly that — silently disappeared at the next
-- deploy, with nothing to explain why. Duties widen "one permission at a time as work is assigned",
-- and that assignment belongs to the administrator, not to a redeploy.
--
-- What a re-run still enforces is the part that is NOT a duty but a security boundary: the
-- SUPER_ADMIN-only list stays off ADMIN (above) and audit.view stays off everyone else (below).
-- Those two strips are deliberate; this one was not.
DELETE FROM role_permission rp
USING role r, permission p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND r.code = 'TECH_STAFF'
  AND p.code IN (SELECT code FROM s038_super_admin_only);

-- ---------- audit.view is an admin-tier capability ----------
-- The audit log carries usernames, IP addresses and the before/after of everyone's work, and it is
-- the only place that answers "who approved this". S004 grants every `action = 'view'` permission to
-- INSPECTOR (and the VIEWER tier), which swept this one along with the harmless ones. The two admin
-- tiers keep it; everyone else loses it. This IS the boundary now: AuditLogController gates on the
-- permission alone, because a USER token carries no ROLE_* authority for a role check to test.
DELETE FROM role_permission rp
USING role r, permission p
WHERE rp.role_id = r.id
  AND rp.permission_id = p.id
  AND p.code = 'audit.view'   -- exactly this code; audit.entity.view is the narrow one and stays
  AND r.code NOT IN ('SUPER_ADMIN', 'ADMIN');

-- ---------- Verification ----------
DO $$
DECLARE
    total INTEGER; sa INTEGER; ma INTEGER; cm INTEGER; leaked INTEGER; missing INTEGER;
BEGIN
    SELECT COUNT(*) INTO total FROM permission WHERE deleted_at IS NULL;

    SELECT COUNT(*) INTO sa FROM role_permission rp JOIN role r ON r.id = rp.role_id
     WHERE r.code = 'SUPER_ADMIN' AND r.deleted_at IS NULL;
    IF sa < total THEN
        RAISE EXCEPTION 'S038: SUPER_ADMIN holds %/% permissions — the break-glass role must hold all of them', sa, total;
    END IF;

    -- Tier 2 must not hold anything reserved for tier 1.
    SELECT COUNT(*) INTO leaked
      FROM role_permission rp
      JOIN role r ON r.id = rp.role_id
      JOIN permission p ON p.id = rp.permission_id
     WHERE r.code = 'ADMIN' AND r.deleted_at IS NULL AND p.code IN (SELECT code FROM s038_super_admin_only);
    IF leaked > 0 THEN
        RAISE EXCEPTION 'S038: Administrator still holds % SUPER_ADMIN-only permission(s)', leaked;
    END IF;
    SELECT COUNT(*) INTO ma FROM role_permission rp JOIN role r ON r.id = rp.role_id
     WHERE r.code = 'ADMIN' AND r.deleted_at IS NULL;

    -- Tier 3: REPORTED, not enforced. The strip above was removed on purpose ("a STARTING POINT for
    -- this role, not a cage") — an administrator widening or narrowing Texnik xodim in the role
    -- editor is the intended workflow, and this changeset is runOnChange, so it re-runs on the next
    -- release for an unrelated edit. An EXCEPTION here would abort the whole Liquibase update and
    -- keep the app from starting on a database whose only "fault" is that someone used that screen
    -- as designed. Drift is worth SAYING; it is not worth a failed deploy. Every RAISE EXCEPTION left
    -- in this block tests something the statements above it have just re-asserted unconditionally
    -- (SUPER_ADMIN completeness, the SUPER_ADMIN-only list off ADMIN, audit.view off everyone
    -- else), so none of them can be tripped by a legitimate role-editor change.
    SELECT COUNT(*) INTO leaked
      FROM role_permission rp
      JOIN role r ON r.id = rp.role_id
      JOIN permission p ON p.id = rp.permission_id
     WHERE r.code = 'TECH_STAFF' AND r.deleted_at IS NULL AND p.code NOT IN (SELECT code FROM s038_tech_staff);
    IF leaked > 0 THEN
        RAISE NOTICE 'S038: Texnik xodim holds % permission(s) beyond its seeded list (assigned in the role editor — kept)', leaked;
    END IF;

    SELECT COUNT(*) INTO missing
      FROM s038_tech_staff t
     WHERE NOT EXISTS (
        SELECT 1 FROM role_permission rp
          JOIN role r ON r.id = rp.role_id
          JOIN permission p ON p.id = rp.permission_id
         WHERE r.code = 'TECH_STAFF' AND p.code = t.code);
    IF missing > 0 THEN
        RAISE NOTICE 'S038: Texnik xodim is missing % permission(s) from its seeded list (revoked in the role editor, or the permission row is absent)', missing;
    END IF;
    SELECT COUNT(*) INTO cm FROM role_permission rp JOIN role r ON r.id = rp.role_id
     WHERE r.code = 'TECH_STAFF' AND r.deleted_at IS NULL;

    -- The audit log is admin-tier reading; a leak here is how an inspector reads everyone's history.
    SELECT COUNT(*) INTO leaked
      FROM role_permission rp
      JOIN role r ON r.id = rp.role_id
      JOIN permission p ON p.id = rp.permission_id
     WHERE p.code = 'audit.view' AND r.deleted_at IS NULL AND r.code NOT IN ('SUPER_ADMIN', 'ADMIN');
    IF leaked > 0 THEN
        RAISE EXCEPTION 'S038: audit.view leaked to % role(s) outside the admin tiers', leaked;
    END IF;

    RAISE NOTICE 'S038: SUPER_ADMIN=%/%, Administrator=%/% (%.0f%%), Texnik xodim=%/%',
        sa, total, ma, total, (ma::numeric / total * 100), cm, total;
END $$;

DROP TABLE IF EXISTS s038_super_admin_only;
DROP TABLE IF EXISTS s038_tech_staff;

-- =====================================================
-- Dashboard access (folded in from the former S038)
-- =====================================================
-- ---------- Grant: the one human role missing dashboard.view ----------
-- Machine roles are untouched: OTM_API already holds it (S004), EXTERNAL_API deliberately does not.
INSERT INTO role_permission (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM role r CROSS JOIN permission p
WHERE r.code = 'CLASSIFIER_MANAGER'
  AND p.code = 'dashboard.view'
ON CONFLICT DO NOTHING;

-- ---------- The menu row names its permission ----------
UPDATE menu
   SET permission = 'dashboard.view',
       updated_at = CURRENT_TIMESTAMP,
       updated_by = 'system'
 WHERE code = 'dashboard'
   AND permission IS DISTINCT FROM 'dashboard.view';

-- ---------- Verification ----------
DO $$
DECLARE
    null_permission_count INTEGER;
    unknown_permissions   TEXT;
    missing_grant_roles   TEXT;
BEGIN
    SELECT COUNT(*) INTO null_permission_count
      FROM menu
     WHERE permission IS NULL AND deleted_at IS NULL;

    SELECT string_agg(m.permission, ', ' ORDER BY m.permission) INTO unknown_permissions
      FROM (SELECT DISTINCT permission FROM menu WHERE deleted_at IS NULL AND permission IS NOT NULL) m
     WHERE NOT EXISTS (
           SELECT 1 FROM permission p WHERE p.code = m.permission AND p.deleted_at IS NULL);

    SELECT string_agg(r.code, ', ' ORDER BY r.code) INTO missing_grant_roles
      FROM role r
     -- deleted_at IS NULL, and no MINISTRY_ADMIN: the merge branch above soft-deletes that row after
     -- stripping its grants, so counting it would make this seed abort on the very path it exists to
     -- support ("the role has no dashboard.view" — of course, it no longer exists).
     WHERE r.deleted_at IS NULL
       AND r.code IN ('SUPER_ADMIN', 'ADMIN', 'INSPECTOR', 'VIEWER', 'REPORT_VIEWER', 'CLASSIFIER_MANAGER', 'TECH_STAFF')
       AND NOT EXISTS (
           SELECT 1
             FROM role_permission rp
             JOIN permission p ON p.id = rp.permission_id
            WHERE rp.role_id = r.id AND p.code = 'dashboard.view');

    RAISE NOTICE 'S038: DASHBOARD ACCESS SEEDED';
    RAISE NOTICE '   menu rows without a permission: % (expected 0)', null_permission_count;
    RAISE NOTICE '   menu permissions with no permission row: %', COALESCE(unknown_permissions, 'none');
    RAISE NOTICE '   human roles still missing dashboard.view: %', COALESCE(missing_grant_roles, 'none');

    -- NOTICE, not EXCEPTION — the same reasoning as the TECH_STAFF checks above. This seed grants
    -- dashboard.view only to CLASSIFIER_MANAGER (the one role that was missing it); for the other
    -- human roles the grant comes from S004 and is a DUTY, not a security boundary. An administrator
    -- revoking it in the role editor is a legitimate act, and a deploy that then aborts turns that
    -- act into an outage with nothing in the log to explain it. Report the drift, do not block.
    IF missing_grant_roles IS NOT NULL THEN
        RAISE NOTICE 'S038: human role(s) % lack dashboard.view - the landing page answers 403 for them', missing_grant_roles;
    END IF;
END $$;
