package com.yanny.alicompat.neoforge.gametest;

import com.yanny.ali.neoforge.AliMod;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.gametest.CompatDataGolden;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class CompatDataGameTest {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, (helper) -> helper.register(Identifier.fromNamespaceAndPath(Utils.MOD_ID, "compat_data"), CompatDataGameTest::compatData));
    }

    public static void compatData(GameTestHelper helper) {
        CompatDataGolden.run(helper, AliMod.SERVER);
    }
}
