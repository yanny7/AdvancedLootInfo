package com.yanny.aci.compatibility;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class SmallFont {
    private static final ResourceLocation FONT = ResourceLocation.fromNamespaceAndPath("aci", "small_digits");
    private static final String CHARS = "0123456789.,-−%";

    private SmallFont() {}

    public static boolean canRender(String text) {
        return text.chars().allMatch((c) -> CHARS.indexOf(c) >= 0);
    }

    @NotNull
    public static Component text(String text) {
        return Component.literal(text).withStyle((s) -> s.withFont(FONT));
    }
}
