package com.yanny.ali.plugin.mods.farmers_delight;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierAccessor;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@ClassAccessor("vectorwing.farmersdelight.common.loot.modifier.ReplaceItemModifier")
public class ReplaceItemModifier extends BaseAccessor<Object> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Item removedItem;
    @FieldAccessor
    private Item addedItem;
    @FieldAccessor
    private int addedCount;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ReplaceItemModifier(Object parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate predicate) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);

        return GlobalLootModifierUtils.getLootModifier(conditionList, (c) -> {
            Function<IDataNode, List<IDataNode>> factory = (src) -> {
                List<IDataNode> nodes = new ArrayList<>();
                IItemNode node = (IItemNode) src;
                List<LootItemCondition> allConditions = Stream.concat(c.stream(), node.getConditions().stream()).toList();
                Map<Enchantment, Map<Integer, RangeValue>> chance = NodeUtils.getEnchantedChance(utils, allConditions, 1);
                Map<Enchantment, Map<Integer, RangeValue>> count = NodeUtils.getEnchantedCount(utils, Collections.emptyList());
                ITooltipNode tooltip = EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, Collections.emptyList(), allConditions);

                if (!c.isEmpty()) {
                    nodes.add(new ModifiedNode(utils, src, new ItemNode(1, new RangeValue(addedCount), addedItem.getDefaultInstance(), tooltip, Collections.emptyList(), allConditions)));
                } else {
                    nodes.add(new ItemNode(1, new RangeValue(addedCount), addedItem.getDefaultInstance(), tooltip, Collections.emptyList(), allConditions));
                }

                return nodes;
            };
            return Collections.singletonList(new IOperation.ReplaceOperation((itemStack) -> itemStack.getItem().equals(removedItem), factory));
        }, predicate);
    }
}
