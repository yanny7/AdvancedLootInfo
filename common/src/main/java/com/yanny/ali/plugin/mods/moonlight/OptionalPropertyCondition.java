package com.yanny.ali.plugin.mods.moonlight;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import com.yanny.ali.plugin.server.RegistriesTooltipUtils;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getStatePropertiesPredicateTooltip;
import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

@ClassAccessor("net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition")
public class OptionalPropertyCondition extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    @FieldAccessor
    @Nullable
    private Block block;
    @FieldAccessor
    private StatePropertiesPredicate properties;

    public OptionalPropertyCondition(LootItemCondition parent) {
        super(parent);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        if (block != null) {
            ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.optional_property"));

            tooltip.add(RegistriesTooltipUtils.getBlockTooltip(utils, "ali.property.value.block", block));
            tooltip.add(getStatePropertiesPredicateTooltip(utils, "ali.property.branch.properties", properties));

            return tooltip;
        } else {
            return new TooltipNode();
        }
    }
}
