package dbms.model;

import java.time.format.DateTimeParseException;
import java.time.LocalDate;

/** Поддерживаемые типы данных столбцов. */
public enum DataType {
    INT, DOUBLE, STRING, DATE, BOOLEAN;

    /**Превращает текст в значение этого типа. */
    public Object parse(String text) {
        String t = text.trim();
        try {
            switch (this) {
                case INT:
                    return Integer.parseInt(t);
                case DOUBLE:
                    return Double.parseDouble(t);
                case STRING:
                    return text;
                case DATE:
                    return LocalDate.parse(t);
                case BOOLEAN:
                    if (t.equalsIgnoreCase("true")) return true;
                    if (t.equalsIgnoreCase("false")) return false;
                    throw new IllegalArgumentException("Ожидалось TRUE или FALSE, получено: " + text);
                default:
                    throw new IllegalStateException("Неизвестный тип " + this);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Значение '" + text + "' не подходит для типа " + this);
        }
    }

    /** Проверяет, что Java-тип значения соответствует этому типу. {@code null} допустим. */
    public boolean accepts(Object value) {
        if (value == null) return true;
        switch (this) {
            case INT:     return value instanceof Integer;
            case DOUBLE:  return value instanceof Double;
            case STRING:  return value instanceof String;
            case DATE:    return value instanceof LocalDate;
            case BOOLEAN: return value instanceof Boolean;
            default:      return false;
        }
    }
}