package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinCanItemPerformAbility;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

@AliEntrypoint
public class NeoForgePlugin implements IPlugin {
    @Override
    public String getModId() {
        return "neoforge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanItemPerformAbility.class, NeoForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, NeoForgePlugin::getLootTableIdTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanItemPerformAbility condition) {
        MixinCanItemPerformAbility cond = (MixinCanItemPerformAbility) condition;
        return utils.getValueTooltip(utils, cond.getAbility()).build("ali.type.condition.can_item_perform_ability");
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return utils.getValueTooltip(utils, cond.getTargetLootTableId()).build("ali.type.condition.loot_table_id");
    }
}
