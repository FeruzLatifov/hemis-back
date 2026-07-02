-- =====================================================
-- Rollback S010: REMOVE FRONTEND-SOURCED TRANSLATIONS
-- =====================================================
-- Removes translations seeded by S010 (frontend missing keys).
-- We cannot rely on category alone (S006 owns the same categories), so
-- we delete by the explicit message_key list this seed introduced.
-- Safe rollback — checks if tables exist first.
-- =====================================================

DO $$
DECLARE
    translations_exists BOOLEAN;
    messages_exists BOOLEAN;
    _deleted_translations BIGINT := 0;
    _deleted_messages BIGINT := 0;
    -- The exact set of keys S010 inserts. Keep this list in sync with the seed.
    _keys TEXT[] := ARRAY[
        -- ACTIONS
        'Add to favorites','Remove from favorites','Back to login','Back to roles','Back to users',
        'Close menu','Open menu','Toggle theme','Show password','Hide password','Show all','Show',
        'Try again','Go back','Go to Dashboard','Send reset link','Reset password','Set new password',
        'Resend email','Request a new reset link','Select language','Generate properties',
        'Regenerate properties files','Manage translations','Create role','Edit role','Delete role',
        'Remove filter','Yes, clear','Yes, generate',
        -- LABELS
        'Account lock','Account status','Activity','Activity details','All actions','All events',
        'All pages','Recent','Results','Quick search','Search pages...','Search pages (Ctrl+K)',
        'Search by code...','Search by PINFL...','Search permissions...','Search roles...',
        'Key or text...','Breadcrumb','Sidebar','Main header','Main navigation','User menu',
        'Notifications','Quick links','Need help?','Skip to main content','Light','Dark',
        'Other languages','Uzbek (latin)','Cyrillic text (oz-UZ)','Russian translation (ru-RU)',
        'Primary text (uz-UZ)','Primary language, required','Optional, but recommended',
        'Optional description','For grouping translations (menu, button, label...)',
        'Example: menu, button, label, error, validation','If active, frontend will show the translation',
        'Used in code, cannot be changed','View and edit translation key-value pairs','Name (English)',
        'Role name','Role details','Permissions','Current status','Changed fields','Created',
        'Endpoint','Entity','Entity type','Event','Event type','Errors','Error type','Failure reason',
        'Old value','New value','Total activities','Total errors','Total logins','Top user','Synced',
        'Group','Groups','Group count','Group name','Group ID','No groups have been added yet','Per page','Shown','Gender','Education form','Education year',
        'Enrollment records','Living area','Object area','Utility area',
        'Cadastral cost','Share sum','Successor','Duplicate analysis','Column settings',
        'Message','Text','Version','Security','University','University Information',
        'University directions','User information','Profile saved','Today, {{time}}',
        -- AUTH
        'Check your email','Enter your email and we will send you a reset link',
        'Enter your new password below','Reset link sent to your email','Resend in {{seconds}}s',
        'Password has been reset successfully','Session expired due to inactivity',
        'This account is currently locked','Checking...','HEMIS. All rights reserved.','Ministry Portal',
        -- VALIDATION
        'Required','Email already in use','Username already exists','Invalid email format',
        'Invalid or missing reset token','Invalid reset link','Invalid role name',
        'Code must be uppercase letters, digits, and underscores','Name must be 2-100 characters',
        'Password must be at least 6 characters','Phone number must be in format +998XXXXXXXXX',
        -- MESSAGES
        'No data available','No active records','No duplicate translations found',
        'No faculties have been added yet','No departments have been added yet',
        'Department count','Department type',
        -- Attached specialities registry (university speciality CRUD card)
        'University specialities','Add attached speciality','Edit attached speciality',
        'Education type','Speciality level','Speciality','Bachelor','Master','Ordinatura','Doctoral',
        'No attached specialities have been added yet','Attached speciality created',
        'Attached speciality updated','Attached speciality deleted','Delete attached speciality?',
        -- Diploma blanks + Blank distribution registry cards ('Number' owned by student registry — excluded)
        'Diploma blanks','Blank distribution','Blank code','Series','Start number','End number',
        'Quantity','Blank category','Received date','Distribution date','Supplier',
        'Add distribution','Edit distribution','No diploma blanks have been added yet',
        'No distributions have been added yet',
        'No permissions available','No permissions found',
        'No roles found','No roles have been created yet','No students found','No translations found',
        'Cache cleared','Cache cleared successfully','JSON files downloaded',
        'JSON files downloading...','External data synced successfully','Lifecycle event added',
        'Official appointed successfully','Official removed','Translation activated',
        'Translation deactivated','University successfully created','University successfully deleted',
        'University successfully updated','Role successfully created','Role successfully deleted',
        'Role successfully updated','Permissions and translations updated. Page reloading...',
        'This page is under development',
        'Teacher management features will be available once the backend API is ready',
        'You have unsaved changes. Are you sure you want to leave?','Connection restored',
        'Your internet connection is back','No internet connection','Please check your network connection',
        'Please try again later','Please try again after {{seconds}} seconds','Too many requests',
        -- REPORTS (excluding 7 keys owned by S006: Student/Teacher/University reports,
        --           Scientific activity/publications/projects, Intellectual property)
        'Students report','Teachers report','Institutions report','Academic report',
        'Scientific report','Economic report','Dissertation defenses','Patents, licenses',
        'Scopus, Web of Science publications','Local and international projects',
        'Candidate of Sciences, Doctor of Sciences','Doctor of Science, PhD, DSc',
        'Bachelor, Master, PhD distribution','Total students count and distribution',
        'Grant and contract students','Distribution by regions','Department employees',
        'Professor, Associate professor statistics','General indicators','Main HEI statistics',
        'HEI rating and comparison','Institute, University, Academy','State, Private, Joint',
        'Form of ownership','By education type','By payment type','By region',
        'By scientific degrees','By academic titles','By experience','By work experience',
        'By departments','By organizational form','By rating',
        -- Plural-aware count keys (i18next CLDR suffixes for ru/uz/oz/en).
        '{{count}} parameters','{{count}} parameters_one','{{count}} parameters_few',
        '{{count}} parameters_many','{{count}} parameters_other',
        '{{count}} students found','{{count}} students found_one','{{count}} students found_few',
        '{{count}} students found_many','{{count}} students found_other',
        '{{count}} groups found_one','{{count}} groups found_few',
        '{{count}} groups found_many','{{count}} groups found_other',
        '{{count}} specialities found_one','{{count}} specialities found_few',
        '{{count}} specialities found_many','{{count}} specialities found_other',
        'Must be at least {{count}} characters_one','Must be at least {{count}} characters_few',
        'Must be at least {{count}} characters_many','Must be at least {{count}} characters_other',
        'Certificates','Scholarships','Qualifications','GPA rating',
        -- ERRORS
        'Page not found','Sorry, the page you''re looking for doesn''t exist or has been moved.',
        'If you believe this is an error, please contact the system administrator.',
        'An error occurred while loading this page. Please try again.',
        'Failed to add lifecycle event','Failed to appoint official','Failed to change password',
        'Failed to create role','Failed to create user','Failed to delete role',
        'Failed to delete user','Failed to load translations','Failed to load university information',
        'Failed to remove official','Failed to save profile','Failed to sync external data',
        'Failed to toggle active status','Failed to unlock account','Failed to update role',
        'Failed to update status','Failed to update user','Error checking duplicates',
        'Error clearing cache','Error downloading JSON','Error generating properties',
        'Role code already exists','Role not found','User not found','Unknown error',
        'You do not have permission to perform this action',
        -- CONFIRM
        'Are you sure you want to {{action}} user','Are you sure you want to unlock user',
        -- SOCIAL
        'Facebook','Instagram','Telegram','Twitter','LinkedIn','YouTube',
        -- STUDENT REGISTRY CARDS (Diplomas, Scholarships, Certificates)
        'Student','Diploma number','Register number','Register date','Graduation date',
        'Average grade','Total credit','Admission year','Verified','No diplomas have been added yet',
        'Scholarship category','Scholarship type','Payment form','Decree','Start date','End date',
        'Semester','Monthly amounts','Amount','No scholarships have been added yet',
        'Certificate type','Certificate name','Certificate grade','Certificate subject',
        'Serial number','Issue date','Valid until','No certificates have been added yet',
        -- SCIENCE REGISTRY CARDS (Researchers, Scientific projects, publications, Methodical)
        -- ('Scientific projects','Scientific publications' owned by S006 — excluded)
        'Researchers','Methodical publications','Full name','Student ID number','Science branch',
        'Dissertation theme','Doctoral student type','Accepted date',
        'No researchers have been added yet','Project number','Project type','Contract number',
        'Contract date','No scientific projects have been added yet','Authors','Author count',
        'Source','Issue year','Publication type','No publications have been added yet',
        'Publisher','No methodical works have been added yet',
        -- ANALYTICS REPORT CARDS (KPIs, block titles, columns, filters)
        -- ('By education type','By region','Education year' owned above — excluded)
        'Total students','Grant','Contract','Male','Female','By education form','By gender',
        'By payment form','Top universities','Students count','Total institutions','Faculties',
        'Cathedras','By ownership','By university type','University structure','Total publications',
        'Total projects','Doctoral students','Publications by type','Publications by university',
        'Projects by type','Projects by university','Publications','Projects','Total teachers',
        'PhD holders','Professors','By academic degree','By academic rank','By age','By university',
        'Teachers count','No report data available',
        -- RATING CARDS (menu titles + University/Publications/Projects/Doctoral students/
        --   Top universities/Total publications/Total projects/By university reused — excluded)
        'Rank','Total','Indicators','Universities ranked','Top university',
        'Average score','Average GPA','Debtors','Students counted',
        -- NEW REGISTRY CARDS (Employee jobs, Institution specialities, Dissertation defense,
        --   Publication property, Research activity). 'Employee jobs'/'Scientific activity' (S006),
        --   'Decree number' (S009), 'Speciality code'/'Speciality name' (S006) excluded.
        'Institution specialities','Dissertation defense','Employee','Employee type','Employee form',
        'Job start date','Job end date','Defense date','Defense place','Patent type','Property date',
        'Number','H-index','Scientific work count','Reference count','Scholar database','Link',
        'No records have been added yet',
        -- ACADEMIC + ECONOMIC REPORTS. 'Average score','Debtors','University','By education type',
        --   'By gender','Total','Top universities','Academic report','Economic report' reused — excluded.
        'Average attendance','Universities covered','Top universities by average score',
        'Per-university academic performance','Absentee students','Total graduates','Laboratories',
        'ICT equipment','Graduates by year','By workplace compatibility',
        'Top universities by graduate count','Laboratories by university'
    ];
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_message_translation'
    ) INTO translations_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_message'
    ) INTO messages_exists;

    IF translations_exists AND messages_exists THEN
        DELETE FROM system_message_translation
        WHERE message_id IN (SELECT id FROM system_message WHERE message_key = ANY(_keys));
        GET DIAGNOSTICS _deleted_translations = ROW_COUNT;
        RAISE NOTICE 'S010 Rollback: Deleted % translation rows', _deleted_translations;
    ELSE
        RAISE NOTICE 'S010 Rollback: Tables do not exist, skipping translations';
    END IF;

    IF messages_exists THEN
        DELETE FROM system_message WHERE message_key = ANY(_keys);
        GET DIAGNOSTICS _deleted_messages = ROW_COUNT;
        RAISE NOTICE 'S010 Rollback: Deleted % message rows', _deleted_messages;
    END IF;
END $$;
