package com.yanny.awi.plugin.common;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.api.NumberFunction;
import com.yanny.aci.api.NumberFunctions;
import com.yanny.aci.api.NumberText;
import com.yanny.awi.Utils;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.aci.api.NumberExpr.*;

public final class HeightFunctions {
    public static final Identifier BIASED_TO_BOTTOM = Utils.modLoc("biased_to_bottom");
    public static final Identifier VERY_BIASED_TO_BOTTOM = Utils.modLoc("very_biased_to_bottom");
    public static final Identifier TRAPEZOID = Utils.modLoc("trapezoid");

    private static final NumberFunction BIASED = NumberFunction.builder(BIASED_TO_BOTTOM).arity(3)
            .expand((a) -> add(a.get(0), uniformInt(constant(0), add(uniformInt(constant(0), sub(sub(a.get(1), a.get(0)), a.get(2))), a.get(2), constant(-1)))))
            .format((a, f) -> hideUnitInner(BIASED_TO_BOTTOM, a, f))
            .build();
    private static final NumberFunction VERY_BIASED = NumberFunction.builder(VERY_BIASED_TO_BOTTOM).arity(3)
            .expand((a) -> {
                NumberExpr k = uniformInt(add(a.get(0), a.get(2)), a.get(1));
                NumberExpr l = uniformInt(a.get(0), sub(k, constant(1)));

                return uniformInt(a.get(0), add(l, constant(-1), a.get(2)));
            })
            .format((a, f) -> hideUnitInner(VERY_BIASED_TO_BOTTOM, a, f))
            .build();
    private static final NumberFunction TRAPEZOID_HEIGHT = NumberFunction.builder(TRAPEZOID).arity(3)
            .expand(HeightFunctions::trapezoid)
            .build();

    private HeightFunctions() {
    }

    public static void register() {
        NumberFunctions.register(BIASED);
        NumberFunctions.register(VERY_BIASED);
        NumberFunctions.register(TRAPEZOID_HEIGHT);
    }

    @Nullable
    private static NumberExpr trapezoid(List<NumberExpr> a) {
        if (!(a.get(0) instanceof Const min) || !(a.get(1) instanceof Const max) || !(a.get(2) instanceof Const plateau)) {
            return null;
        }

        double k = max.value() - min.value();

        if (plateau.value() >= k) {
            return uniformInt(min.value(), max.value());
        }

        double l = Math.floor((k - plateau.value()) / 2);

        return add(min, uniformInt(0, k - l), uniformInt(0, l));
    }

    @Nullable
    private static NumberText hideUnitInner(Identifier id, List<NumberExpr> args, List<NumberText> formatted) {
        if (!args.get(2).equals(constant(1))) {
            return null;
        }

        List<NumberText> parts = new ArrayList<>();

        parts.add(NumberText.key(NumberFunction.translationKey(id)));
        parts.add(NumberText.str("("));
        parts.add(formatted.get(0));
        parts.add(NumberText.str("; "));
        parts.add(formatted.get(1));
        parts.add(NumberText.str(")"));
        return NumberText.seq(parts);
    }

    @NotNull
    public static NumberExpr biasedToBottom(int min, int max, int inner) {
        return fn(BIASED_TO_BOTTOM, constant(min), constant(max), constant(inner));
    }

    @NotNull
    public static NumberExpr veryBiasedToBottom(int min, int max, int inner) {
        return fn(VERY_BIASED_TO_BOTTOM, constant(min), constant(max), constant(inner));
    }

    @NotNull
    public static NumberExpr trapezoid(int min, int max, int plateau) {
        return fn(TRAPEZOID, constant(min), constant(max), constant(plateau));
    }
}
