package com.yanny.ali.neoforge;

import com.yanny.aci.compatibility.ScrollableTooltip;
import com.yanny.ali.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

@EventBusSubscriber(modid = Utils.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientModBusSubscriber {
    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ScrollableTooltip.class, (tooltip) -> tooltip);
    }
}
