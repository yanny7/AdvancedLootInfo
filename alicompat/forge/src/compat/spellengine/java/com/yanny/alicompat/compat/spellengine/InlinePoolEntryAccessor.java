package com.yanny.alicompat.compat.spellengine;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import com.yanny.ali.plugin.common.nodes.ReferenceNode;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryTooltip;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.spell_engine.rpg_series.loot.InlinePoolEntry;

import java.util.List;

public class InlinePoolEntryAccessor extends BaseAccessor<InlinePoolEntry> implements IEntry, IEntryTooltip {
    @FieldAccessor
    private LootPool pool;

    public InlinePoolEntryAccessor(InlinePoolEntry parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, NumberExpr chance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(parent, conditions);
        List<LootItemFunction> allFunctions = NodeUtils.getAllFunctions(parent, functions);
        LootCount poolChance = NodeUtils.getChance(utils, parent, chance, sumWeight, chanceConditions);
        IDataNode poolNode = NodeUtils.getLootPoolNode(utils, pool, poolChance.value(), poolChance.conditions(), allFunctions, allConditions);
        IDataNode tableNode = new LootTableNode(List.of(poolNode), TooltipUtils.getLootTableTooltip().build());
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipBuilder.keyOnly(Lang.Group.ALL));
            b.add(TooltipUtils.getQualityTooltip(parent.quality));
            b.add(TooltipUtils.getChanceTooltip(poolChance));
        }).build();

        return new ReferenceNode(List.of(tableNode), NodeUtils.toFloat(poolChance.value()), tooltip);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, pool.entries).build(Lang.Branch.ENTRIES));
            b.add(TooltipUtils.getWeightTooltip(parent.weight));
            b.add(TooltipUtils.getQualityTooltip(parent.quality));
            b.add(utils.getValueTooltip(utils, parent.conditions).build(Lang.Branch.PREDICATES));
            b.add(utils.getValueTooltip(utils, parent.functions).build(Lang.Branch.MODIFIERS));
        }, SpellEngineLang.Entry.INLINE_POOL);
    }
}
