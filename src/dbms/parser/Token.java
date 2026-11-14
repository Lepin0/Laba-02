package dbms.parser;

import dbms.model.DataType;

public final class Token {
    private final TokenType type;
    private final String text;
    private final int position;

    public Token(TokenType type, String text, int position) {
        this.type = type;
        this.text = text;
        this.position = position;
    }

    public TokenType getType() { return type; }
    public String getText() { return text; }
    public int getPosition() { return position; }

    public boolean isSymbol(String symbol) {
        return type == TokenType.SYMBOL && text.equals(symbol);
    }

    public boolean isKeyword(String word) {
        return type == TokenType.IDENT && text.equalsIgnoreCase(word);
    }

    public Object toValue(DataType target) {
        switch (type) {
            case IDENT:
                if (isKeyword("NULL")) return null;
                if (target == DataType.BOOLEAN && (isKeyword("TRUE") || isKeyword("FALSE"))) {
                    return target.parse(text);
                }
                throw new IllegalArgumentException("Ожидалось значение, получено слово '" + text + "'");
            case NUMBER:
            case STRING:
                return target.parse(text);
            default:
                throw new IllegalArgumentException("Ожидалось значение, получено " + this);
        }
    }

    @Override
    public String toString() {
        return type == TokenType.EOF ? "конец запроса" : "'" + text + "'";
    }
}
