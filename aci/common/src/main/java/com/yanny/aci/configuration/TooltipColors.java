package com.yanny.aci.configuration;

import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipStyle;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipColors {
    public String text = "gold";
    public String value = "aqua";
    public String error = "red";
    public String branch = "dark_gray";

    private transient TooltipStyle style = null;

    @NotNull
    public TooltipStyle resolve(String modId) {
        if (style == null) {
            style = new TooltipStyle(
                    parse(modId, "text", text, TooltipStyle.DEFAULT.text()),
                    parse(modId, "value", value, TooltipStyle.DEFAULT.value()),
                    parse(modId, "error", error, TooltipStyle.DEFAULT.error()),
                    parse(modId, "branch", branch, TooltipStyle.DEFAULT.branch())
            );
        }

        return style;
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
