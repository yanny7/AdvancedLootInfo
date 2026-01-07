package com.yanny.ali.lootjs.node;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.lootjs.mixin.MixinAddLootAction;
import com.yanny.ali.lootjs.LootJsPlugin;
import com.yanny.ali.lootjs.Utils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AddLootNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(LootJsPlugin.ID, "add_loot");

    private final ITooltipNode tooltip;
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
            case DEFAULT -> EntryTooltipUtils.getGroupTooltip();
            case SEQUENCE -> EntryTooltipUtils.getSequentialTooltip();
            case ALTERNATIVES -> EntryTooltipUtils.getAlternativesTooltip();
        };
    }

    public AddLootNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
        addType = buf.readEnum(AddLootAction.AddType.class);
    }

    public AddLootAction.AddType getAddType() {
        return addType;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
        buf.writeEnum(addType);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
