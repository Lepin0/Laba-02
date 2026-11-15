package dbms.command;

import dbms.model.Column;
import dbms.model.Row;
import dbms.model.Table;
import dbms.parser.Condition;
import dbms.parser.Token;
import dbms.storage.Database;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** UPDATE: меняет значения в подходящих строках. Сначала всё проверяет, потом меняет. */
public final class UpdateCommand implements Command {
    private final String tableName;
    private final List<String> columns;
    private final List<Token> values;
    private final Condition where;

    /** Создаёт команду: таблица, столбцы, новые значения и условие (или null). */
    public UpdateCommand(String tableName, List<String> columns, List<Token> values, Condition where) {
        this.tableName = tableName;
        this.columns = columns;
        this.values = values;
        this.where = where;
    }

    /** Обновляет строки. */
    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);

        int k = columns.size();
        int[] idx = new int[k];
        Object[] vals = new Object[k];
        for (int i = 0; i < k; i++) {
            idx[i] = table.columnIndex(columns.get(i));
            vals[i] = values.get(i).toValue(table.getColumns().get(idx[i]).getType());
        }

        List<Row> targets = RowFinder.find(table, where).rows();
        if (targets.isEmpty()) return Result.ofMessage("Обновлено записей: 0");

        int pk = table.primaryKeyIndex();
        Map<Integer, Set<Object>> referenced = Integrity.referencedValues(db, table);

        for (int i = 0; i < k; i++) {
            Column col = table.getColumns().get(idx[i]);

            if (idx[i] == pk) {
                if (vals[i] == null) {
                    throw new IllegalArgumentException("Первичный ключ не может быть пустым");
                }
                if (targets.size() > 1) {
                    throw new IllegalArgumentException("Нельзя присвоить одно значение первичного ключа нескольким записям");
                }
                Row only = targets.get(0);
                for (Row r : table.getRows()) {
                    if (r != only && vals[i].equals(r.get(pk))) {
                        throw new IllegalArgumentException("Дубликат первичного ключа: " + vals[i]);
                    }
                }
            }

            if (col.getRefTable() != null && vals[i] != null) {
                Integrity.requireParentExists(db, col, vals[i]);
            }

            Set<Object> used = referenced.get(idx[i]);
            if (used != null) {
                for (Row r : targets) {
                    Object old = r.get(idx[i]);
                    if (old != null && !old.equals(vals[i]) && used.contains(old)) {
                        throw new IllegalArgumentException("Нельзя изменить " + col.getName() + " = " + old
                                + ": на запись ссылаются из другой таблицы");
                    }
                }
            }
        }

        for (Row r : targets) {
            for (int i = 0; i < k; i++) r.set(idx[i], vals[i]);
        }
        table.rebuildIndexes();
        db.save(table);
        return Result.ofMessage("Обновлено записей: " + targets.size());
    }
}
