package com.yanny.ali.plugin.lootjs.node;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinWeightedAddLootAction;
import com.yanny.ali.mixin.MixinWeightedRandomList;
import com.yanny.ali.plugin.common.tooltip.ArrayTooltipNode;
import com.yanny.ali.plugin.common.tooltip.LiteralTooltipNode;
import com.yanny.ali.plugin.common.tooltip.ValueTooltipNode;
import com.yanny.ali.plugin.lootjs.LootJsPlugin;
import com.yanny.ali.plugin.lootjs.Utils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeightedAddLootNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(LootJsPlugin.ID, "weighted_add_loot");

    private final ITooltipNode tooltip;

    public WeightedAddLootNode(IServerUtils utils, WeightedAddLootAction lootAction, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        MixinWeightedAddLootAction action = (MixinWeightedAddLootAction) lootAction;
        //noinspection unchecked
        MixinWeightedRandomList<WeightedEntry.Wrapper<LootEntry>> weightedList = (MixinWeightedRandomList<WeightedEntry.Wrapper<LootEntry>>) action.getWeightedRandomList();
        int sumWeight = weightedList.getTotalWeight();

        tooltip = getTooltip(utils, action);

        for (WeightedEntry.Wrapper<LootEntry> wrapper : weightedList.getItems()) {
            addChildren(Utils.getEntry(utils, wrapper.getData(), sumWeight, functions, conditions, false));
        }
    }

    public WeightedAddLootNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = ITooltipNode.decodeNode(utils, buf);
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        ITooltipNode.encodeNode(utils, tooltip, buf);
    }

    @Override
    public ITooltipNode getTooltip() {
        return tooltip;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    public static ITooltipNode getTooltip(IServerUtils utils, MixinWeightedAddLootAction action) {
        return ArrayTooltipNode.array()
                .add(LiteralTooltipNode.translatable("ali.enum.group_type.random"))
                .add(EntryTooltipUtils.getRolls(utils.convertNumber(utils, action.getNumberProvider()), new RangeValue(0)))
                .add(ValueTooltipNode.value(action.getAllowDuplicateLoot()).build("ali.property.value.allow_duplicate_loot"))
                .build();
    }
}
