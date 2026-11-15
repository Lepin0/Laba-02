package dbms.model;

import java.io.Serializable;

public class Column implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Описание столбца: имя, тип, признак первичного ключа и необязательная ссылка на другую таблицу. */
    private final String name;
    private final DataType type;
    private final boolean primaryKey;
    private final String refTable;
    private final String refColumn;

    /** Столбец без внешнего ключа. */
    public Column(String name, DataType type, boolean primaryKey) {
        this(name, type, primaryKey, null, null);
    }
    /** Столбец с внешним ключом на {@code refTable(refColumn)}. */
    public Column(String name, DataType type, boolean primaryKey, String refTable, String refColumn) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.refTable = refTable;
        this.refColumn = refColumn;
    }


    public String getName() { return name; }
    public DataType getType() { return type; }
    public boolean isPrimaryKey() { return primaryKey; }
    public String getRefTable() { return refTable; }
    public String getRefColumn() { return refColumn; }
}