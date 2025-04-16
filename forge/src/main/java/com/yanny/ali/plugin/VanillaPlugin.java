package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@AliEntrypoint
public class VanillaPlugin extends Plugin {
    @Override
    public void registerClient(IClientRegistry registry) {
        super.registerClient(registry);
        registry.registerConditionTooltip(CanToolPerformAction.LOOT_CONDITION_TYPE, VanillaPlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.LOOT_TABLE_ID, VanillaPlugin::getLootTableIdTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getCanToolPerformActionTooltip(IClientUtils utils, int pad, CanToolPerformAction cond) {
        return GenericTooltipUtils.getStringTooltip(utils, pad, "ali.type.condition.can_tool_perform_action", cond.action.name());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLootTableIdTooltip(IClientUtils utils, int pad, LootTableIdCondition cond) {
        return GenericTooltipUtils.getResourceLocationTooltip(utils, pad, "ali.type.condition.loot_table_id", ((MixinLootTableIdCondition) cond).getTargetLootTableId());
    }
}
