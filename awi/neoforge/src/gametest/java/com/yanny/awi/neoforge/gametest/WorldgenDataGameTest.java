package com.yanny.awi.neoforge.gametest;

import com.yanny.awi.Utils;
import com.yanny.awi.gametest.WorldgenDataGolden;
import com.yanny.awi.neoforge.AwiMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Utils.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldgenDataGameTest {
    @GameTest(template = "empty")
    public void worldgenData(GameTestHelper helper) {
        WorldgenDataGolden.run(helper, AwiMod.SERVER);
    }
}
