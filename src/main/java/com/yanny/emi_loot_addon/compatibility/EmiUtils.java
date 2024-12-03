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
    public static MutableComponent translate(String key, Object... args) {
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
        return Component.translatable("emi.description.emi_loot_addon.unit", value, unit).withStyle(PARAM_STYLE).withStyle(ChatFormatting.BOLD);
    }
}
