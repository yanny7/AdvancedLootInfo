package com.yanny.aci.configuration;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipStyle;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TooltipColors {
    public String text = "gold";
    public String value = "aqua";
    public String error = "red";
    public String branch = "dark_gray";
    public String secondary = "gray";
    public List<String> levels = List.of("green", "yellow", "light_purple", "blue", "red", "gold");

    private transient TooltipStyle style = null;

    @NotNull
    public TooltipStyle resolve(String modId) {
        if (style == null) {
            style = new TooltipStyle(
                    parse(modId, "text", text, TooltipStyle.DEFAULT.text()),
                    parse(modId, "value", value, TooltipStyle.DEFAULT.value()),
                    parse(modId, "error", error, TooltipStyle.DEFAULT.error()),
                    parse(modId, "branch", branch, TooltipStyle.DEFAULT.branch()),
                    parse(modId, "secondary", secondary, TooltipStyle.DEFAULT.secondary()),
                    parseLevels(modId)
            );
        }

        return style;
    }

    @NotNull
    private List<Integer> parseLevels(String modId) {
        List<Integer> colors = new ArrayList<>();

        if (levels != null) {
            for (String color : levels) {
                TextColor parsed = color != null ? TextColor.parseColor(color) : null;

                if (parsed != null) {
                    colors.add(parsed.getValue());
                } else {
                    CommonLogUtils.getLogger(modId).warn("Invalid tooltip color '{}' in 'levels', skipping", color);
                }
            }
        }

        return colors;
    }

    @NotNull
    private static Style parse(String modId, String field, @Nullable String color, Style fallback) {
        TextColor parsed = color != null ? TextColor.parseColor(color) : null;

        if (parsed == null) {
            CommonLogUtils.getLogger(modId).warn("Invalid tooltip color '{}' for '{}', using default", color, field);
            return fallback;
        }

        return Style.EMPTY.withColor(parsed);
    }
}
