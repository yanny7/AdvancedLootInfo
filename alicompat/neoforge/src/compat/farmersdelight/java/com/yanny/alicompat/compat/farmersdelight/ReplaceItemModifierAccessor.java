package com.yanny.alicompat.compat.farmersdelight;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import vectorwing.farmersdelight.common.loot.modifier.ReplaceItemModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReplaceItemModifierAccessor extends BaseAccessor<ReplaceItemModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Item removedItem;
    @FieldAccessor
    private Item addedItem;
    @FieldAccessor
    private int addedCount;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ReplaceItemModifierAccessor(ReplaceItemModifier parent) {
        super(parent);
    }

    public Optional<IPageLootModifier> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList, (page, c) -> {
            Function<IDataNode, List<IDataNode>> factory = (src) -> {
                List<IDataNode> nodes = new ArrayList<>();
                IItemNode node = (IItemNode) src;
                List<LootItemCondition> allConditions = Stream.concat(c.stream(), node.getConditions().stream()).toList();
                LootCount chance = NodeUtils.getChance(utils, allConditions, 1);
                LootCount count = LootCount.of(NumberExpr.constant(addedCount));
                TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, NodeUtils.getCountLimit(addedItem.getDefaultInstance()), Collections.emptyList(), allConditions);

                if (!c.isEmpty()) {
                    nodes.add(new ModifiedNode(utils, src, new ItemNode(1, NumberExpr.constant(addedCount), addedItem.getDefaultInstance(), tooltip.build(), Collections.emptyList(), allConditions)));
                } else {
                    nodes.add(new ItemNode(1, NumberExpr.constant(addedCount), addedItem.getDefaultInstance(), tooltip.build(), Collections.emptyList(), allConditions));
                }

                return nodes;
            };
            return Collections.singletonList(new IOperation.ReplaceOperation((itemStack) -> itemStack.getItem().equals(removedItem), factory));
        }));
    }
}
