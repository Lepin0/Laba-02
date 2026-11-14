package dbms.parser;

import dbms.command.*;
import dbms.model.Column;
import dbms.model.DataType;

import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    private Parser(List<Token> tokens) { this.tokens = tokens; }

    public static Command parse(String sql) {
        Parser p = new Parser(Tokenizer.tokenize(sql));
        Command command = p.statement();
        p.acceptSymbol(";");
        p.expectEnd();
        return command;
    }

    private Token peek() { return tokens.get(pos); }

    private Token next() {
        Token t = tokens.get(pos);
        if (t.getType() != TokenType.EOF) pos++;
        return t;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException("Синтаксическая ошибка: " + message);
    }

    private boolean acceptKeyword(String word) {
        if (peek().isKeyword(word)) { pos++; return true; }
        return false;
    }

    private void expectKeyword(String word) {
        if (!acceptKeyword(word)) throw error("ожидалось " + word + ", получено " + peek());
    }

    private boolean acceptSymbol(String symbol) {
        if (peek().isSymbol(symbol)) { pos++; return true; }
        return false;
    }

    private void expectSymbol(String symbol) {
        if (!acceptSymbol(symbol)) throw error("ожидалось '" + symbol + "', получено " + peek());
    }

    private String expectIdent(String what) {
        Token t = peek();
        if (t.getType() != TokenType.IDENT) throw error("ожидалось " + what + ", получено " + t);
        pos++;
        return t.getText();
    }

    private void expectEnd() {
        Token t = peek();
        if (t.getType() == TokenType.EOF) return;
        if (t.isKeyword("AND") || t.isKeyword("OR")) {
            throw error("составные условия (AND/OR) не поддерживаются");
        }
        throw error("лишний текст после конца команды: " + t);
    }

    private Token expectValue() {
        Token t = peek();
        boolean ok = t.getType() == TokenType.NUMBER || t.getType() == TokenType.STRING
                || t.isKeyword("NULL") || t.isKeyword("TRUE") || t.isKeyword("FALSE");
        if (!ok) throw error("ожидалось значение, получено " + t);
        pos++;
        return t;
    }

    private List<String> identList() {
        List<String> list = new ArrayList<>();
        do {
            list.add(expectIdent("имя столбца"));
        } while (acceptSymbol(","));
        return list;
    }

    private List<Token> valueList() {
        List<Token> list = new ArrayList<>();
        do {
            list.add(expectValue());
        } while (acceptSymbol(","));
        return list;
    }

    private Condition optionalWhere() {
        if (!acceptKeyword("WHERE")) return null;
        String column = expectIdent("имя столбца");
        Token opToken = next();
        Condition.Op op = opToken.getType() == TokenType.SYMBOL ? Condition.Op.fromSymbol(opToken.getText()) : null;
        if (op == null) throw error("ожидался оператор сравнения (=, !=, <, >, <=, >=), получено " + opToken);
        return new Condition(column, op, expectValue());
    }

    private Command statement() {
        Token t = peek();
        if (t.isKeyword("INSERT")) return insert();
        if (t.isKeyword("SELECT")) return select();
        if (t.isKeyword("UPDATE")) return update();
        if (t.isKeyword("DELETE")) return delete();
        if (t.isKeyword("CREATE")) return create();
        if (t.isKeyword("DROP")) return drop();
        if (t.isKeyword("SHOW")) return show();
        if (t.getType() == TokenType.EOF) throw error("пустой запрос");
        throw error("ожидалась команда INSERT, SELECT, UPDATE, DELETE, CREATE, DROP или SHOW, получено " + t);
    }

    private Command insert() {
        next();
        expectKeyword("INTO");
        String table = expectIdent("имя таблицы");
        List<String> columns = null;
        if (acceptSymbol("(")) {
            columns = identList();
            expectSymbol(")");
        }
        expectKeyword("VALUES");
        List<List<Token>> tuples = new ArrayList<>();
        do {
            expectSymbol("(");
            tuples.add(valueList());
            expectSymbol(")");
        } while (acceptSymbol(","));
        return new InsertCommand(table, columns, tuples);
    }

    private Command select() {
        next();
        List<String> columns = null;
        if (!acceptSymbol("*")) columns = identList();
        expectKeyword("FROM");
        String table = expectIdent("имя таблицы");
        return new SelectCommand(table, columns, optionalWhere());
    }

    private Command update() {
        next();
        String table = expectIdent("имя таблицы");
        expectKeyword("SET");
        List<String> columns = new ArrayList<>();
        List<Token> values = new ArrayList<>();
        do {
            columns.add(expectIdent("имя столбца"));
            expectSymbol("=");
            values.add(expectValue());
        } while (acceptSymbol(","));
        return new UpdateCommand(table, columns, values, optionalWhere());
    }

    private Command delete() {
        next();
        expectKeyword("FROM");
        String table = expectIdent("имя таблицы");
        return new DeleteCommand(table, optionalWhere());
    }

    private Command create() {
        next();
        if (acceptKeyword("TABLE")) return createTable();
        if (acceptKeyword("INDEX")) return createIndex();
        throw error("после CREATE ожидалось TABLE или INDEX, получено " + peek());
    }

    private Command createTable() {
        String name = expectIdent("имя таблицы");
        expectSymbol("(");
        List<Column> columns = new ArrayList<>();
        do {
            columns.add(columnDefinition());
        } while (acceptSymbol(","));
        expectSymbol(")");
        return new CreateTableCommand(name, columns);
    }

    private Column columnDefinition() {
        String name = expectIdent("имя столбца");
        DataType type = dataType();
        boolean primaryKey = false;
        String refTable = null;
        String refColumn = null;
        while (true) {
            if (acceptKeyword("PRIMARY")) {
                expectKeyword("KEY");
                primaryKey = true;
            } else if (acceptKeyword("REFERENCES")) {
                refTable = expectIdent("имя таблицы");
                expectSymbol("(");
                refColumn = expectIdent("имя столбца");
                expectSymbol(")");
            } else {
                break;
            }
        }
        return new Column(name, type, primaryKey, refTable, refColumn);
    }

    private DataType dataType() {
        Token t = next();
        if (t.getType() != TokenType.IDENT) throw error("ожидался тип данных, получено " + t);
        DataType type;
        switch (t.getText().toUpperCase()) {
            case "INT": case "INTEGER":
                type = DataType.INT; break;
            case "DOUBLE": case "FLOAT": case "REAL":
                type = DataType.DOUBLE; break;
            case "STRING": case "VARCHAR": case "TEXT":
                type = DataType.STRING; break;
            case "DATE":
                type = DataType.DATE; break;
            case "BOOLEAN": case "BOOL":
                type = DataType.BOOLEAN; break;
            default:
                throw error("неизвестный тип данных " + t + " (допустимы INT, DOUBLE, STRING, DATE, BOOLEAN)");
        }
        if (acceptSymbol("(")) {
            if (next().getType() != TokenType.NUMBER) throw error("ожидался размер типа");
            expectSymbol(")");
        }
        return type;
    }

    private Command createIndex() {
        String indexName = expectIdent("имя индекса");
        expectKeyword("ON");
        String table = expectIdent("имя таблицы");
        expectSymbol("(");
        String column = expectIdent("имя столбца");
        expectSymbol(")");
        return new CreateIndexCommand(indexName, table, column);
    }

    private Command drop() {
        next();
        expectKeyword("TABLE");
        return new DropTableCommand(expectIdent("имя таблицы"));
    }

    private Command show() {
        next();
        expectKeyword("TABLES");
        return new ShowTablesCommand();
    }
}
