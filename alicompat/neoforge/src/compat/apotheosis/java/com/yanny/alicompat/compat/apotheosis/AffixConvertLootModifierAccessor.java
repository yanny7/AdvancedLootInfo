package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipContext;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.common.nodes.ModifiedNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixConvertLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AffixConvertLootModifierAccessor extends BaseAccessor<AffixConvertLootModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    private List<AffixConvertLootModifier.AffixConversionEntry> entries;

    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AffixConvertLootModifierAccessor(AffixConvertLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions), (c) -> getOperations(utils, c));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table((location) -> matching(location) != null, true);
    }

    @NotNull
    private List<IOperation> getOperations(IServerUtils utils, List<LootItemCondition> conditions) {
        AffixConvertLootModifier.AffixConversionEntry entry = matching(TooltipContext.get());

        if (entry == null || entry.chance() <= 0) {
            return Collections.emptyList();
        }

        return Collections.singletonList(new IOperation.ReplaceOperation(
                (itemStack) -> !LootCategory.forItem(itemStack).isNone(),
                (src) -> getNodes(utils, conditions, entry, src)));
    }

    @NotNull
    private static List<IDataNode> getNodes(IServerUtils utils, List<LootItemCondition> conditions, AffixConvertLootModifier.AffixConversionEntry entry, IDataNode src) {
        IItemNode node = (IItemNode) src;
        LootItemCondition chance = LootItemRandomChanceCondition.randomChance(entry.chance()).build();
        List<LootItemCondition> allConditions = Stream.concat(Stream.concat(conditions.stream(), node.getConditions().stream()), Stream.of(chance)).toList();
        EnchantedRanges enchantedChance = NodeUtils.getEnchantedChance(utils, allConditions, node.getChance());
        RangeValue count = new RangeValue(node.getCount());
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, enchantedChance, new EnchantedRanges(count), node.getFunctions(), allConditions);

        tooltip.add(utils.getValueTooltip(utils, entry.rarities()).build(ApotheosisLang.Branch.RARITY));

        ItemNode replacement = new ItemNode(node.getChance(), count, node.getItem(), tooltip.build(), node.getFunctions(), allConditions);

        return List.of(new ModifiedNode(utils, src, replacement));
    }

    @Nullable
    private AffixConvertLootModifier.AffixConversionEntry matching(@Nullable ResourceLocation location) {
        if (location == null) {
            return null;
        }

        return entries.stream().filter((e) -> e.pattern().matches(location)).findFirst().orElse(null);
    }
}
