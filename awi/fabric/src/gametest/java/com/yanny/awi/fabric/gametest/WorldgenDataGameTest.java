package com.yanny.awi.fabric.gametest;

import com.yanny.awi.fabric.CommonAwiMod;
import com.yanny.awi.gametest.WorldgenDataGolden;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class WorldgenDataGameTest {
    @GameTest
    public void worldgenData(GameTestHelper helper) {
        WorldgenDataGolden.run(helper, CommonAwiMod.SERVER);
    }
}
