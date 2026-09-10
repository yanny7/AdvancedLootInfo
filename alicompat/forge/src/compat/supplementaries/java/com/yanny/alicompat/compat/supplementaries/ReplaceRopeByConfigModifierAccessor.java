package com.yanny.alicompat.compat.supplementaries;

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
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.forge.ReplaceRopeByConfigModifier;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ReplaceRopeByConfigModifierAccessor extends BaseAccessor<ReplaceRopeByConfigModifier> implements IGlobalLootModifierAccessor {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public ReplaceRopeByConfigModifierAccessor(ReplaceRopeByConfigModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(conditions), (c) -> getReplaceOperations(utils, c));
    }

    @NotNull
    private static List<IOperation> getReplaceOperations(IServerUtils utils, List<LootItemCondition> conditions) {
        if (CommonConfigs.Functional.ROPE_REPLACE_LOOT_TABLES.get() != CommonConfigs.ReplaceTableMode.REPLACE) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.ReplaceOperation(
                ReplaceRopeByConfigModifierAccessor::isForeignRope,
                (src) -> Collections.singletonList(getReplacedNode(utils, conditions, src))
        ));
    }

    private static boolean isForeignRope(ItemStack stack) {
        return stack.is(ModTags.ROPES) && !stack.is(ModRegistry.ROPE_ITEM.get());
    }

    @NotNull
    private static IDataNode getReplacedNode(IServerUtils utils, List<LootItemCondition> conditions, IDataNode src) {
        if (!(src instanceof IItemNode node)) {
            return src;
        }

        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), node.getConditions().stream()).toList();
        EnchantedRanges chance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        EnchantedRanges count = new EnchantedRanges(node.getCount());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chance, count, Collections.emptyList(), allConditions);
        ItemNode replacement = new ItemNode(1, node.getCount(), ModRegistry.ROPE_ITEM.get().getDefaultInstance(), tooltip.build(), Collections.emptyList(), allConditions);

        return new ModifiedNode(utils, src, replacement);
    }
}
