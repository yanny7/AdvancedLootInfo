package com.yanny.emi_loot_addon.compatibility;

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
    public static MutableComponent translatableType(Enum<?> type, Object... args) {
        return translatable("emi.type.emi_loot_addon." + type.name().toLowerCase(), args);
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
        return Component.literal(value.toString()).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
    }

    @NotNull
    public static MutableComponent value(Object value, String unit) {
        return Component.translatable("emi.util.emi_loot_addon.two_values", value, unit).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
    }

    @NotNull
    public static MutableComponent pair(Object value1, Object value2) {
        return Component.translatable("emi.util.emi_loot_addon.two_values_with_space", value1, value2).withStyle(TEXT_STYLE);
    }

    @NotNull
    public static MutableComponent list(Object... args) {
        return Component.translatable("emi.util.emi_loot_addon.list." + args.length, args).withStyle(TEXT_STYLE);
    }
}
