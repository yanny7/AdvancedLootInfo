package com.yanny.ali.plugin.lootjs.node;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.lootjs.LootJsPlugin;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemTagNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LootJsPlugin.ID, "item_tag");

    private final List<ITooltipNode> tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final TagKey<? extends ItemLike> tag;
    private final RangeValue count;
    private final float chance;
    private final boolean modified;

    public ItemTagNode(IServerUtils utils, TagKey<? extends ItemLike> entry, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions, boolean preserveCount) {
        this(utils, entry, chance, false, functions, conditions, preserveCount);
    }

    public ItemTagNode(IServerUtils utils, TagKey<? extends ItemLike> entry, float chance, boolean modified, List<LootItemFunction> functions, List<LootItemCondition> conditions, boolean preserveCount) {
        this.conditions = conditions;
        this.functions = functions;
        this.tag = entry;
        this.chance = chance;
        this.modified = modified;

        if (preserveCount) {
            tooltip = getItemTooltip(utils, chance, functions, conditions);
            count = getCount(utils, 1, functions).get(null).get(0);
        } else {
            tooltip = getItemTooltip(utils, chance, Collections.emptyList(), Collections.emptyList());
            count = getCount(utils, 1, Collections.emptyList()).get(null).get(0);
        }
    }

    public ItemTagNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        tag = TagKey.create(Registries.ITEM, buf.readResourceLocation());
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        count = new RangeValue(buf);
        modified = buf.readBoolean();
        chance = buf.readFloat();

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
    }

    public boolean isModified() {
        return modified;
    }

    @Override
    public Either<ItemStack, TagKey<? extends ItemLike>> getModifiedItem() {
        return Either.right(tag);
    }

    @Override
    public List<LootItemCondition> getConditions() {
        return conditions;
    }

    @Override
    public List<LootItemFunction> getFunctions() {
        return functions;
    }

    @NotNull
    public RangeValue getCount() {
        return count;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(tag.location());
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
        count.encode(buf);
        buf.writeBoolean(modified);
        buf.writeFloat(chance);
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
    private static List<ITooltipNode> getItemTooltip(IServerUtils utils, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap = TooltipUtils.getChance(utils, conditions, chance);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> countMap = getCount(utils, 1, functions);

        return EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chanceMap, countMap, functions, conditions);
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
}
