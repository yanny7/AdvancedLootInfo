package com.yanny.alicompat.neoforge.gametest;

import com.yanny.ali.neoforge.AliMod;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.gametest.CompatDataGolden;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.resource.NeoForgeReloadListeners;

import java.util.List;
import java.util.stream.StreamSupport;

@EventBusSubscriber(modid = Utils.MOD_ID)
public class CompatDataGameTest {
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, (helper) -> helper.register(Identifier.fromNamespaceAndPath(Utils.MOD_ID, "compat_data"), CompatDataGameTest::compatData));
    }

    public static void compatData(GameTestHelper helper) {
        LootModifierManager manager = helper.getLevel().getServer().getServerResources().managers().getListener(NeoForgeReloadListeners.LOOT_MODIFIERS_KEY);
        List<Class<?>> globalLootModifiers = StreamSupport.stream(manager.getSortedModifiers().spliterator(), false).<Class<?>>map(Object::getClass).distinct().toList();

        CompatDataGolden.run(helper, AliMod.SERVER, globalLootModifiers);
    }
}
