package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@ClassAccessor("vectorwing.farmersdelight.refabricated.CanItemPerformAbilityCondition")
public class CanItemPerformAbilityCondition extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    @FieldAccessor(clazz = ItemAbility.class)
    private ItemAbility ability;

    public CanItemPerformAbilityCondition(LootItemCondition condition) {
        super(condition);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch("ali.type.condition.can_item_perform_ability")
                .add(utils.getValueTooltip(utils, ability.toString()).key("ali.property.value.value"));
    }

    @ClassAccessor("vectorwing.farmersdelight.refabricated.ItemAbility")
    private static class ItemAbility extends BaseAccessor<Object> {
        public ItemAbility(Object parent) {
            super(parent);
        }

        @Override
        public String toString() {
            return ((Enum<?>) parent).name();
        }
    }
}
