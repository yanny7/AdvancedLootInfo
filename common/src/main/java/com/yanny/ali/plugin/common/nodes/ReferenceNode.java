package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ReferenceNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "reference");

    private final ITooltipNode tooltip;
    private final float chance;

    public ReferenceNode(IServerUtils utils, LootTableReference entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();
        LootTable lootTable = utils.getLootTable(entry.name);

        if (lootTable != null) {
            addChildren(new LootTableNode(utils, lootTable, chance * entry.weight / sumWeight, allFunctions, allConditions));
        } else {
            addChildren(new MissingNode());
        }

        this.chance = chance * entry.weight / sumWeight;
        tooltip = EntryTooltipUtils.getReferenceTooltip(entry, chance, sumWeight);
    }

    public ReferenceNode(IServerUtils utils, ResourceLocation table, List<LootItemCondition> conditions, ITooltipNode tooltip) {
        Map<Enchantment, Map<Integer, RangeValue>> chance = TooltipUtils.getChance(utils, conditions, 1);
        LootTable lootTable = utils.getLootTable(table);

        if (lootTable != null) {
            addChildren(new LootTableNode(utils, lootTable, 1, Collections.emptyList(), conditions));
        } else {
            addChildren(new MissingNode());
        }

        this.chance = chance.get(null).get(0).min();
        this.tooltip = tooltip;
    }

    public ReferenceNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
        buf.writeFloat(chance);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public float getChance() {
        return chance;
    }
}
