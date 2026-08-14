-- Rollback S020: remove menu + role grants + permissions.
-- system_message ('OTM API clients') is intentionally kept (S019 convention — shared i18n).
DELETE FROM menu WHERE code = 'sys-oauth-clients';

DELETE FROM role_permission rp
USING permission p
WHERE rp.permission_id = p.id
  AND p.code IN ('oauth-clients.view', 'oauth-clients.manage');

DELETE FROM permission
WHERE code IN ('oauth-clients.view', 'oauth-clients.manage');
