package dbms.command;

import dbms.model.Column;
import dbms.model.Row;
import dbms.model.Table;
import dbms.parser.Token;
import dbms.storage.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** INSERT INTO: добавляет одну или несколько строк. Если одна строка не прошла проверку, добавленные ранее отменяются. */
public final class InsertCommand implements Command {
    private final String tableName;
    private final List<String> columns;
    private final List<List<Token>> tuples;

    /** Создаёт команду: таблица, столбцы (или null, если все) и значения. */
    public InsertCommand(String tableName, List<String> columns, List<List<Token>> tuples) {
        this.tableName = tableName;
        this.columns = columns;
        this.tuples = tuples;
    }

    /** Добавляет строки в таблицу. */
    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);
        List<Column> cols = table.getColumns();

        int[] map;
        if (columns == null) {
            map = new int[cols.size()];
            for (int i = 0; i < map.length; i++) map[i] = i;
        } else {
            map = new int[columns.size()];
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < map.length; i++) {
                map[i] = table.columnIndex(columns.get(i));
                if (!seen.add(map[i])) {
                    throw new IllegalArgumentException("Столбец '" + columns.get(i) + "' указан дважды");
                }
            }
        }

        List<Row> rows = new ArrayList<>();
        for (List<Token> tuple : tuples) {
            if (tuple.size() != map.length) {
                throw new IllegalArgumentException("Ожидалось значений: " + map.length + ", получено: " + tuple.size());
            }
            List<Object> values = new ArrayList<>(Collections.nCopies(cols.size(), (Object) null));
            for (int j = 0; j < map.length; j++) {
                values.set(map[j], tuple.get(j).toValue(cols.get(map[j]).getType()));
            }
            rows.add(new Row(values));
        }

        List<Row> inserted = new ArrayList<>();
        try {
            for (Row r : rows) {
                db.insertRow(table, r);
                inserted.add(r);
            }
        } catch (RuntimeException e) {
            if (!inserted.isEmpty()) {
                Set<Row> undo = Collections.newSetFromMap(new IdentityHashMap<Row, Boolean>());
                undo.addAll(inserted);
                table.getRows().removeIf(undo::contains);
                table.rebuildIndexes();
                db.save(table);
            }
            throw e;
        }
        return Result.ofMessage("Вставлено записей: " + inserted.size());
    }
}
