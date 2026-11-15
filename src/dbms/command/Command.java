package dbms.command;

import dbms.storage.Database;

/** Команда базы данных. Каждый запрос превращается в такой объект. */
public interface Command {

    /** Выполняет команду и возвращает результат. */
    Result execute(Database db);
}
