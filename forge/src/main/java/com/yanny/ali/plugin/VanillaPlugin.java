package com.yanny.ali.plugin;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.condition.CanToolPerformActionAliCondition;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@AliEntrypoint
public class VanillaPlugin extends CommonPlugin {
    @Override
    public void registerCommon(ICommonRegistry registry) {
        super.registerCommon(registry);
        registry.registerCondition(CanToolPerformActionAliCondition.class, getKey(CanToolPerformAction.LOOT_CONDITION_TYPE), CanToolPerformActionAliCondition::new, CanToolPerformActionAliCondition::new);
    }

    @Override
    public void registerClient(IClientRegistry registry) {
        super.registerClient(registry);
        registry.registerConditionTooltip(CanToolPerformActionAliCondition.class, VanillaPlugin::getTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTooltip(IUtils utils, int pad, ILootCondition condition) {
        CanToolPerformActionAliCondition cond = (CanToolPerformActionAliCondition) condition;
        return GenericTooltipUtils.getStringTooltip(pad, "ali.type.condition.can_tool_perform_action", cond.action);
    }
}
