package dbms.command;

import java.util.Collections;
import java.util.List;

/** Результат команды: сообщение и, если нужно, таблица. */
public final class Result {
    private final String message;
    private final List<String> headers;
    private final List<List<Object>> rows;

    private Result(String message, List<String> headers, List<List<Object>> rows) {
        this.message = message;
        this.headers = headers;
        this.rows = rows;
    }

    /** Создаёт результат только с сообщением. */
    public static Result ofMessage(String message) {
        return new Result(message, null, null);
    }

    /** Создаёт результат с таблицей и сообщением. */
    public static Result ofTable(List<String> headers, List<List<Object>> rows, String message) {
        return new Result(message, headers, rows);
    }

    /** Есть ли в результате таблица. */
    public boolean hasTable() { return headers != null; }

    /** Возвращает сообщение. */
    public String getMessage() { return message; }

    /** Возвращает заголовки столбцов. */
    public List<String> getHeaders() { return headers == null ? Collections.<String>emptyList() : headers; }

    /** Возвращает строки таблицы. */
    public List<List<Object>> getRows() { return rows == null ? Collections.<List<Object>>emptyList() : rows; }
}
