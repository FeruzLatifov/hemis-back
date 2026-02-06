-- Rollback: S007_seed_i18n_audit_logs.sql
-- Audit loglar tarjimalarini o'chirish

DELETE FROM system_message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE message_key IN (
        'Audit Logs', 'Activity Log', 'Request Log', 'Error Log', 'Login Log',
        'Entity Type', 'Entity Name', 'IP Address', 'Status Code', 'Response Time',
        'Error Type', 'Error Message', 'Event Type', 'Failure Reason',
        'User Agent', 'Session ID', 'Request ID',
        'Old Value', 'New Value', 'Changed Fields', 'Stack Trace',
        'Request Body', 'Response Body', 'Query String',
        'View Details', 'Export Logs',
        'Filter by user', 'Filter by action', 'Filter by date', 'Date range',
        'Top Users', 'Top Endpoints', 'Error Rate',
        'Total Activities', 'Total Requests', 'Total Errors', 'Total Logins',
        'Login Success', 'Login Failed', 'Token Refresh', 'Session Expired'
    )
);

DELETE FROM system_messages
WHERE message_key IN (
    'Audit Logs', 'Activity Log', 'Request Log', 'Error Log', 'Login Log',
    'Entity Type', 'Entity Name', 'IP Address', 'Status Code', 'Response Time',
    'Error Type', 'Error Message', 'Event Type', 'Failure Reason',
    'User Agent', 'Session ID', 'Request ID',
    'Old Value', 'New Value', 'Changed Fields', 'Stack Trace',
    'Request Body', 'Response Body', 'Query String',
    'View Details', 'Export Logs',
    'Filter by user', 'Filter by action', 'Filter by date', 'Date range',
    'Top Users', 'Top Endpoints', 'Error Rate',
    'Total Activities', 'Total Requests', 'Total Errors', 'Total Logins',
    'Login Success', 'Login Failed', 'Token Refresh', 'Session Expired'
);
