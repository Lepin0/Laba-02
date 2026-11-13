package dbms.storage;

import dbms.model.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private final Map<String, Table> tables = new LinkedHashMap<>();
    private final TableRepository repository;

    public Database(String directory) {
        this.repository = new TableRepository(directory);
        for (Table t : repository.loadAll()) {
            tables.put(key(t.getName()), t);
        }
    }

    private static String key(String name) {
        return name.toLowerCase();
    }

    public boolean hasTable(String name) {
        return tables.containsKey(key(name));
    }

    public Table getTable(String name) {
        Table t = tables.get(key(name));
        if (t == null) {
            throw new IllegalArgumentException("Таблица '" + name + "' не существует");
        }
        return t;
    }

    public List<String> tableNames() {
        List<String> names = new ArrayList<>();
        for (Table t : tables.values()) names.add(t.getName());
        return names;
    }

    public void addTable(Table table) {
        if (hasTable(table.getName())) {
            throw new IllegalArgumentException("Таблица '" + table.getName() + "' уже существует");
        }
        tables.put(key(table.getName()), table);
        repository.save(table);
    }

    public void dropTable(String name) {
        Table t = getTable(name); // бросит ошибку, если таблицы нет
        tables.remove(key(name));
        repository.delete(t.getName());
    }

    public void save(Table table) {
        repository.save(table);
    }
}