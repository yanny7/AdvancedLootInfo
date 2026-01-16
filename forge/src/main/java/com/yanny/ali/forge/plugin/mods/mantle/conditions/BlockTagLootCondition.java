package com.yanny.ali.forge.plugin.mods.mantle.conditions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.tooltip.BranchTooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@ClassAccessor("slimeknights.mantle.loot.condition.BlockTagLootCondition")
public class BlockTagLootCondition extends BaseAccessor<LootItemCondition> implements IConditionTooltip {
    @FieldAccessor
    private TagKey<Block> tag;

    @FieldAccessor
    private StatePropertiesPredicate properties;

    public BlockTagLootCondition(LootItemCondition parent) {
        super(parent);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch()
                .add(utils.getValueTooltip(utils, tag).build("ali.property.value.tag"))
                .add(utils.getValueTooltip(utils, properties).build("ali.property.branch.properties"))
                .build("ali.type.condition.block_tag");
    }
}
