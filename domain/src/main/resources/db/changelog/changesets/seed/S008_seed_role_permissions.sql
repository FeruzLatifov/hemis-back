-- S008: Add roles.manage permission for Role CRUD admin
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES (gen_random_uuid(), 'roles', 'manage', 'roles.manage', 'Manage roles', 'Create, edit, and delete roles', 'ADMIN', now())
ON CONFLICT (code) DO NOTHING;

-- Assign roles.manage to SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'SUPER_ADMIN' AND p.code = 'roles.manage'
    AND NOT EXISTS (
        SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
    );
