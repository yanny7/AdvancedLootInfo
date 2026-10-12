package com.yanny.ali.forge.gametest;

import com.yanny.ali.Utils;
import com.yanny.ali.forge.AliMod;
import com.yanny.ali.gametest.LootDataGolden;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;

@GameTestHolder(Utils.MOD_ID)
public class LootDataGameTest {
    private static final List<LootDataGolden.Variant> VARIANTS = List.of(
            new LootDataGolden.Variant("show_unbounded_global_loot_modifiers", (config) -> {
                boolean old = config.showUnboundedGlobalLootModifiers;

                config.showUnboundedGlobalLootModifiers = true;
                return () -> config.showUnboundedGlobalLootModifiers = old;
            })
    );

    @GameTest(template = Utils.MOD_ID + ":empty", timeoutTicks = LootDataGolden.TIMEOUT_TICKS)
    public void lootData(GameTestHelper helper) {
        LootDataGolden.run(helper, AliMod.SERVER, VARIANTS, List::of);
    }
}
