package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberExpr;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class NumberConverters {
    private NumberConverters() {
    }

    @NotNull
    public static <U, T> NumberExpr convert(String modId, ManagedRegistry<Class<?>, BiFunction<U, T, NumberExpr>> registry, U utils, T value,
                                            Function<T, String> typeId) {
        Optional<BiFunction<U, T, NumberExpr>> converter = registry.get(value.getClass());

        if (converter.isEmpty()) {
            return NumberExpr.opaque(safeTypeId(value, typeId));
        }

        try {
            return converter.get().apply(utils, value);
        } catch (Throwable e) {
            String id = safeTypeId(value, typeId);

            CommonLogUtils.getLogger(modId).warn("Failed to convert number provider {}: {}", id, e.getMessage(), e);
            return NumberExpr.opaque(id);
        }
    }

    @NotNull
    private static <T> String safeTypeId(T value, Function<T, String> typeId) {
        try {
            String id = typeId.apply(value);

            if (id != null) {
                return id;
            }
        } catch (Throwable ignored) {
        }

        return ManagedRegistry.classKeyName(value.getClass());
    }
}
