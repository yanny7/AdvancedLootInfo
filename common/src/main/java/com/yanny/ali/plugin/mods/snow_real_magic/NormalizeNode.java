package com.yanny.ali.plugin.mods.snow_real_magic;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.GenericTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NormalizeNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("snowrealmagic", "item_stack");

    private final List<ITooltipNode> tooltip;
    private final float chance;

    public NormalizeNode(IServerUtils utils, float chance, int quality, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        tooltip = new ArrayList<>();
        tooltip.add(new TooltipNode(GenericTooltipUtils.translatable("ali.enum.group_type.normalize")));
        tooltip.addAll(getItemTooltip(utils, chance, quality, functions, conditions));
        this.chance = chance;
    }

    public NormalizeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        chance = 1;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
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
    private static List<ITooltipNode> getItemTooltip(IServerUtils utils, float chance, int quality, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap = TooltipUtils.getChance(utils, conditions, chance);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> countMap = getCount(utils, 1, functions);

        return EntryTooltipUtils.getTooltip(utils, quality, chanceMap, countMap, functions, conditions);
    }

    @NotNull
    public static Map<Holder<Enchantment>, Map<Integer, RangeValue>> getCount(IServerUtils utils, int baseCount, List<LootItemFunction> functions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> count = new LinkedHashMap<>();

        count.put(null, Map.of(0, new RangeValue(baseCount)));

        for (LootItemFunction function : functions) {
            utils.applyCountModifier(utils, function, count);
        }

        return count;
    }

    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() {
        return Either.left(ItemStack.EMPTY);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return List.of();
    }

    @Override
    public List<LootItemFunction> getFunctions() {
        return List.of();
    }

    @Override
    public RangeValue getCount() {
        return new RangeValue();
    }

    @Override
    public float getChance() {
        return chance;
    }
}
