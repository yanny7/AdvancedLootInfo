package com.yanny.alicompat.compat.hybridaquatic;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.GlmNodeUtils;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import dev.hybridlabs.aquatic.loot.HAGlobalLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class HAGlobalLootModifierAccessor extends BaseAccessor<HAGlobalLootModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public HAGlobalLootModifierAccessor(HAGlobalLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> {
            List<IOperation> operations = new ArrayList<>();
            List<WeightedEntry.Wrapper<ResourceLocation>> tables = parent.getTables().unwrap();
            int totalWeight = Math.max(1, tables.stream().mapToInt((t) -> t.getWeight().asInt()).sum());
            float replaceChance = 1 - parent.getChance();

            operations.add(new IOperation.RemoveOperation(this::isReplaced, (src) -> keptNode(utils, c, src)));

            for (WeightedEntry.Wrapper<ResourceLocation> table : tables) {
                List<LootItemCondition> tableConditions = withChance(c, replaceChance * table.getWeight().asInt() / totalWeight);

                operations.add(new IOperation.AddOperation(this::isReplaced, GlmNodeUtils.referenceNode(utils, tableConditions, table.data())));
            }

            return operations;
        });
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table(parent.getTarget()::equals, true);
    }

    private boolean isReplaced(ItemStack stack) {
        return stack.is(parent.getItemTag());
    }

    @NotNull
    private IDataNode keptNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        if (!(src instanceof ItemNode node)) {
            return src;
        }

        List<LootItemCondition> allConditions = Stream.concat(node.getConditions().stream(), withChance(conditions, parent.getChance()).stream()).toList();
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, new EnchantedRanges(new RangeValue(node.getCount())), node.getFunctions(), allConditions);

        return new ItemNode(node.getChance(), new RangeValue(node.getCount()), node.getItem(), tooltip.build(), node.getFunctions(), allConditions);
    }

    @NotNull
    private static List<LootItemCondition> withChance(List<LootItemCondition> conditions, float chance) {
        return Stream.concat(conditions.stream(), Stream.of(LootItemRandomChanceCondition.randomChance(chance).build())).toList();
    }
}
