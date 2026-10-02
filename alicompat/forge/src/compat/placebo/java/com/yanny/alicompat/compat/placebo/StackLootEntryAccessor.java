package com.yanny.alicompat.compat.placebo;

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
import dev.shadowsoffire.placebo.loot.StackLootEntry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class StackLootEntryAccessor extends BaseAccessor<StackLootEntry> implements IEntry, IEntryTooltip {
    @FieldAccessor
    private ItemStack stack;

    @FieldAccessor
    private int min;

    @FieldAccessor
    private int max;

    public StackLootEntryAccessor(StackLootEntry parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(utils, parent, conditions);
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(utils, parent, functions);
        float itemChance = NodeUtils.getChance(parent, chance, sumWeight);
        ItemStack itemStack = TooltipUtils.getItemStack(utils, stack.copy(), allFunctions);
        LootCount count = NodeUtils.getCount(utils, NumberExpr.uniformInt(min, max), allFunctions);
        TooltipNode tooltip = TooltipUtils.getTooltip(utils, parent.quality, NodeUtils.getChance(utils, allConditions, itemChance), count, NodeUtils.getCountLimit(itemStack), allFunctions, allConditions).build();

        return new ItemNode(itemChance, count.value(), itemStack, tooltip, allFunctions, allConditions);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, stack).build(Lang.Branch.ITEM));
            b.add(utils.getValueTooltip(utils, NumberExpr.uniformInt(min, max)).build(Lang.Value.COUNT));
            b.add(TooltipUtils.getWeightTooltip(parent.weight));
            b.add(TooltipUtils.getQualityTooltip(parent.quality));
            b.add(utils.getValueTooltip(utils, parent.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, parent.functions).build(Lang.Branch.MODIFIERS));
        }, PlaceboLang.Entry.STACK);
    }
}
