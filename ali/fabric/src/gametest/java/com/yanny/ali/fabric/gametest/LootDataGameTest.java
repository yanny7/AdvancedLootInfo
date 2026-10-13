package com.yanny.ali.fabric.gametest;

import com.yanny.ali.fabric.CommonAliMod;
import com.yanny.ali.gametest.LootDataGolden;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;

public class LootDataGameTest {
    @GameTest(maxTicks = LootDataGolden.TIMEOUT_TICKS)
    public void lootData(GameTestHelper helper) {
        LootDataGolden.run(helper, CommonAliMod.SERVER, List.of(), List::of);
    }
}
