package com.yanny.ali.lootjs.node;

import com.mojang.datafixers.util.Either;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.LootJsPlugin;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EnchantedRanges;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
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

import java.util.Collections;
import java.util.List;

public class ItemStackNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LootJsPlugin.ID, "item_stack");

    private final TooltipNode tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final ItemStack itemStack;
    private final RangeValue count;
    private final float chance;
    private final boolean modified;

    public ItemStackNode(IServerUtils utils, ItemStack itemStack, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions, boolean preserveCount) {
       this(utils, itemStack, chance, false, functions, conditions, preserveCount);
    }

    public ItemStackNode(IServerUtils utils, ItemStack itemStack, float chance, boolean modified, List<LootItemFunction> functions, List<LootItemCondition> conditions, boolean preserveCount) {
        this.conditions = conditions;
        this.functions = functions;
        this.itemStack = TooltipUtils.getItemStack(utils, itemStack.copyWithCount(1), this.functions);
        this.chance = chance;
        this.modified = modified;

        if (preserveCount) {
            tooltip = getItemTooltip(utils, 1, chance, functions, conditions);
            count = getCount(utils, 1, functions).getUnenchantedValue();
        } else {
            tooltip = getItemTooltip(utils, itemStack.getCount(), chance, functions, conditions);
            count = getCount(utils, itemStack.getCount(), functions).getUnenchantedValue();
        }
    }

    public ItemStackNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        itemStack = ItemStack.STREAM_CODEC.decode(buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        count = new RangeValue(buf);
        modified = buf.readBoolean();
        chance = buf.readFloat();

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
    }

    public boolean isModified() {
        return modified;
    }

    @NotNull
    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() {
        return Either.left(itemStack);
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
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        ItemStack.STREAM_CODEC.encode(buf, itemStack);
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        count.encode(buf);
        buf.writeBoolean(modified);
        buf.writeFloat(chance);
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
    private static TooltipNode getItemTooltip(IServerUtils utils, int baseCount, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        EnchantedRanges chanceMap = NodeUtils.getEnchantedChance(utils, conditions, chance);
        EnchantedRanges countMap = getCount(utils, baseCount, functions);

        return EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chanceMap, countMap, functions, conditions).build();
    }

    @NotNull
    public static EnchantedRanges getCount(IServerUtils utils, int baseCount, List<LootItemFunction> functions) {
        EnchantedRanges count = new EnchantedRanges(baseCount);

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        return count;
    }
}
