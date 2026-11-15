package dbms;

import dbms.model.*;

import java.util.Arrays;
import java.util.Random;

/** Замер скорости поиска без индекса и с индексом. */
public class Benchmark {
    private static final int SCAN_QUERIES = 200;
    private static final int INDEX_QUERIES = 20_000;

    public static void main(String[] args) {
        int[] sizes = {10_000, 100_000, 1_000_000};
        System.out.printf("%-10s %-16s %-18s %-18s %-10s%n",
                "Строк", "Индекс (build)", "Без индекса (мкс)", "С индексом (мкс)", "Ускорение");
        for (int n : sizes) run(n);
    }

    private static void run(int n) {
        Table table = new Table("cars", Arrays.asList(
                new Column("id", DataType.INT, true),
                new Column("brand", DataType.STRING, false),
                new Column("price", DataType.DOUBLE, false)));

        for (int i = 0; i < n; i++) {
            table.getRows().add(new Row(Arrays.<Object>asList(i, "brand" + (i % 10_000), i * 1.5)));
        }
        int brandIdx = table.columnIndex("brand");

        long t0 = System.nanoTime();
        table.createIndex("idx_cars_brand", "brand");
        double buildMs = (System.nanoTime() - t0) / 1e6;

        Random rnd = new Random(42);
        String[] keys = new String[INDEX_QUERIES];
        for (int i = 0; i < keys.length; i++) keys[i] = "brand" + rnd.nextInt(10_000);

        long sink = 0;

        for (int i = 0; i < 20; i++) sink += scan(table, brandIdx, keys[i]);
        for (int i = 0; i < 2000; i++) sink += table.indexForColumn(brandIdx).find(keys[i]).size();

        t0 = System.nanoTime();
        for (int i = 0; i < SCAN_QUERIES; i++) sink += scan(table, brandIdx, keys[i]);
        double scanUs = (System.nanoTime() - t0) / 1e3 / SCAN_QUERIES;

        t0 = System.nanoTime();
        for (int i = 0; i < INDEX_QUERIES; i++) sink += table.indexForColumn(brandIdx).find(keys[i]).size();
        double indexUs = (System.nanoTime() - t0) / 1e3 / INDEX_QUERIES;

        System.out.printf("%-10d %-16.1f %-18.2f %-18.3f %-10.0f (check=%d)%n",
                n, buildMs, scanUs, indexUs, scanUs / indexUs, sink);
    }

    private static int scan(Table table, int col, String key) {
        int count = 0;
        for (Row r : table.getRows()) {
            if (key.equals(r.get(col))) count++;
        }
        return count;
    }
}