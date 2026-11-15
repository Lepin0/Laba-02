package dbms;

import dbms.command.Command;
import dbms.command.Result;
import dbms.parser.Parser;
import dbms.storage.Database;
import dbms.util.TablePrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Консольная программа. Команда заканчивается знаком ; и может занимать несколько строк. */
public class Main {

    /** Запускает программу. Можно указать папку с данными, по умолчанию data. */
    public static void main(String[] args) throws IOException {
        String directory = args.length > 0 ? args[0] : "data";
        Database db = new Database(directory);

        System.out.println("Мини-СУБД. Команда заканчивается ';'. help - справка, exit - выход.");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder buffer = new StringBuilder();

        while (true) {
            System.out.print(buffer.length() == 0 ? "dbms> " : "  ... ");
            System.out.flush();
            String line = in.readLine();
            if (line == null) break;
            String trimmed = line.trim();

            if (buffer.length() == 0) {
                if (trimmed.isEmpty()) continue;
                String word = trimmed.toLowerCase().replace(";", "").trim();
                if (word.equals("exit") || word.equals("quit")) break;
                if (word.equals("help")) {
                    printHelp();
                    continue;
                }
            }

            buffer.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                run(db, buffer.toString());
                buffer.setLength(0);
            }
        }
        System.out.println("До свидания!");
    }

    /** Разбирает и выполняет одну команду, печатает результат или ошибку. */
    private static void run(Database db, String sql) {
        long start = System.nanoTime();
        try {
            Command command = Parser.parse(sql);
            Result result = command.execute(db);
            double ms = (System.nanoTime() - start) / 1e6;
            if (result.hasTable()) {
                System.out.print(TablePrinter.format(result.getHeaders(), result.getRows()));
            }
            System.out.println(result.getMessage() + String.format(" [%.2f мс]", ms));
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Внутренняя ошибка: " + e);
        }
    }

    private static void printHelp() {
        System.out.println("Команды:");
        System.out.println("  CREATE TABLE t (id INT PRIMARY KEY, name STRING, owner INT REFERENCES users(id));");
        System.out.println("  DROP TABLE t;");
        System.out.println("  CREATE INDEX idx ON t (name);");
        System.out.println("  INSERT INTO t VALUES (1, 'a'), (2, 'b');");
        System.out.println("  INSERT INTO t (id, name) VALUES (3, 'c');");
        System.out.println("  SELECT * FROM t;   SELECT name FROM t WHERE id > 1;");
        System.out.println("  UPDATE t SET name = 'x' WHERE id = 2;");
        System.out.println("  DELETE FROM t WHERE id = 3;");
        System.out.println("  SHOW TABLES;");
        System.out.println("Типы: INT, DOUBLE, STRING, DATE ('2026-11-15'), BOOLEAN (TRUE/FALSE).");
        System.out.println("Операторы WHERE: = != <> < > <= >=  (одно условие).");
    }
}
