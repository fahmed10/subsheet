package sil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sil.TokenType.*;

public class SILParser {
    private final List<Token> tokens;
    private int current;
    public ParserError error = ParserError.NONE;

    public SILParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expression parse() {
        return expressionRule();
    }

    private Expression expressionRule() {
        return equalityRule();
    }

    private Expression equalityRule() {
        return binaryRule(this::comparisonRule, EQUAL, EXCLAMATION_EQUAL);
    }

    private Expression comparisonRule() {
        return binaryRule(this::addSubRule, LESSER_THAN, GREATER_THAN, LESSER_EQUAL, GREATER_EQUAL);
    }

    private Expression addSubRule() {
        return binaryRule(this::mulDivRule, PLUS, MINUS);
    }

    private Expression mulDivRule() {
        return binaryRule(this::unaryRule, ASTERISK, SLASH);
    }

    private Expression binaryRule(Function<Expression> leftRule, TokenType ...types) {
        Expression left = leftRule.call();

        if (matchNextToken(types)) {
            Token operator = previousToken();
            Expression right = binaryRule(leftRule, types);
            return new Expression.Binary(operator, left, right);
        }

        return left;
    }

    private Expression unaryRule() {
        if (matchNextToken(EXCLAMATION)) {
            return new Expression.Unary(previousToken(), unaryRule());
        }
        return primaryRule();
    }

    private Expression primaryRule() {
        if (matchNextToken(NUMBER, STRING)) {
            return new Expression.Literal(previousToken().value());
        } else if (matchNextToken(CELL)) {
            if (isNextToken(DOT_DOT)) {
                return rangeRule();
            }
            return new Expression.Cell(previousToken().lexeme());
        } else if (matchNextToken(TRUE, FALSE)) {
            return new Expression.Literal(previousToken().type() == TRUE);
        } else if (matchNextToken(PAREN_LEFT)) {
            return groupRule();
        } else if (isNextToken(IDENTIFIER)) {
            return callRule();
        }

        error = ParserError.UNX_TOKEN;
        return null;
    }

    private Expression rangeRule() {
        String startCellCode = previousToken().lexeme();
        matchNextToken(DOT_DOT);

        if (!matchNextToken(CELL)) {
            error = ParserError.UNX_TOKEN;
            return null;
        }

        String endCellCode = previousToken().lexeme();
        return new Expression.Range(startCellCode, endCellCode);
    }

    private Expression callRule() {
        String functionName = readToken().lexeme();
        forceMatchNextToken(PAREN_LEFT, ParserError.UNX_TOKEN);
        ArrayList<Expression> arguments = new ArrayList<>();
        if (matchNextToken(PAREN_RIGHT)) {
            return new Expression.Call(functionName, arguments);
        }

        do {
            arguments.add(expressionRule());
        } while (matchNextToken(COMMA));

        return new Expression.Call(functionName, arguments);
    }

    private Expression groupRule() {
        Expression expression = expressionRule();
        forceMatchNextToken(PAREN_RIGHT, ParserError.NO_PAREN);
        return new Expression.Group(expression);
    }

    private Token readToken() {
        Token token = tokens.get(current);
        current++;
        return token;
    }

    private void forceMatchNextToken(TokenType type, ParserError errorType) {
        if (!matchNextToken(type)) {
            error = errorType;
        }
    }

    private Token peekToken() {
        return tokens.get(current);
    }

    private boolean isNextToken(TokenType ...types) {
        return Arrays.stream(types).anyMatch(type -> type == peekToken().type());
    }

    private boolean matchNextToken(TokenType ...types) {
        if (isNextToken(types)) {
            readToken();
            return true;
        }

        return false;
    }

    private Token previousToken() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peekToken().type() == EOF;
    }
}
