package uz.hemis.domain.entity.system;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;

/**
 * Menu type classification.
 *
 * <p>Determines how the frontend renders menu items:</p>
 * <ul>
 *   <li>{@link #MAIN} — Regular menu (shown in main section)</li>
 *   <li>{@link #SYSTEM} — System menu (shown below separator)</li>
 * </ul>
 *
 * <p>Wire format (JSON va DB): lowercase "main"/"system" — frontend va
 * V013 CHECK constraint shu shaklni kutadi.</p>
 */
public enum MenuType {
    MAIN("main"),
    SYSTEM("system");

    private final String value;

    MenuType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MenuType fromValue(String value) {
        for (MenuType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return MAIN;
    }

    /**
     * JPA AttributeConverter: enum ↔ lowercase DB value
     */
    @jakarta.persistence.Converter(autoApply = false)
    public static class Converter implements AttributeConverter<MenuType, String> {

        @Override
        public String convertToDatabaseColumn(MenuType attribute) {
            // Return null for null (not a MAIN default): the entity field defaults to MAIN so persist
            // never passes null, and a converter that maps null->'main' would break any future
            // COALESCE(:menuType, m.menuType) optional-filter query (silently forcing menuType='main').
            // Same class of bug that hid NEEDS_REVIEW specialities via ReviewStatus.Converter.
            return attribute != null ? attribute.getValue() : null;
        }

        @Override
        public MenuType convertToEntityAttribute(String dbData) {
            return fromValue(dbData);
        }
    }
}
