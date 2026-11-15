package dbms.storage;

import dbms.model.Table;

import java.io.*;
import java.nio.file.*;

/** Хранение таблиц в файлах {@code <имя>.tbl} (паттерн Repository). */
public class TableRepository {
    private final Path dir;

    /** Открывает каталог, создавая его при необходимости. */
    public TableRepository(String directory) {
        this.dir = Paths.get(directory);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось создать каталог " + dir, e);
        }
    }

    private Path fileFor(String tableName) {
        return dir.resolve(tableName + ".tbl");
    }
    /** Сохраняет таблицу: запись во временный файл, затем атомарная замена основного. */
    public void save(Table table) {
        Path tmp = dir.resolve(table.getName() + ".tbl.tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeObject(table);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка записи таблицы " + table.getName(), e);
        }
        try {
            Files.move(tmp, fileFor(table.getName()),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка замены файла таблицы " + table.getName(), e);
        }
    }

    /** Загружает таблицу из файла. */
    public Table load(Path file) {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {
            return (Table) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Ошибка чтения " + file, e);
        }
    }
    /** Загружает все таблицы из каталога. */
    public java.util.List<Table> loadAll() {
        java.util.List<Table> result = new java.util.ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*.tbl")) {
            for (Path f : files) result.add(load(f));
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка чтения каталога " + dir, e);
        }
        return result;
    }

    /** Удаляет файл таблицы (DROP TABLE). */
    public void delete(String tableName) {
        try {
            Files.deleteIfExists(fileFor(tableName));
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка удаления таблицы " + tableName, e);
        }
    }
}