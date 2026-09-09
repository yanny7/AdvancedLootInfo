package com.yanny.alicompat.compat.artifacts;

import artifacts.forge.loot.RollLootTableModifier;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.resources.ResourceLocation;
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

public class RollLootTableModifierAccessor extends BaseAccessor<RollLootTableModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private ResourceLocation lootTable;
    @FieldAccessor
    private boolean replace;
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public RollLootTableModifierAccessor(RollLootTableModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> {
            List<IOperation> operations = new ArrayList<>();

            if (replace) {
                operations.add(new IOperation.RemoveOperation((itemStack) -> true, (src) -> keptNode(utils, c, src)));
            }

            operations.add(new IOperation.AddOperation((itemStack) -> true, injectedNode(utils, c)));
            return operations;
        });
    }

    @NotNull
    private IDataNode injectedNode(IServerUtils utils, List<LootItemCondition> conditions) {
        TooltipNode tooltip = TooltipBuilder.array((b) -> b
                        .add(TooltipBuilder.keyOnly(Lang.Group.ALL))
                        .add(GenericTooltipUtils.getConditionsSectionTooltip(utils, conditions))
                )
                .build();

        return NodeUtils.getReferenceNode(utils, lootTable, conditions, tooltip);
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
