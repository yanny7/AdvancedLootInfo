package com.yanny.alicompat.compat.twilightforest;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import twilightforest.loot.modifiers.GiantToolGroupingModifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class GiantToolGroupingModifierAccessor extends BaseAccessor<GiantToolGroupingModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public GiantToolGroupingModifierAccessor(GiantToolGroupingModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils, ILootTableIdConditionPredicate ignoredPredicate) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);
        Map<Block, Item> conversions = Map.copyOf(GiantToolGroupingModifier.CONVERSIONS);

        if (conversions.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ILootModifier<Block>() {
            @Override
            public boolean predicate(Block value) {
                return conversions.containsKey(value);
            }

            @NotNull
            @Override
            public List<IOperation> getOperations() {
                return conversions.entrySet().stream()
                        .map((entry) -> (IOperation) new IOperation.ReplaceOperation(
                                (itemStack) -> itemStack.getItem().equals(entry.getKey().asItem()),
                                (src) -> groupNode(utils, conditionList, src, entry.getValue())))
                        .toList();
            }

            @NotNull
            @Override
            public IType<Block> getType() {
                return IType.BLOCK;
            }
        });
    }

    @NotNull
    private static List<IDataNode> groupNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src, Item giantItem) {
        IItemNode node = (IItemNode) src;
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
        RangeValue count = new RangeValue(1);
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(count)),
                node.getFunctions(), allConditions);
        ItemNode giantNode = new ItemNode(node.getChance(), count, giantItem.getDefaultInstance(), tooltip.build(), node.getFunctions(), allConditions);

        return List.of(new ModifiedNode(utils, src, giantNode));
    }
}
