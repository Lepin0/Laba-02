package dbms.parser;

import dbms.model.DataType;

/** Один кусочек запроса: слово, число, строка или знак. */
public final class Token {
    private final TokenType type;
    private final String text;
    private final int position;

    /** Создаёт токен: вид, текст и место в запросе. */
    public Token(TokenType type, String text, int position) {
        this.type = type;
        this.text = text;
        this.position = position;
    }

    /** Возвращает вид токена. */
    public TokenType getType() { return type; }

    /** Возвращает текст токена. */
    public String getText() { return text; }

    /** Возвращает место токена в запросе. */
    public int getPosition() { return position; }

    /** Проверяет, что токен - нужный знак. */
    public boolean isSymbol(String symbol) {
        return type == TokenType.SYMBOL && text.equals(symbol);
    }

    /** Проверяет, что токен - нужное слово (регистр не важен). */
    public boolean isKeyword(String word) {
        return type == TokenType.IDENT && text.equalsIgnoreCase(word);
    }

    /** Превращает токен в значение нужного типа. Слово NULL даёт null. */
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

    /** Возвращает текст токена для сообщений об ошибках. */
    @Override
    public String toString() {
        return type == TokenType.EOF ? "конец запроса" : "'" + text + "'";
    }
}
