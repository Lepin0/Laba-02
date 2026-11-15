
package dbms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.*;
import dbms.index.Index;

/** Создаёт строку из копии переданного списка. */
public class Table implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final List<Column> columns;
    private final List<Row> rows = new ArrayList<>();
    private final Map<String, Index> indexes = new LinkedHashMap<>();

    /** Создаёт пустую таблицу. */
    public Table(String name, List<Column> columns) {
        long pkCount = columns.stream().filter(Column::isPrimaryKey).count();
        if (pkCount != 1) {
            throw new IllegalArgumentException("В таблице должен быть ровно один первичный ключ");
        }
        this.name = name;
        this.columns = new ArrayList<>(columns);
    }

    public String getName() { return name; }
    public List<Column> getColumns() { return columns; }
    public List<Row> getRows() { return rows; }

    /** Номер столбца по имени (без учёта регистра). */
    public int columnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equalsIgnoreCase(columnName)) return i;
        }
        throw new IllegalArgumentException("Нет столбца '" + columnName + "' в таблице " + name);
    }
    /** Номер столбца первичного ключа. */
    public int primaryKeyIndex() {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isPrimaryKey()) return i;
        }
        throw new IllegalStateException("Нет первичного ключа");
    }

    /** Создаёт индекс по столбцу и заполняет его текущими строками. */
    public void createIndex(String indexName, String columnName) {
        if (indexes.containsKey(indexName.toLowerCase())) {
            throw new IllegalArgumentException("Индекс '" + indexName + "' уже существует");
        }
        Index idx = new Index(indexName, columnIndex(columnName));
        idx.rebuild(rows);
        indexes.put(indexName.toLowerCase(), idx);
    }

    /** Индекс по столбцу с данным номером или {@code null}, если его нет. */
    public Index indexForColumn(int columnIdx) {
        for (Index i : indexes.values()) {
            if (i.getColumnIndex() == columnIdx) return i;
        }
        return null;
    }

    /** Перестраивает все индексы; вызывается после UPDATE и DELETE. */
    public void rebuildIndexes() {
        for (Index i : indexes.values()) i.rebuild(rows);
    }
    /** Добавляет строку после проверки числа значений, типов и уникальности ключа. */
        public void insert(Row row) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException("Ожидалось значений: " + columns.size() + ", получено: " + row.size());
            }
            for (int i = 0; i < columns.size(); i++) {
                Column c = columns.get(i);
                if (!c.getType().accepts(row.get(i))) {
                    throw new IllegalArgumentException("Столбец " + c.getName() + " имеет тип " + c.getType());
                }
            }
            int pk = primaryKeyIndex();
            Object key = row.get(pk);
            if (key == null) {
                throw new IllegalArgumentException("Первичный ключ не может быть пустым");
            }
            for (Row existing : rows) {
                if (existing.get(pk).equals(key)) {
                    throw new IllegalArgumentException("Дубликат первичного ключа: " + key);
                }
            }
            rows.add(row);
            for (Index i : indexes.values()) i.add(row);
        }
    }
