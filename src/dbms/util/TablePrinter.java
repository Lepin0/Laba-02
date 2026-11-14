package dbms.util;

import java.util.ArrayList;
import java.util.List;

public final class TablePrinter {
    private TablePrinter() { }

    public static String format(List<String> headers, List<List<Object>> rows) {
        int cols = headers.size();
        List<String[]> cells = new ArrayList<>();
        int[] width = new int[cols];
        for (int i = 0; i < cols; i++) width[i] = headers.get(i).length();
        for (List<Object> row : rows) {
            String[] line = new String[cols];
            for (int i = 0; i < cols; i++) {
                Object v = row.get(i);
                line[i] = v == null ? "NULL" : v.toString();
                width[i] = Math.max(width[i], line[i].length());
            }
            cells.add(line);
        }

        StringBuilder sb = new StringBuilder();
        String separator = separator(width);
        sb.append(separator);
        sb.append(line(headers.toArray(new String[0]), width));
        sb.append(separator);
        for (String[] line : cells) sb.append(line(line, width));
        sb.append(separator);
        return sb.toString();
    }

    private static String separator(int[] width) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : width) {
            for (int i = 0; i < w + 2; i++) sb.append('-');
            sb.append('+');
        }
        return sb.append('\n').toString();
    }

    private static String line(String[] values, int[] width) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < width.length; i++) {
            sb.append(' ').append(values[i]);
            for (int p = values[i].length(); p < width[i]; p++) sb.append(' ');
            sb.append(" |");
        }
        return sb.append('\n').toString();
    }
}
