package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

@AliEntrypoint
public class VanillaPlugin extends Plugin {
    @Override
    public void registerServer(IServerRegistry registry) {
        super.registerServer(registry);
        registry.registerConditionTooltip(CanToolPerformAction.class, VanillaPlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, VanillaPlugin::getLootTableIdTooltip);
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getCanToolPerformActionTooltip(IServerUtils utils, CanToolPerformAction cond) {
        return GenericTooltipUtils.getStringTooltip(utils, "ali.type.condition.can_tool_perform_action", cond.action.name());
    }

    @Unmodifiable
    @NotNull
    public static ITooltipNode getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition cond) {
        return GenericTooltipUtils.getResourceLocationTooltip(utils, "ali.type.condition.loot_table_id", ((MixinLootTableIdCondition) cond).getTargetLootTableId());
    }
}
