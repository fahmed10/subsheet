package sil;

import java.util.List;

public interface Expression {
    interface Visitor<T> {
        T visit(Binary binary);
        T visit(Literal literal);
        T visit(Cell cell);
        T visit(Group group);
        T visit(Unary unary);
        T visit(Call call);
        T visit(Range range);
    }

    <T> T visit(Visitor<T> visitor);

    record Binary(Token operator, Expression left, Expression right) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
    record Literal(Object value) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
    record Cell(String cellCode) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
    record Group(Expression expression) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
    record Unary(Token operator, Expression expression) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
    record Call(String functionName, List<Expression> arguments) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
    record Range(String startCellCode, String endCellCode) implements Expression {
        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
