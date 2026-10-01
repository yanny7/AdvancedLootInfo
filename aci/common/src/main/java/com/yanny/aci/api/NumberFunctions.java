package com.yanny.aci.api;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.number.BuiltinFunctions;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NumberFunctions {
    public static final ResourceLocation ADD = aci("add");
    public static final ResourceLocation SUB = aci("sub");
    public static final ResourceLocation MUL = aci("mul");
    public static final ResourceLocation DIV = aci("div");
    public static final ResourceLocation NEG = aci("neg");
    public static final ResourceLocation MOD = aci("mod");
    public static final ResourceLocation FLOOR_MOD = aci("floor_mod");
    public static final ResourceLocation FLOOR_DIV = aci("floor_div");
    public static final ResourceLocation POW = aci("pow");
    public static final ResourceLocation MIN = aci("min");
    public static final ResourceLocation MAX = aci("max");
    public static final ResourceLocation ABS = aci("abs");
    public static final ResourceLocation FLOOR = aci("floor");
    public static final ResourceLocation CEIL = aci("ceil");
    public static final ResourceLocation ROUND = aci("round");
    public static final ResourceLocation TRUNC = aci("trunc");
    public static final ResourceLocation SQRT = aci("sqrt");
    public static final ResourceLocation SIN = aci("sin");
    public static final ResourceLocation COS = aci("cos");
    public static final ResourceLocation LENGTH = aci("length");
    public static final ResourceLocation CLAMP = aci("clamp");
    public static final ResourceLocation AVG = aci("avg");
    public static final ResourceLocation UNIFORM_INT = aci("uniform_int");
    public static final ResourceLocation UNIFORM_FLOAT = aci("uniform_float");
    public static final ResourceLocation BINOMIAL = aci("binomial");
    public static final ResourceLocation NORMAL = aci("normal");
    public static final ResourceLocation BIASED_TO_BOTTOM = aci("biased_to_bottom");
    public static final ResourceLocation TRAPEZOID_FLOAT = aci("trapezoid_float");

    private static final Map<ResourceLocation, NumberFunction> FUNCTIONS = new ConcurrentHashMap<>();

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
    public static NumberFunction get(ResourceLocation id) {
        return FUNCTIONS.get(id);
    }

    @NotNull
    private static ResourceLocation aci(String path) {
        return ResourceLocation.fromNamespaceAndPath("aci", path);
    }
}
