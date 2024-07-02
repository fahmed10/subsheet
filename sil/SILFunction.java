package sil;

public abstract class SILFunction<T> {
    public abstract boolean checkArgumentCount(int argCount);

    public abstract boolean checkArgumentTypes(Object[] args);

    public final T call(Object[] args) {
        if (!checkArgumentCount(args.length)) {
            throw new SILInterpreter.InterpreterException(InterpreterError.INV_ARGS);
        }

        if (!checkArgumentTypes(args)) {
            throw new SILInterpreter.InterpreterException(InterpreterError.INV_TYPE);
        }

        return onCall(args);
    }

    protected abstract T onCall(Object[] args);
}
