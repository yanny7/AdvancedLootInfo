package com.yanny.aci.tooltip;

import com.yanny.aci.api.ICoreServerUtils;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CoreTooltipUtils {
    @NotNull
    public static List<TooltipLine> toLines(List<TooltipNode> tooltip, int pad, boolean showAdvancedTooltip, TooltipStyle style, NumberOptions options) {
        List<TooltipLine> lines = new ArrayList<>();

        for (TooltipNode node : tooltip) {
            lines.addAll(toLines(node, pad, showAdvancedTooltip, style, options));
        }

        return lines;
    }

    @NotNull
    public static List<TooltipLine> toLines(TooltipNode tooltip, int pad, boolean showAdvancedTooltip, TooltipStyle style, NumberOptions options) {
        return tooltip.getLines(pad, showAdvancedTooltip, style, options);
    }

    @NotNull
    public static <
            T,
            TServerUtils extends ICoreServerUtils<TServerUtils>
            > TooltipBuilder getBuiltInRegistryTooltip(TServerUtils utils, Registry<T> registry, T value) {
        return utils.getValueTooltip(utils, registry.getKey(value));
    }

    @NotNull
    public static <
            T,
            TServerUtils extends ICoreServerUtils<TServerUtils>
            > TooltipBuilder getBuiltInRegistryTooltip(TServerUtils utils, Registry<T> registry, T value, boolean showName) {
        if (showName) {
            return TooltipBuilder.value(TooltipBuilder.registryEntry(registry, value));
        }

        return getBuiltInRegistryTooltip(utils, registry, value);
    }

    @NotNull
    public static String enumKey(String modId, String owner, String constantName) {
        return modId + ".enum." + owner + "." + constantName.toLowerCase(Locale.ROOT);
    }

    @NotNull
    public static String enumOwnerPath(Class<?> type) {
        List<String> names = new ArrayList<>();

        for (Class<?> clazz = type; clazz != null; clazz = clazz.getEnclosingClass()) {
            String name = clazz.getSimpleName();

            if (name.isEmpty()) {
                throw new IllegalArgumentException("Cannot derive enum owner path from anonymous class " + clazz.getName());
            }

            names.add(0, toSnakeCase(name));
        }

        return String.join(".", names);
    }

    @NotNull
    public static String toSnakeCase(String name) {
        StringBuilder builder = new StringBuilder(name.length() + 4);

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (i > 0 && Character.isUpperCase(c)) {
                char prev = name.charAt(i - 1);
                boolean afterWord = Character.isLowerCase(prev) || Character.isDigit(prev);
                boolean acronymEnd = Character.isUpperCase(prev) && i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1));

                if (afterWord || acronymEnd) {
                    builder.append('_');
                }
            }

            builder.append(c);
        }

        return builder.toString().toLowerCase(Locale.ROOT);
    }
}
