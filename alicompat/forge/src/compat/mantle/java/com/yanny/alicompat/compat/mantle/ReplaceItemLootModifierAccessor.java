package com.yanny.alicompat.compat.mantle;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ReplaceItemLootModifierAccessor extends BaseAccessor<ReplaceItemLootModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    private Ingredient original;

    @FieldAccessor
    private ItemOutput replacement;

    @FieldAccessor
    private LootItemFunction[] functions;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ReplaceItemLootModifierAccessor(ReplaceItemLootModifier parent) {
        super(parent);
    }

    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        List<LootItemCondition> conditionList = Arrays.asList(conditions);
        List<LootItemFunction> functionList = Arrays.asList(functions);

        return GlobalLootModifierUtils.getLootModifier(utils, parent, conditionList, (c) -> {
            Function<IDataNode, List<IDataNode>> factory = (src) -> {
                IItemNode node = (IItemNode) src;
                List<LootItemCondition> allConditions = Stream.concat(c.stream(), node.getConditions().stream()).toList();
                EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
                EnchantedRanges count = NodeUtils.getEnchantedCount(utils, functionList);

                count.modifyAllEntries((value) -> value.multiply(replacement.getCount()).multiply(node.getCount()));

                TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, functionList, allConditions);
                ItemNode replaced = new ItemNode(node.getChance(), count.getUnenchantedValue(), replacement.copy(), tooltip.build(), functionList, allConditions);

                return List.of(new ModifiedNode(utils, src, replaced));
            };

            return Collections.singletonList(new IOperation.ReplaceOperation(original, factory));
        });
    }
}
