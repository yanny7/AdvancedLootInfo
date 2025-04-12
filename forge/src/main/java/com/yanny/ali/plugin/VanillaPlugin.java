package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.CanToolPerformAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@AliEntrypoint
public class VanillaPlugin extends Plugin {
    @Override
    public void registerClient(IClientRegistry registry) {
        super.registerClient(registry);
        registry.registerConditionTooltip(CanToolPerformAction.TYPE, VanillaPlugin::getTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTooltip(IClientUtils utils, int pad, LootItemCondition condition) {
        CanToolPerformAction cond = (CanToolPerformAction) condition;
        return GenericTooltipUtils.getStringTooltip(utils, pad, "ali.type.condition.can_tool_perform_action", cond.action.name());
    }
}
