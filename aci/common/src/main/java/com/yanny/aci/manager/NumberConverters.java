package com.yanny.aci.manager;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.NumberConverter;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class NumberConverters {
    private NumberConverters() {
    }

    @NotNull
    public static <U, T> NumberExpr convert(String modId, ManagedRegistry<Class<?>, NumberConverter<U, T>> registry, U utils, T value,
                                            List<TooltipNode> conditions,                                             Function<T, String> typeId) {
        Optional<NumberConverter<U, T>> converter = registry.get(value.getClass());

        if (converter.isEmpty()) {
            return NumberExpr.opaque(safeTypeId(value, typeId));
        }

        try {
            return converter.get().convert(utils, value, conditions);
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
