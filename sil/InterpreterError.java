package sil;

public enum InterpreterError {
    NONE,
    INV_OP,
    INV_TYPE,
    INV_FUNC,
    INV_CELL,
    SELF_REF,
    CIRC_REF,
    INV_ARGS,
}
