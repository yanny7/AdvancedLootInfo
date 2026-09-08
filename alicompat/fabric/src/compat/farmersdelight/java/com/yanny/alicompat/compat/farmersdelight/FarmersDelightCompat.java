package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.Utils;
import com.yanny.alicompat.accessor.PluginUtils;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.loot.function.CopySkilletFunction;
import vectorwing.farmersdelight.common.loot.function.SmokerCookFunction;
import vectorwing.farmersdelight.refabricated.CanItemPerformAbility;
import vectorwing.farmersdelight.refabricated.ItemAbility;

public class FarmersDelightCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return FarmersDelightLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        PluginUtils.registerFunctionTooltip(registry, CopySkilletFunction.class, CopySkilletFunctionAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, SmokerCookFunction.class, SmokerCookFunctionAccessor::new);

        registry.registerEnumTranslation(ItemAbility.class, Utils.MOD_ID, FarmersDelightLang.ITEM_ABILITY);
        registry.registerConditionTooltip(CanItemPerformAbility.class, FarmersDelightCompat::getCanItemPerformAbilityTooltip);

        PluginUtils.registerItemListing(registry, FDItemListingAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getCanItemPerformAbilityTooltip(IServerUtils utils, CanItemPerformAbility cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.ability())), Lang.Conditions.CAN_ITEM_PERFORM_ABILITY);
    }
}
