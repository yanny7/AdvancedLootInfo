package com.yanny.alicompat.forge.gametest;

import com.yanny.ali.forge.AliMod;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.gametest.CompatDataGolden;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Utils.MOD_ID)
@PrefixGameTestTemplate(false)
public class CompatDataGameTest {
    @GameTest(template = "empty")
    public void compatData(GameTestHelper helper) {
        CompatDataGolden.run(helper, AliMod.SERVER);
    }
}
