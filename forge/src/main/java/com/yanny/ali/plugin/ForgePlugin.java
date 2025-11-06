package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

@AliEntrypoint
public class ForgePlugin implements IPlugin {
    @Override
    public String getModId() {
        return "forge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanToolPerformAction.class, ForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, ForgePlugin::getLootTableIdTooltip);
    }

    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return utils.getValueTooltip(utils, cond.action.name()).key("ali.type.condition.can_tool_perform_action");
    }

    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return utils.getValueTooltip(utils, ((MixinLootTableIdCondition) cond).getTargetLootTableId()).key("ali.type.condition.loot_table_id");
    }
}
