package com.yanny.awi.neoforge.gametest;

import com.yanny.awi.Utils;
import com.yanny.awi.gametest.WorldgenDataGolden;
import com.yanny.awi.neoforge.AwiMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class WorldgenDataGameTest {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, (helper) -> helper.register(Utils.modLoc("worldgen_data"), WorldgenDataGameTest::worldgenData));
    }

    public static void worldgenData(GameTestHelper helper) {
        WorldgenDataGolden.run(helper, AwiMod.SERVER);
    }
}
