-- Rollback for S009_seed_universities_translations.sql
-- Delete message keys added in S009 (ON DELETE CASCADE removes translations)

DELETE FROM system_message WHERE message_key IN (
    -- Labels
    'General', 'Legal', 'Officials', 'Property', 'History',
    'Location', 'Contacts', 'Documents', 'Document', 'Document title',
    'Social links', 'Website', 'Feature flags',
    'HEMIS version', 'HEMIS configuration',
    'URLs', 'University site', 'Students', 'Teachers', 'UZBMB',
    'Map location', 'Map URL', 'Latitude', 'Longitude',
    'Extract from URL', 'Open in map', 'Get directions',
    'Paste Google Maps or Yandex Maps link',
    'District', 'Neighborhood', 'Postcode', 'SOATO',
    'INN', 'PINFL', 'Passport', 'OKED',
    'First name', 'Last name', 'Middle name', 'Birth date',
    'Note', 'Period', 'Share', 'Individual', 'Owners',
    'Rector', 'Accountant', 'Account', 'Opened',
    'Director (legal)', 'Director (legal representative)',
    'Legal form', 'Ownership form', 'Legal entity', 'Legal address',
    'Registration number', 'Registration date', 'Re-registration date',
    'Average employees', 'Last synced',
    'Bank accounts', 'Bank info',
    'Founders', 'Lifecycle',
    'Decree', 'Decree number', 'Effective date',
    'Successor university code',
    'Land area', 'Building area', 'Cadastre value', 'Real estate',
    'Valid from', 'Valid to',
    'Grading system', 'Allow transfer outside',
    'Accreditation details', 'Additional details',
    'Short description of the university',
    'e.g. 301', 'Current', 'Closed', 'Restricted', 'selected',
    -- Actions
    'Appoint', 'Appoint official', 'Dismiss', 'Remove',
    'Save profile', 'Add document', 'Back to list',
    'Delete university', 'Export selected',
    'Search in external database', 'Sync external data',
    'Confirm dismissal', 'Select row', 'Select district first',
    'Compact view', 'Comfortable view', 'Click to copy',
    -- Messages
    'Searching...', 'Syncing...', 'Person found', 'Copied',
    'Data refreshed', 'Excel file downloading...', 'University not found',
    'No lifecycle events', 'No documents yet', 'No universities have been added yet',
    'No data. Use Sync in Edit page.',
    'No data. Use Edit page to add contacts, social links, and documents.',
    'No officials. Use Edit page to appoint.',
    'Person not found locally. Enter document or birth date to search external database.',
    'Source: university sync. Use Edit to appoint via ministry.',
    'Status is being changed. Please provide details:',
    -- Confirm
    'Dismissal confirmation',
    'Are you sure you want to delete this university? This action cannot be undone.',
    -- Feature flags
    'OneID login', 'Add foreign student', 'Add transfer student', 'Add academic mobile student',
    -- Document types
    'LICENSE', 'ACCREDITATION', 'CHARTER', 'OTHER',
    -- Activity statuses (hardcoded enum)
    'Merged', 'License revoked', 'Suspended', 'Reorganized'
);
