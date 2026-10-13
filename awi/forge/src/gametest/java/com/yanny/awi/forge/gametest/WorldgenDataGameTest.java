package com.yanny.awi.forge.gametest;

import com.yanny.awi.Utils;
import com.yanny.awi.forge.AwiMod;
import com.yanny.awi.gametest.WorldgenDataGolden;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(Utils.MOD_ID)
public class WorldgenDataGameTest {
    @GameTest(template = Utils.MOD_ID + ":empty")
    public void worldgenData(GameTestHelper helper) {
        WorldgenDataGolden.run(helper, AwiMod.SERVER);
    }
}
