package dbms.parser;

import dbms.model.Column;
import dbms.model.DataType;
import dbms.model.Row;
import dbms.model.Table;

/** Условие из WHERE, например price > 20000. Перед работой нужно вызвать bind. */
public final class Condition {

    /** Знаки сравнения. */
    public enum Op {
        EQ("="), NE("!="), LT("<"), GT(">"), LE("<="), GE(">=");

        private final String symbol;

        Op(String symbol) { this.symbol = symbol; }

        /** Возвращает знак оператора. */
        public String getSymbol() { return symbol; }

        /** Находит оператор по знаку. Если такого нет, возвращает null. */
        public static Op fromSymbol(String s) {
            if (s.equals("<>")) return NE;
            for (Op op : values()) {
                if (op.symbol.equals(s)) return op;
            }
            return null;
        }
    }

    private final String column;
    private final Op op;
    private final Token literal;

    private int columnIndex = -1;
    private Object value;

    /** Создаёт условие: столбец, знак сравнения и значение. */
    public Condition(String column, Op op, Token literal) {
        this.column = column;
        this.op = op;
        this.literal = literal;
    }

    /** Привязывает условие к таблице: ищет столбец и приводит значение к его типу. */
    public Condition bind(Table table) {
        columnIndex = table.columnIndex(column);
        Column c = table.getColumns().get(columnIndex);
        value = literal.toValue(c.getType());
        if (c.getType() == DataType.BOOLEAN && op != Op.EQ && op != Op.NE) {
            throw new IllegalArgumentException("Для логического столбца допустимы только = и !=");
        }
        return this;
    }

    private void requireBound() {
        if (columnIndex < 0) throw new IllegalStateException("Условие не привязано к таблице");
    }

    /** Возвращает знак сравнения. */
    public Op getOp() { return op; }

    /** Возвращает номер столбца в таблице. */
    public int getColumnIndex() {
        requireBound();
        return columnIndex;
    }

    /** Возвращает значение, с которым сравниваем. */
    public Object getValue() {
        requireBound();
        return value;
    }

    /** Можно ли искать по индексу: да, если знак = и значение не NULL. */
    public boolean isIndexLookup() {
        requireBound();
        return op == Op.EQ && value != null;
    }

    /** Проверяет, подходит ли строка под условие. */
    @SuppressWarnings("unchecked")
    public boolean matches(Row row) {
        requireBound();
        Object v = row.get(columnIndex);
        if (v == null || value == null) {
            boolean equal = (v == value);
            if (op == Op.EQ) return equal;
            if (op == Op.NE) return !equal;
            return false;
        }
        int cmp = ((Comparable<Object>) v).compareTo(value);
        switch (op) {
            case EQ: return cmp == 0;
            case NE: return cmp != 0;
            case LT: return cmp < 0;
            case GT: return cmp > 0;
            case LE: return cmp <= 0;
            case GE: return cmp >= 0;
            default: return false;
        }
    }
}
