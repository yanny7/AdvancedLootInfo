package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.CommonLogUtils;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.common.NodeUtils;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ItemNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = Utils.modLoc("item");
    private static final Logger LOGGER = CommonLogUtils.getLogger(Utils.MOD_ID);

    private final TooltipNode tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final Either<ItemStack, TagKey<? extends ItemLike>> item;
    private final List<ItemStack> items;
    private final RangeValue count;
    private final float chance;
    /** Only populated on the client - on the server it is derived from {@link #conditions} in {@link #encode}. */
    private final boolean hasPredicates;

    public ItemNode(float chance, RangeValue count, ItemStack item, TooltipNode tooltip, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this(chance, count, Either.left(item), tooltip, functions, conditions);
    }

    public ItemNode(float chance, RangeValue count, TagKey<? extends ItemLike> tag, TooltipNode tooltip, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this(chance, count, Either.right(tag), tooltip, functions, conditions);
    }

    public ItemNode(float chance, RangeValue count, Either<ItemStack, TagKey<? extends ItemLike>> item, TooltipNode tooltip, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.chance = chance;
        this.count = count.clamp(0, 9999);
        this.item = item;
        this.items = NodeUtils.resolveItems(item);
        this.tooltip = tooltip;
        this.functions = functions;
        this.conditions = conditions;
        this.hasPredicates = false;
    }

    public ItemNode(IClientUtils utils, FriendlyByteBuf buf) {
        item = buf.readEither(FriendlyByteBuf::readItem, (b) -> TagKey.create(Registries.ITEM, b.readResourceLocation()));
        items = NodeUtils.resolveItems(item);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        count = new RangeValue(buf);
        chance = buf.readFloat();
        hasPredicates = buf.readBoolean();

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
    }

    @NotNull
    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getItem() {
        return item;
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
    public RangeValue getCount() {
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
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        int index = buf.writerIndex();

        try {
            buf.writeEither(item, FriendlyByteBuf::writeItem, (b, t) -> b.writeResourceLocation(t.location()));
        } catch (Throwable e) {
            buf.writerIndex(index);
            buf.writeEither(Either.left(ItemStack.EMPTY), FriendlyByteBuf::writeItem, (b, t) -> {});
            LOGGER.warn("Failed to encode {}/{}", BuiltInRegistries.ITEM.getKey(item.left().map(ItemStack::getItem).orElse(Items.AIR)), item.right().orElse(null), e);
        }

        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        count.encode(buf);
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
}
