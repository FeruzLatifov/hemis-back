package uz.hemis.domain.type;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * Custom Hibernate UserType for UUID fields.
 *
 * Handles PostgreSQL JDBC driver returning UUID as String on SELECT.
 * - READ: String -> UUID conversion
 * - WRITE: UUID -> UUID (JDBC driver handles casting to uuid type)
 *
 * Note: Using deprecated UserType methods without BasicValuedMapping parameter.
 * These methods still work in Hibernate 7 but are deprecated in favor of new overloads.
 */
@SuppressWarnings("removal")
public class StringToUuidType implements UserType<UUID> {

    @Override
    public int getSqlType() {
        return Types.OTHER; // PostgreSQL uuid type
    }

    @Override
    public Class<UUID> returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean equals(UUID x, UUID y) {
        if (x == y) return true;
        if (x == null || y == null) return false;
        return x.equals(y);
    }

    @Override
    public int hashCode(UUID x) {
        return x != null ? x.hashCode() : 0;
    }

    @Override
    public UUID nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        Object value = rs.getObject(position);
        if (value == null) return null;

        // Handle both UUID and String returns from PostgreSQL JDBC
        if (value instanceof UUID) {
            return (UUID) value;
        } else if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return null;
            try {
                return UUID.fromString(str);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UUID value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            // Send UUID object - PostgreSQL will accept it for uuid columns
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public UUID deepCopy(UUID value) {
        return value; // UUID is immutable
    }

    @Override
    public boolean isMutable() {
        return false; // UUID is immutable
    }

    @Override
    public Serializable disassemble(UUID value) {
        return value;
    }

    @Override
    public UUID assemble(Serializable cached, Object owner) {
        return (UUID) cached;
    }
}
