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
import com.yanny.ali.plugin.server.EntryTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
    public static final Identifier ID = Identifier.fromNamespaceAndPath(LootJsPlugin.ID, "item_tag");

    private final TooltipNode tooltip;
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
        tag = TagKey.create(Registries.ITEM, buf.readIdentifier());
        tooltip = TooltipNode.CACHE.getNodeById(buf.readVarInt());
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
        return Either.right(tag);
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
    public RangeValue getCount() {
        return count;
    }

    @Override
    public float getChance() {
        return chance;
    }

    @Override
    public void encode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(tag.location());
        buf.writeVarInt(TooltipNode.CACHE.getNodeId(tooltip));
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
    public Identifier getId() {
        return ID;
    }

    @NotNull
    private static TooltipNode getItemTooltip(IServerUtils utils, float chance, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> chanceMap = NodeUtils.getEnchantedChance(utils, conditions, chance);
        Map<Holder<Enchantment>, Map<Integer, RangeValue>> countMap = getCount(utils, 1, functions);

        return EntryTooltipUtils.getTooltip(utils, LootPoolSingletonContainer.DEFAULT_QUALITY, chanceMap, countMap, functions, conditions).build();
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
