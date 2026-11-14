package dbms.command;

import dbms.storage.Database;

public interface Command {

    Result execute(Database db);
}
