package com.yanny.ali.plugin.common.nodes;

import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class EmptyNode implements IDataNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "empty");

    private final List<ITooltipNode> tooltip;
    private final float chance;

    public EmptyNode(IServerUtils utils, EmptyLootItem entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.chance = chance * entry.weight / sumWeight;
        tooltip = EntryTooltipUtils.getEmptyTooltip(utils, entry, chance, sumWeight, functions, conditions);
    }

    public EmptyNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        chance = buf.readFloat();
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
        buf.writeFloat(chance);
    }

    @Override
    public List<ITooltipNode> getTooltip() {
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
