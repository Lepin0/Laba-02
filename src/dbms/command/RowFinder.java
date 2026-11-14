package dbms.command;

import dbms.index.Index;
import dbms.model.Row;
import dbms.model.Table;
import dbms.parser.Condition;

import java.util.ArrayList;
import java.util.List;

final class RowFinder {
    private final List<Row> rows;
    private final Index usedIndex;

    private RowFinder(List<Row> rows, Index usedIndex) {
        this.rows = rows;
        this.usedIndex = usedIndex;
    }

    List<Row> rows() { return rows; }

    Index usedIndex() { return usedIndex; }

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
