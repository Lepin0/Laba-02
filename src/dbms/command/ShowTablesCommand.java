package dbms.command;

import dbms.model.Table;
import dbms.storage.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ShowTablesCommand implements Command {
    @Override
    public Result execute(Database db) {
        List<List<Object>> rows = new ArrayList<>();
        for (String name : db.tableNames()) {
            Table t = db.getTable(name);
            rows.add(Arrays.<Object>asList(t.getName(), t.getColumns().size(), t.getRows().size()));
        }
        return Result.ofTable(Arrays.asList("table", "columns", "rows"), rows, "Таблиц: " + rows.size());
    }
}
