package com.yanny.ali.forge;

import com.yanny.aci.compatibility.ScrollableTooltip;
import com.yanny.ali.Utils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Utils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeClientModBusSubscriber {
    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ScrollableTooltip.class, (tooltip) -> tooltip);
    }
}
