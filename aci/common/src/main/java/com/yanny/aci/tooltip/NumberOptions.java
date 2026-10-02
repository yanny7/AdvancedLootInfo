package com.yanny.aci.tooltip;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record NumberOptions(boolean showFormulas, boolean showCharts, Locale locale) {
    public static final NumberOptions DEFAULT = new NumberOptions(false, false, Locale.ROOT);

    @NotNull
    public static NumberOptions client(boolean showCharts) {
        Minecraft minecraft = Minecraft.getInstance();
        String[] code = minecraft.getLanguageManager().getSelected().split("_", 2);
        Locale locale = code.length == 2 ? new Locale(code[0], code[1]) : new Locale(code[0]);

        return new NumberOptions(minecraft.options.advancedItemTooltips, showCharts, locale);
    }
}
