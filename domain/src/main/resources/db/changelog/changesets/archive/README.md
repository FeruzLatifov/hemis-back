# Archive - Deprecated Changesets

> **WARNING**: This folder contains **DEPRECATED** changesets that are no longer used.

## Status: ARCHIVED (Do Not Use)

These files were part of the initial migration strategy and have been superseded by the
new granular structure in `schema/`, `seed/`, and `migration/` folders.

## Why Keep These Files?

1. **Historical Reference**: Understanding past migration decisions
2. **Debugging**: Comparing old vs new implementations
3. **Rollback Safety**: If needed to understand what was previously deployed

## File Mapping (Old → New)

| Old File | New Location |
|----------|--------------|
| `01-auth-system.sql` | `schema/V001-V005*.sql` + `seed/S001-S004*.sql` |
| `02-migrate-old-users.sql` | `migration/M001_migrate_old_hemis_users.sql` |
| `03-all-translations.sql` | `seed/S006_seed_translations.sql` |
| `04-menu-permissions.sql` | `seed/S003b_seed_permissions_menu.sql` |
| `05-menu-system.sql` | `schema/V008_create_menus.sql` |
| `06-language-config.sql` | `schema/V009-V010*.sql` + `seed/S005*.sql` |

## Do NOT

- Do **not** reference these files in `db.changelog-master.yaml`
- Do **not** run these files directly against any database
- Do **not** modify these files

## Cleanup

These files can be safely deleted after:
1. All environments have been migrated to the new structure
2. Team has confirmed no rollback to old structure is needed
3. Minimum 30 days have passed since last production deployment

---
*Last updated: 2025-11-26*
