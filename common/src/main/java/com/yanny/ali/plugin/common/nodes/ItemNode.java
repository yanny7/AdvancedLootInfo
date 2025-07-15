package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ItemNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "item");

    private final List<ITooltipNode> tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final ItemStack itemStack;
    private final RangeValue count;
    private final float chance;

    public ItemNode(IServerUtils utils, LootItem entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.conditions = Stream.concat(conditions.stream(), entry.conditions.stream()).toList();
        this.functions = Stream.concat(functions.stream(), entry.functions.stream()).toList();
        this.chance = chance * entry.weight / sumWeight;
        itemStack = TooltipUtils.getItemStack(utils, entry.item, this.functions);
        tooltip = EntryTooltipUtils.getSingletonTooltip(utils, entry, chance, sumWeight, functions, conditions);
        count = TooltipUtils.getCount(utils, this.functions).get(null).get(0);
    }

    public ItemNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        itemStack = ItemStack.STREAM_CODEC.decode(buf);;
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        count = new RangeValue(buf);

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
        chance = 1;
    }

    @Override
    public Either<ItemStack, TagKey<Item>> getModifiedItem() {
        return Either.left(itemStack);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return conditions;
    }

    @Override
    public List<LootItemFunction> getFunctions() {
        return functions;
    }

    @Override
    public RangeValue getCount() {
        return count;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, itemStack);
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
}
