package com.yanny.ali.plugin.mods.moonlight;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.common.tooltip.EmptyTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

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
            return BranchTooltipNode.branch()
                    .add(utils.getValueTooltip(utils, block).build("ali.property.value.block"))
                    .add(utils.getValueTooltip(utils, properties).build("ali.property.branch.properties"))
                    .build("ali.type.condition.optional_property");
        } else {
            return EmptyTooltipNode.EMPTY;
        }
    }
}
