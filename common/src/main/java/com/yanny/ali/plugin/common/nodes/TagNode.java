package com.yanny.ali.plugin.common.nodes;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.Utils;
import com.yanny.ali.api.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import com.yanny.ali.plugin.server.TooltipUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TagNode implements IDataNode, IItemNode {
    public static final ResourceLocation ID = new ResourceLocation(Utils.MOD_ID, "tag");

    private final List<ITooltipNode> tooltip;
    private final List<LootItemCondition> conditions;
    private final List<LootItemFunction> functions;
    private final TagKey<Item> tag;
    private final RangeValue count;
    private final float chance;

    public TagNode(IServerUtils utils, TagEntry entry, float chance, int sumWeight, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        this.conditions = Stream.concat(conditions.stream(), Arrays.stream(entry.conditions)).toList();
        this.functions = Stream.concat(functions.stream(), Arrays.stream(entry.functions)).toList();
        this.chance = chance * entry.weight / sumWeight;
        tag = entry.tag;
        tooltip = EntryTooltipUtils.getSingletonTooltip(utils, entry, chance, sumWeight, functions, conditions);
        count = TooltipUtils.getCount(utils, this.functions).get(null).get(0);
    }

    public TagNode(IClientUtils utils, FriendlyByteBuf buf) {
        tag = TagKey.create(Registries.ITEM, buf.readResourceLocation());
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        count = new RangeValue(buf);

        conditions = Collections.emptyList();
        functions = Collections.emptyList();
        chance = 1;
    }

    @Override
    public Either<ItemStack, TagKey<Item>> getModifiedItem() {
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
    public void encode(IServerUtils utils, FriendlyByteBuf buf) {
        buf.writeResourceLocation(tag.location());
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

//    @Override
//    public List<Item> getItems() {
//        return BuiltInRegistries.ITEM.getTag(tag)
//                .map(holders -> holders.stream().map(Holder::value).toList())
//                .orElse(Collections.emptyList());
//    }
}
