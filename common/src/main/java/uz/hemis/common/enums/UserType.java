package uz.hemis.common.enums;

/**
 * User Type Enum — maps to DB column users.user_type
 *
 * <p>DB CHECK constraint: user_type IN ('SYSTEM', 'UNIVERSITY', 'MINISTRY', 'ORGANIZATION')</p>
 *
 * @since 2.0.0
 */
public enum UserType {
    SYSTEM,
    UNIVERSITY,
    MINISTRY,
    ORGANIZATION
}
