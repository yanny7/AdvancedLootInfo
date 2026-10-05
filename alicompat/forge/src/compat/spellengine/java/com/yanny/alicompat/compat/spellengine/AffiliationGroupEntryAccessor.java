package com.yanny.alicompat.compat.spellengine;

import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.GroupNode;
import com.yanny.ali.plugin.server.TooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryChildren;
import com.yanny.alicompat.accessor.IEntryTooltip;
import com.yanny.alicompat.accessor.IEntryWeight;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.spell_engine.rpg_series.loot.AffiliationGroupEntry;
import net.spell_engine.rpg_series.loot.LootConfig;

import java.util.List;

public class AffiliationGroupEntryAccessor extends BaseAccessor<AffiliationGroupEntry> implements IEntry, IEntryTooltip, IEntryWeight, IEntryChildren {
    @FieldAccessor
    private float extraWeight;

    @FieldAccessor
    private LootConfig.Behavior.WeightOperation operation;

    @FieldAccessor
    private boolean includeTeam;

    public AffiliationGroupEntryAccessor(AffiliationGroupEntry parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, NumberExpr chance, NumberExpr sumWeight, List<TooltipNode> chanceConditions, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = NodeUtils.getAllConditions(parent, conditions);
        LootPoolEntryContainer[] children = parent.children().toArray(LootPoolEntryContainer[]::new);
        TooltipNode tooltip = TooltipBuilder.array((b) -> {
            b.add(TooltipUtils.getGroupTooltip());
            addAffiliation(b, utils);
        }).build();

        return new GroupNode(NodeUtils.getChildren(utils, children, chance, sumWeight, chanceConditions, functions, allConditions), tooltip);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return TooltipBuilder.array((b) -> {
            b.add(utils.getValueTooltip(utils, parent.children()).build(Lang.Branch.ENTRIES));
            addAffiliation(b, utils);
            b.add(utils.getValueTooltip(utils, parent.conditions).build(Lang.Branch.PREDICATES));
        }, SpellEngineLang.Entry.AFFILIATION_GROUP);
    }

    @Override
    public NumberExpr getEntryWeight(IServerUtils utils, List<TooltipNode> conditions) {
        return NodeUtils.getTotalWeight(utils, parent.children(), conditions);
    }

    @Override
    public List<LootPoolEntryContainer> getEntryChildren(IServerUtils utils) {
        return parent.children();
    }

    private void addAffiliation(TooltipBuilder builder, IServerUtils utils) {
        builder.add(utils.getValueTooltip(utils, extraWeight).build(SpellEngineLang.Value.EXTRA_WEIGHT));
        builder.add(utils.getValueTooltip(utils, operation).build(Lang.Value.OPERATION));
        builder.add(utils.getValueTooltip(utils, includeTeam).build(SpellEngineLang.Value.INCLUDE_TEAM));
    }
}
