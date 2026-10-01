package com.yanny.ali.lootjs.node;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.LootJsPlugin;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.LootCount;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ItemStackNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LootJsPlugin.ID, "item_stack");

    private final TooltipNode tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final ItemStack itemStack;
    private final List<ItemStack> items;
    private final NumberExpr count;
    private final float chance;
    private final boolean modified;
    /** Only populated on the client - on the server it is derived from {@link #conditions} in {@link #encode}. */
    private final boolean hasPredicates;

    public ItemStackNode(IServerUtils utils, ItemStack itemStack, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions, @Nullable NumberExpr preservedCount) {
       this(utils, itemStack, chance, false, functions, conditions, preservedCount);
    }

    public ItemStackNode(IServerUtils utils, ItemStack itemStack, float chance, boolean modified, List<LootItemFunction> functions, List<LootItemCondition> conditions, @Nullable NumberExpr preservedCount) {
        this.conditions = conditions;
        this.functions = functions;
        this.itemStack = TooltipUtils.getItemStack(utils, itemStack.copyWithCount(1), this.functions);
        this.items = NodeUtils.resolveItems(Either.left(this.itemStack));
        this.chance = chance;
        this.modified = modified;
        this.hasPredicates = false;

        LootCount lootCount;

        if (preservedCount != null) {
            lootCount = LootCount.of(preservedCount);
        } else {
            lootCount = NodeUtils.getCount(utils, NumberExpr.constant(itemStack.getCount()), functions);
        }

        tooltip = getItemTooltip(utils, lootCount, getItem(), chance, functions, conditions);
        count = lootCount.value();
    }

    public ItemStackNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        itemStack = ItemStack.STREAM_CODEC.decode(buf);
        items = NodeUtils.resolveItems(Either.left(itemStack));
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        count = NumberExpr.decode(buf);
        modified = buf.readBoolean();
        chance = buf.readFloat();
        hasPredicates = buf.readBoolean();

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
    }

    public boolean isModified() {
        return modified;
    }

    @NotNull
    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getItem() {
        return Either.left(itemStack);
    }

    @NotNull
    @Override
    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public void retainItems(Predicate<ItemStack> isVisible) {
        items.removeIf(Predicate.not(isVisible));
    }

    @NotNull
    @Override
    public List<LootItemCondition> getConditions() {
        return conditions;
    }

    @NotNull
    @Override
    public List<LootItemFunction> getFunctions() {
        return functions;
    }

    @NotNull
    @Override
    public NumberExpr getCount() {
        return count;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public boolean hasPredicates() {
        return hasPredicates;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, itemStack);
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        count.encode(buf);
        buf.writeBoolean(modified);
        buf.writeFloat(chance);
        buf.writeBoolean(NodeUtils.hasPredicates(utils, conditions));
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

    @NotNull
    private static TooltipNode getItemTooltip(IServerUtils utils, LootCount count, Either<ItemStack, TagKey<? extends ItemLike>> item, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        NumberExpr chanceExpr = NodeUtils.getChance(utils, conditions, chance);

        return TooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chanceExpr, count, NodeUtils.getCountLimit(item), functions, conditions).build();
    }
}
