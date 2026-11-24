package com.yanny.ali.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yanny.aci.compatibility.ScrollableTooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class MixinScreen {
    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void onRenderTooltipStart(PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(components);
    }

    @Inject(method = "renderTooltipInternal", at = @At("RETURN"))
    private void onRenderTooltipEnd(PoseStack poseStack, List<ClientTooltipComponent> components, int x, int y, CallbackInfo ci) {
        ScrollableTooltip.setRenderedComponents(List.of());
    }
}
