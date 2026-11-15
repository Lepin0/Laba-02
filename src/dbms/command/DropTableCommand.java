package dbms.command;

import dbms.model.Column;
import dbms.model.Table;
import dbms.storage.Database;

/** DROP TABLE: удаляет таблицу. Нельзя, если на неё ссылаются другие таблицы. */
public final class DropTableCommand implements Command {
    private final String tableName;

    /** Создаёт команду: имя таблицы. */
    public DropTableCommand(String tableName) {
        this.tableName = tableName;
    }

    /** Удаляет таблицу. */
    @Override
    public Result execute(Database db) {
        Table target = db.getTable(tableName);
        for (String name : db.tableNames()) {
            Table other = db.getTable(name);
            if (other == target) continue;
            for (Column c : other.getColumns()) {
                if (c.getRefTable() != null && c.getRefTable().equalsIgnoreCase(target.getName())) {
                    throw new IllegalArgumentException("Нельзя удалить таблицу " + target.getName()
                            + ": на неё ссылается " + other.getName() + "." + c.getName());
                }
            }
        }
        db.dropTable(tableName);
        return Result.ofMessage("Таблица " + tableName + " удалена");
    }
}
