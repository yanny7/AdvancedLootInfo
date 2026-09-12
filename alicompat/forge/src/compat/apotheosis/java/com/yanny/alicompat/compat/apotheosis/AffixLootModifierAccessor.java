package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.LootPoolNode;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.glm.Destination;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IDestination;
import com.yanny.alicompat.accessor.IGlobalLootModifierAccessor;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootModifier;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootRegistry;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AffixLootModifierAccessor extends BaseAccessor<AffixLootModifier> implements IGlobalLootModifierAccessor, IDestination {
    @FieldAccessor
    protected LootItemCondition[] conditions;

    public AffixLootModifierAccessor(AffixLootModifier parent) {
        super(parent);
    }

    @Override
    public Optional<ILootModifier<?>> getLootModifier(IServerUtils utils) {
        return GlobalLootModifierUtils.getLootModifier(utils, parent, Arrays.asList(this.conditions),
                (c) -> Collections.singletonList(new IOperation.AddOperation((itemStack) -> true, getNode(utils, c))));
    }

    @NotNull
    @Override
    public Destination getDestination(IServerUtils ignoredUtils) {
        return new Destination.Table((location) -> ApotheosisUtils.matches(AdventureConfig.AFFIX_ITEM_LOOT_RULES, location), true);
    }

    @NotNull
    private static IDataNode getNode(IServerUtils utils, List<LootItemCondition> conditions) {
        Collection<AffixLootEntry> entries = AffixLootRegistry.INSTANCE.getValues();
        float chance = ApotheosisUtils.chance(AdventureConfig.AFFIX_ITEM_LOOT_RULES);
        int sumWeight = entries.stream().mapToInt(AffixLootEntry::getWeight).sum();
        List<IDataNode> children = entries.stream().map((entry) -> getEntryNode(utils, conditions, entry, chance, sumWeight)).toList();
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.RANDOM));
            b.add(utils.getValueTooltip(utils, chance).build(Lang.Value.PROBABILITY));
            b.add(TooltipBuilder.keyOnly(ApotheosisLang.Conditions.REQUIRES_PLAYER));
            b.add(utils.getValueTooltip(utils, conditions).build(Lang.Branch.PREDICATES));
        }, ApotheosisLang.GlobalLootModifier.RANDOM_AFFIX_ITEM).build();

        return new LootPoolNode(children, tooltip);
    }

    @NotNull
    private static IDataNode getEntryNode(IServerUtils utils, List<LootItemCondition> conditions, AffixLootEntry entry, float chance, int sumWeight) {
        float itemChance = sumWeight > 0 ? chance * entry.getWeight() / sumWeight : chance;
        EnchantedRanges enchantedChance = NodeUtils.getEnchantedChance(utils, conditions, itemChance);
        TooltipBuilder tooltip = TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, enchantedChance, new EnchantedRanges(new RangeValue(1)), Collections.emptyList(), conditions);

        tooltip.add(utils.getValueTooltip(utils, entry.getWeight()).build(Lang.Value.WEIGHT));
        tooltip.add(utils.getValueTooltip(utils, entry.getQuality()).build(Lang.Description.QUALITY));
        tooltip.add(utils.getValueTooltip(utils, entry.getMinRarity()).build(ApotheosisLang.Value.MIN_RARITY));
        tooltip.add(utils.getValueTooltip(utils, entry.getMaxRarity()).build(ApotheosisLang.Value.MAX_RARITY));
        tooltip.add(utils.getValueTooltip(utils, entry.getDimensions()).build(Lang.Branch.DIMENSIONS));
        tooltip.add(utils.getValueTooltip(utils, entry.getStages()).build(ApotheosisLang.Branch.STAGES));

        return new ItemNode(itemChance, new RangeValue(1), entry.getStack(), tooltip.build(), Collections.emptyList(), conditions);
    }
}
