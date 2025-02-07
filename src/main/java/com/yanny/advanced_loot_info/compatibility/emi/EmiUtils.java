package com.yanny.advanced_loot_info.compatibility.emi;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class EmiUtils {
    private static final ChatFormatting TEXT_STYLE = ChatFormatting.GOLD;
    private static final ChatFormatting PARAM_STYLE = ChatFormatting.AQUA;

    private EmiUtils() {}

    @NotNull
    public static MutableComponent translatableType(String prefix, Enum<?> type, Object... args) {
        return translatable(prefix + "." + type.name().toLowerCase(), args);
    }

    @NotNull
    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(key, Arrays.stream(args).map((arg) -> {
            if (arg instanceof MutableComponent) {
                return arg;
            } else {
                return Component.literal(arg.toString()).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
            }
        }).toArray()).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static MutableComponent value(Object value) {
        if (value instanceof MutableComponent) {
            return ((MutableComponent) value).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
        } else {
            return Component.literal(value.toString()).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
        }
    }

    @NotNull
    public static MutableComponent value(Object value, String unit) {
        return Component.translatable("emi.util.advanced_loot_info.two_values", value, unit).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
    }

    @NotNull
    public static MutableComponent pair(Object value1, Object value2) {
        return Component.translatable("emi.util.advanced_loot_info.two_values_with_space", value1, value2).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static MutableComponent pad(int count, Object arg) {
        if (count > 0) {
            return pair(Component.translatable("emi.util.advanced_loot_info.pad." + count), arg);
        } else {
            if (arg instanceof MutableComponent) {
                return ((MutableComponent) arg).withStyle(TEXT_STYLE);
            } else {
                return Component.literal(arg.toString()).withStyle(TEXT_STYLE);
            }
        }
    }

    @NotNull
    public static MutableComponent keyValue(Object key, Object value) {
        return translatable("emi.util.advanced_loot_info.key_value", key instanceof MutableComponent ? key : Component.literal(key.toString()), value(value));
    }
}
