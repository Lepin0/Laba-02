package dbms.command;

import dbms.model.Row;
import dbms.model.Table;
import dbms.parser.Condition;
import dbms.storage.Database;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DeleteCommand implements Command {
    private final String tableName;
    private final Condition where;

    public DeleteCommand(String tableName, Condition where) {
        this.tableName = tableName;
        this.where = where;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);
        List<Row> targets = RowFinder.find(table, where).rows();
        if (targets.isEmpty()) return Result.ofMessage("Удалено записей: 0");

        Map<Integer, Set<Object>> referenced = Integrity.referencedValues(db, table);
        for (Map.Entry<Integer, Set<Object>> e : referenced.entrySet()) {
            for (Row r : targets) {
                Object v = r.get(e.getKey());
                if (v != null && e.getValue().contains(v)) {
                    throw new IllegalArgumentException("Нельзя удалить запись " + table.getColumns().get(e.getKey()).getName()
                            + " = " + v + ": на неё ссылаются из другой таблицы");
                }
            }
        }

        Set<Row> doomed = Collections.newSetFromMap(new IdentityHashMap<Row, Boolean>());
        doomed.addAll(targets);
        table.getRows().removeIf(doomed::contains);
        table.rebuildIndexes();
        db.save(table);
        return Result.ofMessage("Удалено записей: " + targets.size());
    }
}
