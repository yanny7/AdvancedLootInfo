package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.stream.Stream;

public class ReferenceNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "reference");

    private final List<ITooltipNode> tooltip;

    public ReferenceNode(IServerUtils utils, LootTableReference entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<LootItemFunction> allFunctions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
        List<LootItemCondition> allConditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();
        LootTable lootTable = utils.getLootTable(entry.name);

        if (lootTable != null) {
            addChildren(new LootTableNode(utils, lootTable, chance * entry.weight / sumWeight, allFunctions, allConditions));
        } else {
            addChildren(new MissingNode());
        }

        tooltip = EntryTooltipUtils.getReferenceTooltip(entry, chance, sumWeight);
    }

    public ReferenceNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
