package com.yanny.ali.plugin.mods.mantle.conditions;

import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.TooltipNode;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IConditionTooltip;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.*;

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
        ITooltipNode tooltip = new TooltipNode(translatable("ali.type.condition.block_tag"));

        tooltip.add(getTagKeyTooltip(utils, "ali.property.value.tag", tag));
        tooltip.add(getStatePropertiesPredicateTooltip(utils, "ali.property.branch.properties", properties));

        return tooltip;
    }
}
