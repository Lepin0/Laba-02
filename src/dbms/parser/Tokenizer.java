package dbms.parser;

import java.util.ArrayList;
import java.util.List;

/** Режет текст запроса на токены. */
public final class Tokenizer {
    private Tokenizer() { }

    /** Превращает запрос в список токенов. В конце всегда стоит EOF. */
    public static List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        int n = sql.length();
        int i = 0;
        while (i < n) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            int start = i;
            if (Character.isLetter(c) || c == '_') {
                while (i < n && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) i++;
                tokens.add(new Token(TokenType.IDENT, sql.substring(start, i), start));
            } else if (Character.isDigit(c) || (c == '-' && i + 1 < n && Character.isDigit(sql.charAt(i + 1)))) {
                i++;
                while (i < n && Character.isDigit(sql.charAt(i))) i++;
                if (i + 1 < n && sql.charAt(i) == '.' && Character.isDigit(sql.charAt(i + 1))) {
                    i++;
                    while (i < n && Character.isDigit(sql.charAt(i))) i++;
                }
                tokens.add(new Token(TokenType.NUMBER, sql.substring(start, i), start));
            } else if (c == '\'') {
                StringBuilder sb = new StringBuilder();
                boolean closed = false;
                i++;
                while (i < n) {
                    char d = sql.charAt(i);
                    if (d == '\'') {
                        if (i + 1 < n && sql.charAt(i + 1) == '\'') {
                            sb.append('\'');
                            i += 2;
                            continue;
                        }
                        closed = true;
                        i++;
                        break;
                    }
                    sb.append(d);
                    i++;
                }
                if (!closed) {
                    throw new IllegalArgumentException(
                            "Синтаксическая ошибка: не закрыта строка, начатая в позиции " + (start + 1));
                }
                tokens.add(new Token(TokenType.STRING, sb.toString(), start));
            } else if (c == '<' || c == '>' || c == '!') {
                char nx = i + 1 < n ? sql.charAt(i + 1) : '\0';
                String sym;
                if (nx == '=' || (c == '<' && nx == '>')) {
                    sym = "" + c + nx;
                    i += 2;
                } else if (c == '!') {
                    throw new IllegalArgumentException("Синтаксическая ошибка: после '!' ожидалось '=' (позиция " + (start + 1) + ")");
                } else {
                    sym = String.valueOf(c);
                    i++;
                }
                tokens.add(new Token(TokenType.SYMBOL, sym, start));
            } else if ("(),;*=".indexOf(c) >= 0) {
                i++;
                tokens.add(new Token(TokenType.SYMBOL, String.valueOf(c), start));
            } else {
                throw new IllegalArgumentException("Синтаксическая ошибка: неожиданный символ '" + c + "' (позиция " + (start + 1) + ")");
            }
        }
        tokens.add(new Token(TokenType.EOF, "", n));
        return tokens;
    }
}
