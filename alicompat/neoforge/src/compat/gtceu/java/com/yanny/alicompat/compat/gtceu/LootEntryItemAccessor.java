package com.yanny.alicompat.compat.gtceu;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.IEntry;
import com.yanny.alicompat.accessor.IEntryTooltip;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

@ClassAccessor("com.gregtechceu.gtceu.data.loot.ChestGenHooks$GTLootEntryItem")
public class LootEntryItemAccessor extends BaseAccessor<LootItem> implements IEntry, IEntryTooltip {
    public LootEntryItemAccessor(LootItem parent) {
        super(parent);
    }

    @Override
    public IDataNode create(IServerUtils utils, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        return NodeUtils.getItemNode(utils, parent, chance, sumWeight, functions, conditions);
    }

    @Override
    public TooltipBuilder getTooltip(IServerUtils utils) {
        return EntryTooltipUtils.getItemTooltip(utils, parent);
    }
}
