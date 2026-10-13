package com.yanny.alicompat.fabric.gametest;

import com.yanny.ali.fabric.CommonAliMod;
import com.yanny.alicompat.gametest.CompatDataGolden;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class CompatDataGameTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void compatData(GameTestHelper helper) {
        CompatDataGolden.run(helper, CommonAliMod.SERVER);
    }
}
