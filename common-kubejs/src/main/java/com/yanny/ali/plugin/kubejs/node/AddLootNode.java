package com.yanny.ali.plugin.kubejs.node;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.AddLootAction;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.ITooltipNode;
import com.yanny.ali.api.ListNode;
import com.yanny.ali.mixin.MixinAddLootAction;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.kubejs.KubeJsPlugin;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class AddLootNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(KubeJsPlugin.ID, "add_loot");

    private final List<ITooltipNode> tooltip;
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
            addChildren(new LootEntryNode(utils, entry, sumWeight, functions, conditions));
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
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        addType = buf.readEnum(AddLootAction.AddType.class);
    }

    public AddLootAction.AddType getAddType() {
        return addType;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
        buf.writeEnum(addType);
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
