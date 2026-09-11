package com.yanny.alicompat.accessor;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class GlmNodeUtils {
    @NotNull
    public static IDataNode addedNode(IServerUtils utils, List<LootItemCondition> conditions, ItemStack item, float rawChance, RangeValue count) {
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, conditions, rawChance);
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(count)), Collections.emptyList(), conditions);

        return new ItemNode(rawChance, new RangeValue(count), item, tooltip.build(), Collections.emptyList(), conditions);
    }

    @NotNull
    public static IDataNode referenceNode(IServerUtils utils, List<LootItemCondition> conditions, ResourceLocation lootTable) {
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions));
        }).build();

        return NodeUtils.getReferenceNode(utils, lootTable, conditions, tooltip);
    }

    @NotNull
    public static List<IDataNode> replacedNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src, ItemStack item, RangeValue count) {
        IItemNode node = (IItemNode) src;
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(count)), node.getFunctions(), allConditions);
        ItemNode replacement = new ItemNode(node.getChance(), count, item, tooltip.build(), node.getFunctions(), allConditions);

        return List.of(new ModifiedNode(utils, src, replacement));
    }

    @Nullable
    public static IDataNode keptNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        if (conditions.isEmpty()) {
            return null;
        }

        if (!(src instanceof ItemNode node)) {
            return src;
        }

        List<LootItemCondition> allConditions = new ArrayList<>(node.getConditions());

        allConditions.add(new InvertedLootItemCondition(new AllOfCondition(conditions)));

        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(node.getCount())), node.getFunctions(), allConditions);

        return new ItemNode(node.getChance(), new RangeValue(node.getCount()), node.getItem(), tooltip.build(), node.getFunctions(), allConditions);
    }
}
