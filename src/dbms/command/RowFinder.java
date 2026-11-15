package dbms.command;

import dbms.index.Index;
import dbms.model.Row;
import dbms.model.Table;
import dbms.parser.Condition;

import java.util.ArrayList;
import java.util.List;

/** Ищет строки по условию. Если на столбце есть индекс и условие =, ищет через индекс, иначе перебирает всё. */
final class RowFinder {
    private final List<Row> rows;
    private final Index usedIndex;

    private RowFinder(List<Row> rows, Index usedIndex) {
        this.rows = rows;
        this.usedIndex = usedIndex;
    }

    /** Возвращает найденные строки. */
    List<Row> rows() { return rows; }

    /** Возвращает использованный индекс или null. */
    Index usedIndex() { return usedIndex; }

    /** Ищет строки. Если условия нет, возвращает все строки. */
    static RowFinder find(Table table, Condition where) {
        if (where == null) {
            return new RowFinder(new ArrayList<>(table.getRows()), null);
        }
        where.bind(table);
        if (where.isIndexLookup()) {
            Index idx = table.indexForColumn(where.getColumnIndex());
            if (idx != null) {
                return new RowFinder(new ArrayList<>(idx.find(where.getValue())), idx);
            }
        }
        List<Row> result = new ArrayList<>();
        for (Row r : table.getRows()) {
            if (where.matches(r)) result.add(r);
        }
        return new RowFinder(result, null);
    }
}
