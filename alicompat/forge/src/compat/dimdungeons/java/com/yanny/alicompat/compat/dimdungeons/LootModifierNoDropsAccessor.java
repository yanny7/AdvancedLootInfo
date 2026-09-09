package com.yanny.alicompat.compat.dimdungeons;

import com.catastrophe573.dimdungeons.DungeonConfig;
import com.catastrophe573.dimdungeons.utils.LootModifierNoDrops;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LootModifierNoDropsAccessor extends BaseAccessor<LootModifierNoDrops> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public LootModifierNoDropsAccessor(LootModifierNoDrops parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(this.conditions);
        Set<Block> blacklist = Set.copyOf(DungeonConfig.blockDropBlacklist);

        if (blacklist.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ILootModifier<Block>() {
            @Override
            public boolean predicate(Block value) {
                return blacklist.contains(value);
            }

            @NotNull
            @Override
            public List<IOperation> getOperations() {
                return List.of(new IOperation.RemoveOperation((itemStack) -> true, (src) -> keptNode(utils, conditionList, src)));
            }

            @NotNull
            @Override
            public IType<Block> getType() {
                return IType.BLOCK;
            }
        });
    }

    @Nullable
    private static IDataNode keptNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        if (conditions.isEmpty()) {
            return null;
        }

        if (!(src instanceof ItemNode node)) {
            return src;
        }

        List<LootItemCondition> allConditions = new ArrayList<>(node.getConditions());

        allConditions.add(new InvertedLootItemCondition(new AllOfCondition(conditions.toArray(LootItemCondition[]::new))));

        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(node.getCount())),
                node.getFunctions(), allConditions);

        return new ItemNode(node.getChance(), new RangeValue(node.getCount()), node.getItem(), tooltip.build(), node.getFunctions(), allConditions);
    }
}
