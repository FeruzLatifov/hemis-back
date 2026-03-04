package uz.hemis.domain.entity;

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
 * <p>Database stores lowercase values ("main", "system").</p>
 */
public enum MenuType {
    MAIN("main"),
    SYSTEM("system");

    private final String value;

    MenuType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

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
            return attribute != null ? attribute.getValue() : MAIN.getValue();
        }

        @Override
        public MenuType convertToEntityAttribute(String dbData) {
            return fromValue(dbData);
        }
    }
}
