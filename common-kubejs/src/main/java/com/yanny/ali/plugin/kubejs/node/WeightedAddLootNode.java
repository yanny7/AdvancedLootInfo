package com.yanny.ali.plugin.kubejs.node;

import com.almostreliable.lootjs.core.LootEntry;
import com.almostreliable.lootjs.loot.action.WeightedAddLootAction;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinWeightedAddLootAction;
import com.yanny.ali.mixin.MixinWeightedRandomList;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.kubejs.KubeJsPlugin;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

public class WeightedAddLootNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(KubeJsPlugin.ID, "weighted_add_loot");

    private final List<ITooltipNode> tooltip;

    public WeightedAddLootNode(IServerUtils utils, WeightedAddLootAction lootAction, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        MixinWeightedAddLootAction action = (MixinWeightedAddLootAction) lootAction;
        //noinspection unchecked
        MixinWeightedRandomList<WeightedEntry.Wrapper<LootEntry>> weightedList = (MixinWeightedRandomList<WeightedEntry.Wrapper<LootEntry>>) action.getWeightedRandomList();
        int sumWeight = weightedList.getTotalWeight();

        tooltip = getTooltip(utils, action);

        for (WeightedEntry.Wrapper<LootEntry> wrapper : weightedList.getItems()) {
            addChildren(new LootEntryNode(utils, wrapper.getData(), sumWeight, functions, conditions));
        }
    }

    public WeightedAddLootNode(IClientUtils utils, FriendlyByteBuf buf) {
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

    @NotNull
    public static List<ITooltipNode> getTooltip(IServerUtils utils, MixinWeightedAddLootAction action) {
        List<ITooltipNode> tooltip = new ArrayList<>();

        tooltip.add(new TooltipNode(translatable("ali.enum.group_type.random")));
        tooltip.add(EntryTooltipUtils.getRolls(utils.convertNumber(utils, action.getNumberProvider()), new RangeValue(0)));
        tooltip.add(new TooltipNode(translatable("ali.property.value.allow_duplicate_loot", action.getAllowDuplicateLoot())));

        return tooltip;
    }
}
