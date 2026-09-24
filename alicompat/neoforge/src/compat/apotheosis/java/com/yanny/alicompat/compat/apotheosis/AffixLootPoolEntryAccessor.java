package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryTooltip;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.entry.AffixLootPoolEntry;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Set;

public class AffixLootPoolEntryAccessor extends BaseAccessor<AffixLootPoolEntry> implements IEntry, IEntryTooltip {
    @FieldAccessor
    private Set<DynamicHolder<LootRarity>> rarities;

    @FieldAccessor
    private Set<DynamicHolder<AffixLootEntry>> entries;

    public AffixLootPoolEntryAccessor(AffixLootPoolEntry parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(parent, conditions);
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(parent, functions);
        float itemChance = NodeUtils.getChance(parent, chance, sumWeight);
        EnchantedRanges enchantedChance = NodeUtils.getEnchantedChance(utils, allConditions, itemChance);
        EnchantedRanges enchantedCount = NodeUtils.getEnchantedCount(utils, allFunctions);
        TooltipNode tooltip = TooltipUtils.getTooltip(utils, parent.quality, enchantedChance, enchantedCount, allFunctions, allConditions).build();
        ItemStack itemStack = TooltipUtils.getItemStack(utils, ApotheosisUtils.firstEntryStack(entries), allFunctions);

        return new ItemNode(itemChance, enchantedCount.getUnenchantedValue(), itemStack, tooltip, allFunctions, allConditions);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarities).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, entries).build(Lang.Branch.ENTRIES));
            b.add(TooltipUtils.getWeightTooltip(parent.weight));
            b.add(TooltipUtils.getQualityTooltip(parent.quality));
            b.add(utils.getValueTooltip(utils, parent.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, parent.functions).build(Lang.Branch.MODIFIERS));
        }, ApotheosisLang.Entry.RANDOM_AFFIX_ITEM);
    }
}
