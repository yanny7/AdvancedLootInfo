package com.yanny.ali.fabric.plugin.mods.farmers_delight;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@ClassAccessor("vectorwing.farmersdelight.refabricated.CanItemPerformAbilityCondition")
public class CanItemPerformAbilityCondition extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    @FieldAccessor
    private Enum<?> ability;

    public CanItemPerformAbilityCondition(LootItemCondition parent) {
        super(parent);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return utils.getValueTooltip(utils, ability).build("ali.type.condition.can_item_perform_ability");
    }
}
