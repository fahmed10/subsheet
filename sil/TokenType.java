package sil;

public enum TokenType {
    NUMBER,
    STRING,
    PAREN_LEFT, PAREN_RIGHT,
    CELL,
    IDENTIFIER,
    PLUS, MINUS, ASTERISK, SLASH, EXCLAMATION,
    GREATER_THAN, LESSER_THAN, GREATER_EQUAL, LESSER_EQUAL, EQUAL, EXCLAMATION_EQUAL,
    DOT_DOT,
    COMMA,
    TRUE, FALSE,
    EOF
}
