package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.mods.PluginUtils;
import com.yanny.alicompat.IModCompat;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;
import vectorwing.farmersdelight.common.loot.function.SmokerCookFunction;
import vectorwing.farmersdelight.refabricated.CanItemPerformAbility;

public class FarmersDelightCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return FarmersDelightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerFunctionTooltip(CopySkilletFunction.class, (utils, function) -> new CopySkilletFunctionAccessor(function).getTooltip(utils));
        registry.registerFunctionTooltip(SmokerCookFunction.class, (utils, function) -> new SmokerCookFunctionAccessor(function).getTooltip(utils));

        registry.registerConditionTooltip(CanItemPerformAbility.class, (utils, condition) -> utils.getValueTooltip(utils, condition.ability()).key(Lang.Conditions.CAN_ITEM_PERFORM_ABILITY));

        PluginUtils.registerItemListing(registry, FDItemListingAccessor.class);
    }
}
