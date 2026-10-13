package com.yanny.alicompat.compat.appliedcooking;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.blay09.mods.balm.neoforge.world.level.storage.loot.internal.NeoForgeBalmLootModifier;
import org.jetbrains.annotations.NotNull;
import sebastrn.appliedcooking.lootable.KitchenStationBlockLootFunction;

import java.util.Optional;

public class AppliedCookingCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return AppliedCookingLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, KitchenStationBlockLootFunction.class, KitchenStationBlockLootFunctionAccessor::new);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        registry.registerGlobalLootModifier(NeoForgeBalmLootModifier.class, AppliedCookingCompat::getBalmLootModifier);
    }

    @NotNull
    private static Optional<IPageLootModifier> getBalmLootModifier(IServerUtils ignoredUtils, NeoForgeBalmLootModifier ignoredModifier) {
        return Optional.empty();
    }
}
