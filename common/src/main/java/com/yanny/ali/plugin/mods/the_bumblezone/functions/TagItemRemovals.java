package com.yanny.ali.plugin.mods.the_bumblezone.functions;

import com.yanny.ali.api.BranchTooltipNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.ConditionalFunction;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.IFunctionTooltip;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;

import java.util.Arrays;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.getSubConditionsTooltip;

@ClassAccessor("com.telepathicgrunt.the_bumblezone.loot.functions.TagItemRemovals")
public class TagItemRemovals extends ConditionalFunction implements IFunctionTooltip {
    @FieldAccessor
    private TagKey<Item> tagKey;

    public TagItemRemovals(LootItemConditionalFunction conditionalFunction) {
        super(conditionalFunction);
    }

    @Override
    public ITooltipNode getTooltip(IServerUtils utils) {
        return BranchTooltipNode.branch("ali.type.function.tag_item_removals")
                .add(utils.getValueTooltip(utils, tagKey).key("ali.property.value.tag"))
                .add(getSubConditionsTooltip(utils, Arrays.asList(predicates)));
    }
}
