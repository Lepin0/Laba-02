package dbms.command;

import java.util.Collections;
import java.util.List;

public final class Result {
    private final String message;
    private final List<String> headers;
    private final List<List<Object>> rows;

    private Result(String message, List<String> headers, List<List<Object>> rows) {
        this.message = message;
        this.headers = headers;
        this.rows = rows;
    }

    public static Result ofMessage(String message) {
        return new Result(message, null, null);
    }

    public static Result ofTable(List<String> headers, List<List<Object>> rows, String message) {
        return new Result(message, headers, rows);
    }

    public boolean hasTable() { return headers != null; }
    public String getMessage() { return message; }
    public List<String> getHeaders() { return headers == null ? Collections.<String>emptyList() : headers; }
    public List<List<Object>> getRows() { return rows == null ? Collections.<List<Object>>emptyList() : rows; }
}
