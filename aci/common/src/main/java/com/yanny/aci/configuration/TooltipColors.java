package com.yanny.aci.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.tooltip.TooltipStyle;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipColors {
    public static final Codec<TooltipColors> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
                Codec.STRING.fieldOf("text").orElse("gold").forGetter((c) -> c.text),
                Codec.STRING.fieldOf("value").orElse("aqua").forGetter((c) -> c.value),
                Codec.STRING.fieldOf("error").orElse("red").forGetter((c) -> c.error),
                Codec.STRING.fieldOf("branch").orElse("dark_gray").forGetter((c) -> c.branch)
        ).apply(instance, (text, value, error, branch) -> {
            TooltipColors colors = new TooltipColors();

            colors.text = text;
            colors.value = value;
            colors.error = error;
            colors.branch = branch;
            return colors;
        })
    );

    public String text = "gold";
    public String value = "aqua";
    public String error = "red";
    public String branch = "dark_gray";

    private TooltipStyle style = null;

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
        TextColor parsed = color != null ? TextColor.parseColor(color).result().orElse(null) : null;

        if (parsed == null) {
            CommonLogUtils.getLogger(modId).warn("Invalid tooltip color '{}' for '{}', using default", color, field);
            return fallback;
        }

        return Style.EMPTY.withColor(parsed);
    }
}
