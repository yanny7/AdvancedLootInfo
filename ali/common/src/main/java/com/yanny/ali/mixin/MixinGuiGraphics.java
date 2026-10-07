package com.yanny.ali.mixin;

import com.yanny.aci.compatibility.ScrollableTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class MixinGuiGraphics {
    private static final String TOOLTIP = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V";
    // NeoForge-only overload, called directly by the tooltip render path instead of the vanilla one
    private static final String TOOLTIP_STACK = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ZLnet/minecraft/world/item/ItemStack;)V";

    @Inject(method = TOOLTIP, at = @At("HEAD"))
    private void onRenderTooltipStart(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier background, boolean extraSpaceAfterFirstLine, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(components, extraSpaceAfterFirstLine);
    }

    @Inject(method = TOOLTIP, at = @At("RETURN"))
    private void onRenderTooltipEnd(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier background, boolean extraSpaceAfterFirstLine, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(List.of(), false);
    }

    @Inject(method = TOOLTIP_STACK, at = @At("HEAD"), require = 0)
    private void onRenderStackTooltipStart(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier background, boolean extraSpaceAfterFirstLine, ItemStack stack, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(components, extraSpaceAfterFirstLine);
    }

    @Inject(method = TOOLTIP_STACK, at = @At("RETURN"), require = 0)
    private void onRenderStackTooltipEnd(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier background, boolean extraSpaceAfterFirstLine, ItemStack stack, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(List.of(), false);
    }
}
