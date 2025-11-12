-- ================================================================
-- V5: Simple Menu Translations (Working with existing permissions)
-- ================================================================

-- Create translation tables
CREATE TABLE IF NOT EXISTS h_system_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    message_key VARCHAR(255) NOT NULL UNIQUE,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS h_system_message_translation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES h_system_message(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(message_id, language)
);

-- Dashboard
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.dashboard', 'Bosh sahifa')
ON CONFLICT (message_key) DO NOTHING;

-- Data Student
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.data.student', 'Talabalar')
ON CONFLICT (message_key) DO NOTHING;

-- Data Employee
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.data.employee', 'Xodimlar')
ON CONFLICT (message_key) DO NOTHING;

-- Reports
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.reports', 'Hisobotlar')
ON CONFLICT (message_key) DO NOTHING;

-- Reports Students
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.reports.students', 'Talabalar hisobotlari')
ON CONFLICT (message_key) DO NOTHING;

-- Reports Employees
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.reports.employees', 'Xodimlar hisobotlari')
ON CONFLICT (message_key) DO NOTHING;

-- Reports Universities
INSERT INTO h_system_message (category, message_key, message) 
VALUES ('menu', 'menu.reports.universities', 'OTM hisobotlari')
ON CONFLICT (message_key) DO NOTHING;
