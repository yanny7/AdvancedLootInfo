package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getStringTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@ClassAccessor("vectorwing.farmersdelight.refabricated.CanItemPerformAbilityCondition")
public class CanItemPerformAbilityCondition extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    @FieldAccessor(clazz = ItemAbility.class)
    private ItemAbility ability;

    public CanItemPerformAbilityCondition(LootItemCondition condition) {
        super(condition);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.can_item_perform_ability"));

        tooltip.add(getStringTooltip(utils, "ali.property.value.value", ability.toString()));

        return tooltip;
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
