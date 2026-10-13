package com.yanny.alicompat.neoforge.gametest;

import com.yanny.ali.neoforge.AliMod;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.gametest.CompatDataGolden;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Utils.MOD_ID)
@PrefixGameTestTemplate(false)
public class CompatDataGameTest {
    @GameTest(template = "empty")
    public void compatData(GameTestHelper helper) {
        CompatDataGolden.run(helper, AliMod.SERVER);
    }
}
