package com.yanny.alicompat.compat.aether;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AetherNodeUtils {
    @NotNull
    public static List<LootItemCondition> withChance(List<LootItemCondition> conditions, float chance) {
        List<LootItemCondition> result = new ArrayList<>(conditions);

        result.add(LootItemRandomChanceCondition.randomChance(chance).build());
        return result;
    }

    @NotNull
    public static List<IDataNode> countedNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src, NumberExpr count) {
        IItemNode node = (IItemNode) src;
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
        NumberExpr chance = NodeUtils.getChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, LootCount.of(count), NodeUtils.getCountLimit(node.getItem()), node.getFunctions(), allConditions);
        ItemNode replacement = new ItemNode(node.getChance(), count, node.getItem(), tooltip.build(), node.getFunctions(), allConditions);

        return List.of(new ModifiedNode(utils, src, replacement));
    }
}
