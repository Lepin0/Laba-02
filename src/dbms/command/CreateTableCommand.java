package dbms.command;

import dbms.model.Column;
import dbms.model.Table;
import dbms.storage.Database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CreateTableCommand implements Command {
    private final String tableName;
    private final List<Column> columns;

    public CreateTableCommand(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public Result execute(Database db) {
        Set<String> names = new HashSet<>();
        for (Column c : columns) {
            if (!names.add(c.getName().toLowerCase())) {
                throw new IllegalArgumentException("Столбец '" + c.getName() + "' объявлен дважды");
            }
            if (c.getRefTable() != null) {
                Table parent = db.getTable(c.getRefTable());
                int pi = parent.columnIndex(c.getRefColumn());
                if (pi != parent.primaryKeyIndex()) {
                    throw new IllegalArgumentException("Внешний ключ должен ссылаться на первичный ключ таблицы "
                            + parent.getName());
                }
                if (parent.getColumns().get(pi).getType() != c.getType()) {
                    throw new IllegalArgumentException("Тип столбца " + c.getName() + " не совпадает с типом "
                            + parent.getName() + "." + c.getRefColumn());
                }
            }
        }
        db.addTable(new Table(tableName, columns));
        return Result.ofMessage("Таблица " + tableName + " создана");
    }
}
