package com.yanny.ali.neoforge.gametest;

import com.yanny.ali.Utils;
import com.yanny.ali.gametest.LootDataGolden;
import com.yanny.ali.neoforge.AliMod;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class LootDataGameTest {
    private static final List<LootDataGolden.Variant> VARIANTS = List.of(
            new LootDataGolden.Variant("show_unbounded_global_loot_modifiers", (config) -> {
                boolean old = config.showUnboundedGlobalLootModifiers;

                config.showUnboundedGlobalLootModifiers = true;
                return () -> config.showUnboundedGlobalLootModifiers = old;
            })
    );

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, (helper) -> helper.register(Utils.modLoc("loot_data"), LootDataGameTest::lootData));
    }

    public static void lootData(GameTestHelper helper) {
        LootDataGolden.run(helper, AliMod.SERVER, VARIANTS, LootDataGameTest::scriptErrors);
    }

    private static List<String> scriptErrors() {
        return ScriptType.SERVER.console.errors.stream().map((line) -> "KubeJS: " + line.getText()).toList();
    }
}
