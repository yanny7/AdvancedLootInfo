package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

public class ItemNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "item");
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ITooltipNode tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final Either<ItemStack, TagKey<? extends ItemLike>> item;
    private final RangeValue count;
    private final float chance;

    public ItemNode(float chance, RangeValue count, ItemStack item, ITooltipNode tooltip, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this(chance, count, Either.left(item), tooltip, functions, conditions);
    }

    public ItemNode(float chance, RangeValue count, TagKey<? extends ItemLike> tag, ITooltipNode tooltip, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this(chance, count, Either.right(tag), tooltip, functions, conditions);
    }

    public ItemNode(float chance, RangeValue count, Either<ItemStack, TagKey<? extends ItemLike>> item, ITooltipNode tooltip, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.chance = chance;
        this.count = count;
        this.item = item;
        this.tooltip = tooltip;
        this.functions = functions;
        this.conditions = conditions;
    }

    public ItemNode(IClientUtils utils, FriendlyByteBuf buf) {
        item = buf.readEither(FriendlyByteBuf::readItem, (b) -> TagKey.create(Registries.ITEM, b.readResourceLocation()));
        tooltip = ITooltipNode.decodeNode(utils, buf);
        count = new RangeValue(buf);
        chance = buf.readFloat();

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
    }

    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() {
        return item;
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
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        int index = buf.writerIndex();

        try {
            buf.writeEither(item, FriendlyByteBuf::writeItem, (b, t) -> b.writeResourceLocation(t.location()));
        } catch (Throwable e) {
            buf.writerIndex(index);
            buf.writeEither(Either.left(ItemStack.EMPTY), FriendlyByteBuf::writeItem, (b, t) -> {});
            LOGGER.warn("Failed to encode {}/{}", BuiltInRegistries.ITEM.getKey(item.left().map(ItemStack::getItem).orElse(Items.AIR)), item.right().orElse(null), e);
        }

        ITooltipNode.encodeNode(utils, tooltip, buf);
        count.encode(buf);
        buf.writeFloat(chance);
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
