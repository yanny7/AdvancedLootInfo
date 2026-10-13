package com.yanny.alicompat.fabric.gametest;

import com.yanny.ali.fabric.CommonAliMod;
import com.yanny.alicompat.gametest.CompatDataGolden;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class CompatDataGameTest {
    @GameTest
    public void compatData(GameTestHelper helper) {
        CompatDataGolden.run(helper, CommonAliMod.SERVER);
    }
}
