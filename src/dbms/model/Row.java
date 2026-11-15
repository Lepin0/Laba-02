
package dbms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Строка таблицы: значения в порядке столбцов. */
public class Row implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Object> values;

    /** Создаёт строку из копии переданного списка. */
    public Row(List<Object> values) {
        this.values = new ArrayList<>(values);
    }

    public Object get(int index) { return values.get(index); }
    public void set(int index, Object value) { values.set(index, value); }
    public int size() { return values.size(); }
    public List<Object> getValues() { return values; }

    @Override
    public String toString() { return values.toString(); }
}