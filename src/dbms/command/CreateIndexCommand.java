package dbms.command;

import dbms.model.Table;
import dbms.storage.Database;

public final class CreateIndexCommand implements Command {
    private final String indexName;
    private final String tableName;
    private final String columnName;

    public CreateIndexCommand(String indexName, String tableName, String columnName) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);
        table.createIndex(indexName, columnName);
        db.save(table);
        return Result.ofMessage("Индекс " + indexName + " создан");
    }
}
