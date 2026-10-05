package com.yanny.alicompat.compat.apotheosis;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.ItemNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryTooltip;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityClamp;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AffixLootPoolEntryAccessor extends BaseAccessor<AffixLootPoolEntry> implements IEntry, IEntryTooltip {
    @FieldAccessor
    private RarityClamp.Simple rarityLimit;

    @FieldAccessor
    private List<DynamicHolder<AffixLootEntry>> entries;

    public AffixLootPoolEntryAccessor(AffixLootPoolEntry parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, NumberExpr chance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(parent, conditions);
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(parent, functions);
        LootCount itemChance = NodeUtils.getChance(utils, parent, chance, sumWeight, chanceConditions);
        ItemStack itemStack = TooltipUtils.getItemStack(utils, ApotheosisUtils.firstEntryStack(entries), allFunctions);
        LootCount count = NodeUtils.getCount(utils, allFunctions);
        TooltipNode tooltip = TooltipUtils.getTooltip(utils, parent.quality, NodeUtils.getChance(utils, allConditions, itemChance), count, NodeUtils.getCountLimit(itemStack), allFunctions, allConditions).build();

        return new ItemNode(NodeUtils.toFloat(itemChance.value()), count.value(), itemStack, tooltip, allFunctions, allConditions);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, rarityLimit).build(ApotheosisLang.Branch.RARITY));
            b.add(utils.getValueTooltip(utils, entries).build(Lang.Branch.ENTRIES));
            b.add(TooltipUtils.getWeightTooltip(parent.weight));
            b.add(TooltipUtils.getQualityTooltip(parent.quality));
            b.add(utils.getValueTooltip(utils, parent.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, parent.functions).build(Lang.Branch.MODIFIERS));
        }, ApotheosisLang.Entry.RANDOM_AFFIX_ITEM);
    }
}
