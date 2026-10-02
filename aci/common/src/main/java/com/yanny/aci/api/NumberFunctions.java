package com.yanny.aci.api;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.number.BuiltinFunctions;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NumberFunctions {
    public static final Identifier ADD = aci("add");
    public static final Identifier SUB = aci("sub");
    public static final Identifier MUL = aci("mul");
    public static final Identifier DIV = aci("div");
    public static final Identifier NEG = aci("neg");
    public static final Identifier MOD = aci("mod");
    public static final Identifier FLOOR_MOD = aci("floor_mod");
    public static final Identifier FLOOR_DIV = aci("floor_div");
    public static final Identifier POW = aci("pow");
    public static final Identifier MIN = aci("min");
    public static final Identifier MAX = aci("max");
    public static final Identifier ABS = aci("abs");
    public static final Identifier FLOOR = aci("floor");
    public static final Identifier CEIL = aci("ceil");
    public static final Identifier ROUND = aci("round");
    public static final Identifier TRUNC = aci("trunc");
    public static final Identifier SQRT = aci("sqrt");
    public static final Identifier SIN = aci("sin");
    public static final Identifier COS = aci("cos");
    public static final Identifier LENGTH = aci("length");
    public static final Identifier CLAMP = aci("clamp");
    public static final Identifier AVG = aci("avg");
    public static final Identifier UNIFORM_INT = aci("uniform_int");
    public static final Identifier UNIFORM_FLOAT = aci("uniform_float");
    public static final Identifier BINOMIAL = aci("binomial");
    public static final Identifier NORMAL = aci("normal");
    public static final Identifier BIASED_TO_BOTTOM = aci("biased_to_bottom");
    public static final Identifier VERY_BIASED_TO_BOTTOM = aci("very_biased_to_bottom");
    public static final Identifier TRAPEZOID_FLOAT = aci("trapezoid_float");
    public static final Identifier TRAPEZOID_INT = aci("trapezoid_int");

    private static final Map<Identifier, NumberFunction> FUNCTIONS = new ConcurrentHashMap<>();

    static {
        BuiltinFunctions.registerAll();
    }

    private NumberFunctions() {
    }

    public static void register(NumberFunction function) {
        NumberFunction previous = FUNCTIONS.putIfAbsent(function.id(), function);

        if (previous != null && previous != function) {
            CommonLogUtils.getLogger(function.id().getNamespace())
                    .warn("Number function {} is already registered, keeping the first definition", function.id());
        }
    }

    @Nullable
    public static NumberFunction get(Identifier id) {
        return FUNCTIONS.get(id);
    }

    @NotNull
    private static Identifier aci(String path) {
        return Identifier.fromNamespaceAndPath("aci", path);
    }
}
