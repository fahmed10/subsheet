package sil;

import java.util.*;
import static sil.TokenType.*;

public class SILTokenizer {
    private final String code;
    private final List<Token> tokens = new ArrayList<>();
    private int start;
    private int current;
    public TokenizerError error = TokenizerError.NONE;

    private static final HashMap<Character, TokenType> operators = new HashMap<>();
    private static final HashMap<String, TokenType> compoundOperators = new HashMap<>();
    private static final HashMap<String, TokenType> keywords = new HashMap<>();

    static {
        operators.put('+', PLUS);
        operators.put('-', MINUS);
        operators.put('*', ASTERISK);
        operators.put('/', SLASH);
        operators.put('>', GREATER_THAN);
        operators.put('<', LESSER_THAN);
        operators.put('(', PAREN_LEFT);
        operators.put(')', PAREN_RIGHT);
        operators.put('=', EQUAL);
        operators.put(',', COMMA);
        operators.put('!', EXCLAMATION);
        compoundOperators.put(">=", GREATER_EQUAL);
        compoundOperators.put("<=", LESSER_EQUAL);
        compoundOperators.put("!=", EXCLAMATION_EQUAL);
        compoundOperators.put("..", DOT_DOT);
        keywords.put("True", TRUE);
        keywords.put("False", FALSE);
    }

    public SILTokenizer(String code) {
        this.code = code;
    }

    public List<Token> tokenize() {
        while (current < code.length()) {
            readToken();
            start = current;
        }

        addToken(EOF);

        return tokens;
    }

    private void readToken() {
        char c = readChar();
        if (c == '"') {
            readString();
        }
        else if (isCharAsciiDigit(c) || (c == '.' && peekChar() != '.')) {
            readNumber();
        }
        else if (c >= 'A' && c <= 'Z' && ((peekChar() >= 'A' && peekChar() <= 'Z') || isCharAsciiDigit(peekChar()))) {
            readCell();
        }
        else if (isCharAsciiLetter(c)) {
            readIdentifier();
        }
        else if (compoundOperators.keySet().stream().anyMatch(o -> c == o.charAt(0))) {
            String operator = compoundOperators.keySet().stream().filter(o -> c == o.charAt(0)).findAny().get();
            if (matchChar(operator.charAt(1))) {
                addToken(compoundOperators.get(operator));
            } else if (operators.containsKey(c)) {
                addToken(operators.get(c));
            }
        }
        else if (operators.containsKey(c)) {
            addToken(operators.get(c));
        }
        else if (c != '\r' && c != '\n' && c != '\t' && c != ' ') {
            error = TokenizerError.INV_CHAR;
        }
    }

    private void readString() {
        while (current < code.length() && peekChar() != '"') {
            current++;
        }
        current++;

        addToken(STRING, code.substring(start + 1, current - 1));
    }

    private void readNumber() {
        while (current < code.length() && (isCharAsciiDigit(peekChar()) || peekChar() == '.')) {
            current++;
        }

        try {
            addToken(NUMBER, Double.parseDouble(code.substring(start, current)));
        }
        catch (NumberFormatException ex) {
            error = TokenizerError.INV_NUM;
        }
    }

    private void readCell() {
        while (current < code.length() && peekChar() >= 'A' && peekChar() <= 'Z') {
            current++;
        }

        if (current >= code.length() || !isCharAsciiDigit(peekChar())) {
            error = TokenizerError.INV_CELL;
            return;
        }

        while (current < code.length() && isCharAsciiDigit(peekChar())) {
            current++;
        }

        addToken(CELL);
    }

    private void readIdentifier() {
        while (current < code.length() && ((peekChar() >= 'a' && peekChar() <= 'z') || isCharAsciiDigit(peekChar()))) {
            current++;
        }

        if (keywords.containsKey(code.substring(start, current))) {
            addToken(keywords.get(code.substring(start, current)));
            return;
        }

        addToken(IDENTIFIER);
    }

    private void addToken(TokenType type) {
        tokens.add(new Token(code.substring(start, current), type, null));
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(code.substring(start, current), type, literal));
    }

    private char readChar() {
        char c = code.charAt(current);
        current++;
        return c;
    }

    private boolean matchChar(char c) {
        if (code.charAt(current) == c) {
            readChar();
            return true;
        }

        return false;
    }

    private char peekChar() {
        return code.charAt(current);
    }

    private boolean isCharAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isCharAsciiLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
