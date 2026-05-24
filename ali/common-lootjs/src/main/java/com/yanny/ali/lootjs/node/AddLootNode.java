package com.yanny.ali.lootjs.node;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.lootjs.LootJsPlugin;
import com.yanny.ali.lootjs.Utils;
import com.yanny.ali.lootjs.mixin.MixinAddLootAction;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddLootNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(LootJsPlugin.ID, "add_loot");

    private final TooltipNode tooltip;
    private final AddLootAction.AddType addType;

    public AddLootNode(IServerUtils utils, AddLootAction lootAction, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        MixinAddLootAction mixinAddLootAction = (MixinAddLootAction) lootAction;
        int sumWeight;

        if (mixinAddLootAction.getType() == AddLootAction.AddType.DEFAULT) {
            sumWeight = 0;

            for (LootEntry entry : mixinAddLootAction.getEntries()) {
                sumWeight += entry.getWeight();
            }
        } else {
            sumWeight = LootPoolSingletonContainer.DEFAULT_WEIGHT;
        }

        for (LootEntry entry : mixinAddLootAction.getEntries()) {
            addChildren(Utils.getEntry(utils, entry, sumWeight, functions, conditions, false));
        }

        addType = mixinAddLootAction.getType();
        tooltip = switch (mixinAddLootAction.getType()) {
            case DEFAULT -> EntryTooltipUtils.getGroupTooltip().build();
            case SEQUENCE -> EntryTooltipUtils.getSequentialTooltip().build();
            case ALTERNATIVES -> EntryTooltipUtils.getAlternativesTooltip().build();
        };
    }

    public AddLootNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = TooltipNode.CACHE.getNodeById(buf.readVarInt());
        addType = buf.readEnum(AddLootAction.AddType.class);
    }

    public AddLootAction.AddType getAddType() {
        return addType;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeVarInt(TooltipNode.CACHE.getNodeId(tooltip));
        buf.writeEnum(addType);
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
