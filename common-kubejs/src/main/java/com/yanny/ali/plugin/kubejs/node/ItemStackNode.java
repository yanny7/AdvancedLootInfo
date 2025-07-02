package com.yanny.ali.plugin.kubejs.node;

import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.kubejs.KubeJsPlugin;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemStackNode extends ListNode implements ISlotNode {
    public static final ResourceLocation ID = new ResourceLocation(KubeJsPlugin.ID, "item_stack");

    private final List<ITooltipNode> tooltip;
    private final ItemStack itemStack;
    private final RangeValue count;

    public ItemStackNode(IServerUtils utils, ItemStack itemStack, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.itemStack = itemStack.copyWithCount(1);
        tooltip = getItemTooltip(utils, itemStack.getCount(), chance, functions, conditions);
        count = getCount(utils, itemStack.getCount(), functions).get(null).get(0);
    }

    public ItemStackNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        itemStack = buf.readItem();
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        count = new RangeValue(buf);
    }

    public ItemStack getModifiedItem() {
        return itemStack;
    }

    @Override
    public RangeValue getCount() {
        return count;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeItem(itemStack);
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
        count.encode(buf);
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
    private static List<ITooltipNode> getItemTooltip(IServerUtils utils, int baseCount, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        Map<Enchantment, Map<Integer, RangeValue>> chanceMap = TooltipUtils.getChance(utils, conditions, chance);
        Map<Enchantment, Map<Integer, RangeValue>> countMap = getCount(utils, baseCount, functions);

        return EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chanceMap, countMap, functions, conditions);
    }

    @NotNull
    public static Map<Enchantment, Map<Integer, RangeValue>> getCount(IServerUtils utils, int baseCount, List<LootItemFunction> functions) {
        Map<Enchantment, Map<Integer, RangeValue>> count = new LinkedHashMap<>();

        count.put(null, Map.of(0, new RangeValue(baseCount)));

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        return count;
    }
}
