package dbms.index;

import dbms.model.Row;
import java.io.Serializable;
import java.util.*;

/** Хэш-индекс по одному столбцу: значение -> строки с этим значением. Ускоряет поиск по равенству. */
public class Index implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int columnIndex;
    private final Map<Object, List<Row>> map = new HashMap<>();

    public Index(String name, int columnIndex) {
        this.name = name;
        this.columnIndex = columnIndex;
    }

    public String getName() { return name; }
    /** Номер индексируемого столбца в таблице. */
    public int getColumnIndex() { return columnIndex; }

    /** Добавляет строку в индекс. */
    public void add(Row row) {
        map.computeIfAbsent(row.get(columnIndex), k -> new ArrayList<>()).add(row);
    }

    /** Полностью перестраивает индекс по списку строк. */
    public void rebuild(List<Row> rows) {
        map.clear();
        for (Row r : rows) add(r);
    }
    /** Строки с заданным значением (пустой список, если таких нет). */
    public List<Row> find(Object key) {
        List<Row> result = map.get(key);
        return result == null ? Collections.<Row>emptyList() : result;
    }
}