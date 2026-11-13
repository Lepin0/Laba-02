package dbms.index;

import dbms.model.Row;
import java.io.Serializable;
import java.util.*;

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
    public int getColumnIndex() { return columnIndex; }

    public void add(Row row) {
        map.computeIfAbsent(row.get(columnIndex), k -> new ArrayList<>()).add(row);
    }

    public void rebuild(List<Row> rows) {
        map.clear();
        for (Row r : rows) add(r);
    }

    public List<Row> find(Object key) {
        List<Row> result = map.get(key);
        return result == null ? Collections.<Row>emptyList() : result;
    }
}