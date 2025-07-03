package com.yanny.ali.plugin.kubejs.node;

import com.almostreliable.lootjs.core.LootEntry;
import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinLootEntry;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.kubejs.KubeJsPlugin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yanny.ali.plugin.server.GenericTooltipUtils.translatable;

public class LootEntryNode extends ListNode {
    public static final ResourceLocation ID = new ResourceLocation(KubeJsPlugin.ID, "loot_entry");

    private final List<ITooltipNode> tooltip;
    private final boolean isRandom;

    public LootEntryNode(IServerUtils utils, LootEntry entry, int sumWeight, List<LootItemCondition> conditions) {
        List<LootItemCondition> allConditions = new ArrayList<>(conditions);
        MixinLootEntry mixinLootEntry = (MixinLootEntry) entry;

        allConditions.addAll(mixinLootEntry.getConditions());

        for (IDataNode node : getNodes(utils, mixinLootEntry.getWeight(), sumWeight, mixinLootEntry.getGenerator(), mixinLootEntry.getPostModifications(), allConditions)) {
            addChildren(node);
        }

        isRandom = mixinLootEntry.getGenerator() instanceof LootEntry.RandomIngredientGenerator;
        tooltip = getRandomTooltip();
    }

    public LootEntryNode(IClientUtils utils, FriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = NodeUtils.decodeTooltipNodes(utils, buf);
        isRandom = buf.readBoolean();
    }

    public boolean isRandom() {
        return isRandom;
    }

    @Override
    public void encodeNode(IServerUtils utils, FriendlyByteBuf buf) {
        NodeUtils.encodeTooltipNodes(utils, buf, tooltip);
        buf.writeBoolean(isRandom);
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
    private static List<IDataNode> getNodes(IServerUtils utils, int weight, int sumWeight, LootEntry.Generator generator, List<LootItemFunction> functions, List<LootItemCondition> conditions) {
        List<IDataNode> items = new ArrayList<>();

        if (generator instanceof LootEntry.ItemGenerator itemGenerator) {
            items.add(new ItemStackNode(utils, itemGenerator.item(), (float) weight / sumWeight, functions, conditions));
        } else if (generator instanceof LootEntry.VanillaWrappedLootEntry lootEntry) {
            LootPoolEntryContainer entry = lootEntry.entry();
            items.add(utils.getEntryFactory(utils, entry).create(Collections.emptyList(), utils, entry, 1, sumWeight, functions, conditions));
        } else if (generator instanceof LootEntry.RandomIngredientGenerator ingredientGenerator) {
            for (ItemStack item : ingredientGenerator.ingredient().getItems()) {
                items.add(new ItemStackNode(utils, item, (float) weight / sumWeight, functions, conditions));
            }
        }

        return items;
    }

    @Unmodifiable
    @NotNull
    public static List<ITooltipNode> getRandomTooltip() {
        return List.of(new TooltipNode(translatable("ali.enum.group_type.random")));
    }
}
