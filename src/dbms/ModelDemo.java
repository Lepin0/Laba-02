package dbms;

import dbms.model.*;
import java.util.Arrays;

public class ModelDemo {
    public static void main(String[] args) {
        Table cars = new Table("cars", Arrays.asList(
                new Column("id", DataType.INT, true),
                new Column("brand", DataType.STRING, false),
                new Column("price", DataType.DOUBLE, false),
                new Column("available", DataType.BOOLEAN, false)));

        cars.insert(new Row(Arrays.<Object>asList(1, "Toyota", 25000.0, true)));
        cars.insert(new Row(Arrays.<Object>asList(2, "BMW", null, false)));

        try {
            cars.insert(new Row(Arrays.<Object>asList(1, "Audi", 1.0, true)));
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
        try {
            cars.insert(new Row(Arrays.<Object>asList(3, "Audi", "дорого", true)));
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }

        for (Row r : cars.getRows()) System.out.println(r);
        System.out.println(DataType.INT.parse("42") + " " + DataType.DATE.parse("2026-11-15"));

        dbms.storage.Database db = new dbms.storage.Database("data");
        if (!db.hasTable("cars")) {
            db.addTable(cars);
            System.out.println("Таблица создана и сохранена");
        }
        System.out.println("Загружено: " + db.getTable("CARS").getRows());

        dbms.storage.Database db2 = new dbms.storage.Database("data");
        Table t = db2.getTable("cars");
        if (t.indexForColumn(t.columnIndex("brand")) == null) {
            t.createIndex("idx_cars_brand", "brand");
            db2.save(t);
            System.out.println("Индекс создан");
        } else {
            System.out.println("Индекс уже загружен с диска");
        }
        System.out.println(t.indexForColumn(t.columnIndex("brand")).find("BMW"));
    }
}
