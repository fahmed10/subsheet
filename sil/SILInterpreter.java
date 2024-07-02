package sil;

import subsheet.CellCode;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static sil.Expression.*;

public class SILInterpreter implements Visitor<Object> {
    public interface SpreadsheetInterface {
        String getValueFromCell(String cellCode);
        String getValueFromCell(CellCode cellCode);
        String getCellCode();
    }

    public static class InterpreterException extends RuntimeException {
        public InterpreterError type;

        public InterpreterException(InterpreterError type) {
            this.type = type;
        }
    }

    public InterpreterError error = InterpreterError.NONE;
    private static SpreadsheetInterface spreadsheetInterface;
    private static final HashMap<String, SILFunction<?>> functions = new HashMap<>();

    static {
        functions.put("if", new SILFunction<>() {
            @Override
            public boolean checkArgumentCount(int argCount) {
                return argCount == 3;
            }

            @Override
            public boolean checkArgumentTypes(Object[] args) {
                return args[0] instanceof Boolean;
            }

            @Override
            protected Object onCall(Object[] args) {
                return args[((boolean) args[0]) ? 1 : 2];
            }
        });
        functions.put("sum", new SILFunction<>() {
            @Override
            public boolean checkArgumentCount(int argCount) {
                return argCount == 1;
            }

            @Override
            public boolean checkArgumentTypes(Object[] args) {
                return args[0] instanceof SILRange;
            }

            @Override
            protected Object onCall(Object[] args) {
                SILRange range = ((SILRange) args[0]);
                double sum = 0;

                for (CellCode cellCode : range.getCellsInRange()) {
                    String value = spreadsheetInterface.getValueFromCell(cellCode);
                    Double d = tryParseDouble(value);
                    if (d != null) {
                        sum += d;
                    } else if (!value.isEmpty()) {
                        throw new InterpreterException(InterpreterError.INV_TYPE);
                    }
                }

                return sum;
            }
        });
    }

    public Object interpret(Expression expression, SpreadsheetInterface spreadsheetInterface) {
        SILInterpreter.spreadsheetInterface = spreadsheetInterface;

        try {
            return evaluate(expression);
        } catch (InterpreterException ex) {
            error = ex.type;
            return null;
        } catch (StackOverflowError ex) {
            error = InterpreterError.CIRC_REF;
            return null;
        }
    }

    private static Double tryParseDouble(String s) {
        try {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    Object evaluate(Expression expression) {
        return expression.visit(this);
    }

    @Override
    public Object visit(Binary binary) {
        Object left = evaluate(binary.left());
        Object right = evaluate(binary.right());

        switch (binary.operator().type()) {
            case PLUS -> {
                if (left instanceof Double a && right instanceof Double b) {
                    return a + b;
                } else if ((left instanceof String || left instanceof Double) && (right instanceof String || right instanceof Double)) {
                    String a = left.toString();
                    String b = right.toString();
                    DecimalFormat formatter = new DecimalFormat("#.######");
                    if (left instanceof Double d) {
                        a = formatter.format(d);
                    }
                    if (right instanceof Double d) {
                        b = formatter.format(d);
                    }
                    return a + b;
                }
            }
            case MINUS -> {
                return implementDoubleOperator(left, right, (a, b) -> a - b);
            }
            case ASTERISK -> {
                return implementDoubleOperator(left, right, (a, b) -> a * b);
            }
            case SLASH -> {
                return implementDoubleOperator(left, right, (a, b) -> a / b);
            }
            case EQUAL -> {
                return Objects.equals(left, right);
            }
            case EXCLAMATION_EQUAL -> {
                return !Objects.equals(left, right);
            }
            case LESSER_THAN -> {
                return implementComparisonOperator(left, right, (a, b) -> a < b);
            }
            case GREATER_THAN -> {
                return implementComparisonOperator(left, right, (a, b) -> a > b);
            }
            case LESSER_EQUAL -> {
                return implementComparisonOperator(left, right, (a, b) -> a <= b);
            }
            case GREATER_EQUAL -> {
                return implementComparisonOperator(left, right, (a, b) -> a >= b);
            }
        }

        throw new InterpreterException(InterpreterError.NONE);
    }

    private Double implementDoubleOperator(Object left, Object right, BinaryOperator<Double> operator) {
        if (left instanceof Double a && right instanceof Double b) {
            return operator.apply(a, b);
        } else {
            throw new InterpreterException(InterpreterError.INV_OP);
        }
    }

    private boolean implementComparisonOperator(Object left, Object right, BiFunction<Double, Double, Boolean> operator) {
        if (left instanceof Double a && right instanceof Double b) {
            return operator.apply(a, b);
        } else {
            throw new InterpreterException(InterpreterError.INV_OP);
        }
    }

    @Override
    public Object visit(Unary unary) {
        if (unary.operator().type() == TokenType.EXCLAMATION) {
            Object value = evaluate(unary.expression());
            if (value instanceof Boolean b) {
                return !b;
            }

            throw new InterpreterException(InterpreterError.INV_OP);
        }

        throw new InterpreterException(InterpreterError.NONE);
    }

    @Override
    public Object visit(Literal literal) {
        return literal.value();
    }

    @Override
    public Object visit(Cell cell) {
        if (cell.cellCode().equals(spreadsheetInterface.getCellCode())){
            throw new InterpreterException(InterpreterError.SELF_REF);
        }

        String value = spreadsheetInterface.getValueFromCell(cell.cellCode());
        Double number = tryParseDouble(value);
        if (number != null && !value.contains("f") && !value.contains("d") && !value.contains("e")) {
            return number;
        } else {
            return value;
        }
    }

    @Override
    public Object visit(Group group) {
        return evaluate(group.expression());
    }

    @Override
    public Object visit(Call call) {
        if (!functions.containsKey(call.functionName())) {
            throw new InterpreterException(InterpreterError.INV_FUNC);
        }

        return functions.get(call.functionName()).call(call.arguments().stream().map(e -> evaluate((Expression) e)).toArray());
    }

    @Override
    public Object visit(Range range) {
        return new SILRange(CellCode.fromString(range.startCellCode()), CellCode.fromString(range.endCellCode()));
    }
}
