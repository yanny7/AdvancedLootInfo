package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinCanToolPerformAction;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.neoforged.neoforge.common.loot.CanToolPerformAction;
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
        registry.registerConditionTooltip(CanToolPerformAction.class, NeoForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, NeoForgePlugin::getLootTableIdTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction condition) {
        MixinCanToolPerformAction cond = (MixinCanToolPerformAction) condition;
        return GenericTooltipUtils.getStringTooltip(utils, "ali.type.condition.can_tool_perform_action", cond.getAction().name());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return GenericTooltipUtils.getResourceLocationTooltip(utils, "ali.type.condition.loot_table_id", cond.getTargetLootTableId());
    }
}
