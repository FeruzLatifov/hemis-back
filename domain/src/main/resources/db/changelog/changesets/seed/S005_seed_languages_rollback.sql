-- Rollback S005: REMOVE SEEDED LANGUAGES
DELETE FROM languages WHERE created_by = 'system';
