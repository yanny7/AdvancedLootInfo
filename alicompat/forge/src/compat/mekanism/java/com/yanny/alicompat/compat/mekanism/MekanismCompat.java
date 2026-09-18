package com.yanny.alicompat.compat.mekanism;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import mekanism.common.item.loot.PersonalStorageContentsLootFunction;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import org.jetbrains.annotations.NotNull;

public class MekanismCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return MekanismLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(PersonalStorageContentsLootFunction.class, MekanismCompat::personalStorageContentsTooltip);

        registry.registerValueTooltip(FullCanteenItemPredicate.class, MekanismCompat::fullCanteenItemPredicateTooltip);
        PluginUtils.registerValueTooltip(registry, MaxedModuleContainerItemPredicate.class, MaxedModuleContainerItemPredicateAccessor.class);
    }

    @NotNull
    private static TooltipBuilder personalStorageContentsTooltip(IServerUtils utils, PersonalStorageContentsLootFunction fun) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, MekanismLang.Functions.COPY_PERSONAL_STORAGE_CONTENTS);
    }

    @NotNull
    private static TooltipBuilder fullCanteenItemPredicateTooltip(IServerUtils utils, FullCanteenItemPredicate predicate) {
        return TooltipBuilder.keyOnly(MekanismLang.Value.FULL_CANTEEN);
    }
}
