package com.yanny.ali.plugin.server.bytecode;

import com.yanny.aci.api.NumberInterval;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

final class Arithmetic implements Opcodes {
    private Arithmetic() {}

    @NotNull
    static Object compute(int op, Object a, Object b) {
        return switch (op) {
            case IADD -> i(a) + i(b);
            case ISUB -> i(a) - i(b);
            case IMUL -> i(a) * i(b);
            case IDIV -> i(a) / nonZero(i(b));
            case IREM -> i(a) % nonZero(i(b));
            case ISHL -> i(a) << i(b);
            case ISHR -> i(a) >> i(b);
            case IUSHR -> i(a) >>> i(b);
            case IAND -> i(a) & i(b);
            case IOR -> i(a) | i(b);
            case IXOR -> i(a) ^ i(b);
            case LADD -> l(a) + l(b);
            case LSUB -> l(a) - l(b);
            case LMUL -> l(a) * l(b);
            case LDIV -> l(a) / nonZero(l(b));
            case LREM -> l(a) % nonZero(l(b));
            case LSHL -> l(a) << i(b);
            case LSHR -> l(a) >> i(b);
            case LUSHR -> l(a) >>> i(b);
            case LAND -> l(a) & l(b);
            case LOR -> l(a) | l(b);
            case LXOR -> l(a) ^ l(b);
            case FADD -> f(a) + f(b);
            case FSUB -> f(a) - f(b);
            case FMUL -> f(a) * f(b);
            case FDIV -> f(a) / f(b);
            case FREM -> f(a) % f(b);
            case DADD -> d(a) + d(b);
            case DSUB -> d(a) - d(b);
            case DMUL -> d(a) * d(b);
            case DDIV -> d(a) / d(b);
            case DREM -> d(a) % d(b);
            default -> throw new Fail("arithmetic " + op);
        };
    }

    static Object negate(Object a) {
        if (a instanceof Integer i) {
            return -i;
        } else if (a instanceof Long l) {
            return -l;
        } else if (a instanceof Float f) {
            return -f;
        } else if (a instanceof Double d) {
            return -d;
        }

        throw new Fail("negate");
    }

    static Object convert(int op, Object a) {
        return switch (op) {
            case I2L -> (long) i(a);
            case I2F -> (float) i(a);
            case I2D -> (double) i(a);
            case L2I -> (int) l(a);
            case L2F -> (float) l(a);
            case L2D -> (double) l(a);
            case F2I -> (int) f(a);
            case F2L -> (long) f(a);
            case F2D -> (double) f(a);
            case D2I -> (int) d(a);
            case D2L -> (long) d(a);
            case D2F -> (float) d(a);
            case I2B -> (int) (byte) i(a);
            case I2C -> (int) (char) i(a);
            case I2S -> (int) (short) i(a);
            default -> throw new Fail("conversion " + op);
        };
    }

    static int compare(int op, Object a, Object b) {
        if (op == LCMP) {
            return Long.compare(l(a), l(b));
        }

        double x = op == FCMPL || op == FCMPG ? f(a) : d(a);
        double y = op == FCMPL || op == FCMPG ? f(b) : d(b);

        if (x < y) {
            return -1;
        } else if (x > y) {
            return 1;
        } else if (x == y) {
            return 0;
        }

        return op == FCMPG || op == DCMPG ? 1 : -1;
    }

    static Value.Kind kindOf(int op) {
        if (op >= ISHL) {
            return (op - ISHL) % 2 == 0 ? Value.Kind.INT : Value.Kind.LONG;
        }

        return switch ((op - IADD) % 4) {
            case 0 -> Value.Kind.INT;
            case 1 -> Value.Kind.LONG;
            case 2 -> Value.Kind.FLOAT;
            default -> Value.Kind.DOUBLE;
        };
    }

    @Nullable
    static Boolean decide(int op, NumberInterval difference) {
        if (difference.isUnknown()) {
            return null;
        }

        return switch (op) {
            case IFEQ -> equalsZero(difference);
            case IFNE -> not(equalsZero(difference));
            case IFLT -> belowZero(difference);
            case IFGE -> not(belowZero(difference));
            case IFGT -> aboveZero(difference);
            case IFLE -> not(aboveZero(difference));
            default -> null;
        };
    }

    static int intOf(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Integer i) {
            return i;
        }

        throw new Fail("symbolic trade number");
    }

    static float floatOf(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Float f) {
            return f;
        }

        throw new Fail("symbolic float");
    }

    static double doubleOf(Value value) {
        if (value instanceof Value.Known known && known.value() instanceof Double d) {
            return d;
        }

        throw new Fail("symbolic double");
    }

    @Nullable
    private static Boolean equalsZero(NumberInterval d) {
        if (d.isPoint() && d.lo() == 0) {
            return true;
        }

        boolean containsZero = (d.lo() < 0 || d.lo() == 0 && d.loClosed()) && (d.hi() > 0 || d.hi() == 0 && d.hiClosed());

        return containsZero ? null : false;
    }

    @Nullable
    private static Boolean belowZero(NumberInterval d) {
        if (d.hi() < 0 || d.hi() == 0 && !d.hiClosed()) {
            return true;
        }

        return d.lo() >= 0 ? false : null;
    }

    @Nullable
    private static Boolean aboveZero(NumberInterval d) {
        if (d.lo() > 0 || d.lo() == 0 && !d.loClosed()) {
            return true;
        }

        return d.hi() <= 0 ? false : null;
    }

    @Nullable
    private static Boolean not(@Nullable Boolean value) {
        return value == null ? null : !value;
    }

    private static int nonZero(int value) {
        if (value == 0) {
            throw new Fail("division by zero");
        }

        return value;
    }

    private static long nonZero(long value) {
        if (value == 0) {
            throw new Fail("division by zero");
        }

        return value;
    }

    private static int i(Object value) {
        return (Integer) value;
    }

    private static long l(Object value) {
        return (Long) value;
    }

    private static float f(Object value) {
        return (Float) value;
    }

    private static double d(Object value) {
        return (Double) value;
    }
}
