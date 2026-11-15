package dbms.command;

import dbms.model.Column;
import dbms.model.Row;
import dbms.model.Table;
import dbms.storage.Database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Проверки связей между таблицами (внешние ключи). */
final class Integrity {
    private Integrity() { }

    /** Собирает значения, на которые ссылаются другие таблицы. Ключ - номер столбца. */
    static Map<Integer, Set<Object>> referencedValues(Database db, Table table) {
        Map<Integer, Set<Object>> result = new HashMap<>();
        for (String name : db.tableNames()) {
            Table child = db.getTable(name);
            for (int ci = 0; ci < child.getColumns().size(); ci++) {
                Column c = child.getColumns().get(ci);
                if (c.getRefTable() == null || !c.getRefTable().equalsIgnoreCase(table.getName())) continue;
                int parentIdx = table.columnIndex(c.getRefColumn());
                Set<Object> set = result.get(parentIdx);
                if (set == null) {
                    set = new HashSet<>();
                    result.put(parentIdx, set);
                }
                for (Row r : child.getRows()) {
                    Object v = r.get(ci);
                    if (v != null) set.add(v);
                }
            }
        }
        return result;
    }

    /** Проверяет, что в главной таблице есть запись, на которую мы ссылаемся. */
    static void requireParentExists(Database db, Column column, Object value) {
        Table parent = db.getTable(column.getRefTable());
        int pi = parent.columnIndex(column.getRefColumn());
        for (Row r : parent.getRows()) {
            if (value.equals(r.get(pi))) return;
        }
        throw new IllegalArgumentException("Нет записи " + column.getRefTable() + "."
                + column.getRefColumn() + " = " + value + " (столбец " + column.getName() + ")");
    }
}
