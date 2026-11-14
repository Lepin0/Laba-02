package dbms.command;

import dbms.index.Index;
import dbms.model.Row;
import dbms.model.Table;
import dbms.parser.Condition;
import dbms.storage.Database;

import java.util.ArrayList;
import java.util.List;

public final class SelectCommand implements Command {
    private final String tableName;
    private final List<String> columns;
    private final Condition where;

    public SelectCommand(String tableName, List<String> columns, Condition where) {
        this.tableName = tableName;
        this.columns = columns;
        this.where = where;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);

        List<String> headers = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        if (columns == null) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                idx.add(i);
                headers.add(table.getColumns().get(i).getName());
            }
        } else {
            for (String name : columns) {
                int i = table.columnIndex(name);
                idx.add(i);
                headers.add(table.getColumns().get(i).getName());
            }
        }

        RowFinder found = RowFinder.find(table, where);

        List<List<Object>> result = new ArrayList<>();
        for (Row r : found.rows()) {
            List<Object> out = new ArrayList<>(idx.size());
            for (int i : idx) out.add(r.get(i));
            result.add(out);
        }

        Index used = found.usedIndex();
        String message = "Найдено записей: " + result.size()
                + (used != null ? " (использован индекс " + used.getName() + ")" : "");
        return Result.ofTable(headers, result, message);
    }
}
