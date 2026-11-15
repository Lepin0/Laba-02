package dbms;

import dbms.command.Result;
import dbms.parser.Parser;
import dbms.storage.Database;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class AutoTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("dbms-test");
        try {
            runAll(dir);
        } finally {
            deleteRecursively(dir);
        }
        System.out.println("\nПройдено: " + passed + ", провалено: " + failed);
        if (failed > 0) System.exit(1);
    }

    private static void runAll(Path dir) throws IOException {
        Database db = new Database(dir.toString());

        exec(db, "CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE, available BOOLEAN);");
        exec(db, "CREATE TABLE sales (id INT PRIMARY KEY, car_id INT REFERENCES cars(id), sold DATE, buyer STRING);");
        check("CREATE TABLE: две таблицы", db.tableNames().size() == 2);
        expectError("CREATE TABLE: повтор имени", db,
                "CREATE TABLE cars (id INT PRIMARY KEY);");

        exec(db, "INSERT INTO cars VALUES (1, 'Toyota', 25000.0, TRUE);");
        exec(db, "INSERT INTO cars (id, brand) VALUES (2, 'BMW');");
        exec(db, "INSERT INTO cars VALUES (3, 'Audi', 18000.5, FALSE), (4, 'Kia', 15000, TRUE);");
        check("INSERT: 4 строки", rows(db, "SELECT * FROM cars;") == 4);
        expectError("INSERT: дубликат PK", db, "INSERT INTO cars VALUES (1, 'Dup', 1.0, TRUE);");
        expectError("INSERT: неверный тип", db, "INSERT INTO cars VALUES (5, 'Ford', 'abc', TRUE);");
        expectError("INSERT: неверное число значений", db, "INSERT INTO cars VALUES (5, 'Ford');");
        check("INSERT: плохие строки не попали в таблицу", rows(db, "SELECT * FROM cars;") == 4);

        expectError("SELECT: нет таблицы", db, "SELECT * FROM nothing;");
        expectError("Парсер: опечатка в команде", db, "SELEC * FROM cars;");

        exec(db, "INSERT INTO sales VALUES (1, 3, '2026-11-10', 'Ivan');");
        expectError("FK: несуществующая ссылка", db, "INSERT INTO sales VALUES (2, 99, '2026-11-11', 'Petr');");
        check("FK: в sales одна строка", rows(db, "SELECT * FROM sales;") == 1);

        check("SELECT: price > 20000 (только Toyota)",
                rows(db, "SELECT brand, price FROM cars WHERE price > 20000;") == 1);
        check("SELECT: brand = BMW", rows(db, "SELECT * FROM cars WHERE brand = 'BMW';") == 1);
        check("SELECT: available = TRUE (Toyota, Kia, без BMW)",
                rows(db, "SELECT * FROM cars WHERE available = TRUE;") == 2);

        exec(db, "UPDATE cars SET price = 30000 WHERE id = 2;");
        check("UPDATE: цена изменилась", rows(db, "SELECT * FROM cars WHERE price = 30000;") == 1);
        exec(db, "UPDATE cars SET price = 1 WHERE id = 100;");
        check("UPDATE: несуществующий id ничего не меняет", rows(db, "SELECT * FROM cars WHERE price = 1;") == 0);
        expectError("UPDATE: дубликат PK", db, "UPDATE cars SET id = 1 WHERE id = 2;");
        check("UPDATE: после ошибки id=1 остался один", rows(db, "SELECT * FROM cars WHERE id = 1;") == 1);

        exec(db, "DELETE FROM cars WHERE brand = 'Toyota';");
        check("DELETE: осталось 3 строки", rows(db, "SELECT * FROM cars;") == 3);

        exec(db, "CREATE INDEX idx_cars_brand ON cars (brand);");
        check("INDEX: поиск Audi = 1", rows(db, "SELECT * FROM cars WHERE brand = 'Audi';") == 1);
        exec(db, "INSERT INTO cars VALUES (6, 'Audi', 20000.0, TRUE);");
        check("INDEX после INSERT: Audi = 2", rows(db, "SELECT * FROM cars WHERE brand = 'Audi';") == 2);
        exec(db, "UPDATE cars SET brand = 'Skoda' WHERE id = 6;");
        check("INDEX после UPDATE: Audi = 1", rows(db, "SELECT * FROM cars WHERE brand = 'Audi';") == 1);
        check("INDEX после UPDATE: Skoda = 1", rows(db, "SELECT * FROM cars WHERE brand = 'Skoda';") == 1);
        exec(db, "DELETE FROM cars WHERE id = 6;");
        check("INDEX после DELETE: Skoda = 0", rows(db, "SELECT * FROM cars WHERE brand = 'Skoda';") == 0);
        expectError("INDEX: повторное создание", db, "CREATE INDEX idx_cars_brand ON cars (brand);");

        Database db2 = new Database(dir.toString());
        check("ДИСК: таблицы загрузились", db2.tableNames().size() == 2);
        check("ДИСК: строки cars сохранились", rows(db2, "SELECT * FROM cars;") == 3);
        check("ДИСК: строки sales сохранились", rows(db2, "SELECT * FROM sales;") == 1);
        check("ДИСК: поиск по индексу работает после загрузки",
                rows(db2, "SELECT * FROM cars WHERE brand = 'Audi';") == 1);
        expectError("ДИСК: индекс загрузился с диска", db2, "CREATE INDEX idx_cars_brand ON cars (brand);");
        check("ДИСК: нет временных .tmp файлов", countTmpFiles(dir) == 0);

        exec(db2, "DROP TABLE sales;");
        check("DROP: таблица удалена из памяти", !db2.hasTable("sales"));
        Database db3 = new Database(dir.toString());
        check("DROP: таблица не вернулась после перезапуска", !db3.hasTable("sales"));
        expectError("DROP: повторное удаление", db3, "DROP TABLE sales;");
    }


    private static Result exec(Database db, String sql) {
        return Parser.parse(sql).execute(db);
    }

    private static int rows(Database db, String sql) {
        return exec(db, sql).getRows().size();
    }

    private static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "[ OK ] " : "[FAIL] ") + name);
    }

    private static void expectError(String name, Database db, String sql) {
        try {
            exec(db, sql);
            check(name + " (ошибка не возникла!)", false);
        } catch (IllegalArgumentException | IllegalStateException e) {
            check(name, true);
        }
    }

    private static long countTmpFiles(Path dir) throws IOException {
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".tmp")).count();
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }
}