package dbms.command;

import dbms.model.Table;
import dbms.storage.Database;

/** CREATE INDEX: создаёт индекс по столбцу, чтобы поиск работал быстрее. */
public final class CreateIndexCommand implements Command {
    private final String indexName;
    private final String tableName;
    private final String columnName;

    /** Создаёт команду: имя индекса, таблица и столбец. */
    public CreateIndexCommand(String indexName, String tableName, String columnName) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    /** Создаёт индекс. */
    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);
        table.createIndex(indexName, columnName);
        db.save(table);
        return Result.ofMessage("Индекс " + indexName + " создан");
    }
}
