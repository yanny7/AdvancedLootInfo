package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.*;
import com.yanny.advanced_loot_info.mixin.MixinLootPool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class LootPoolEntry implements ILootEntry {
    public final List<LootEntry> entries;
    public final List<ILootCondition> conditions;
    public final List<ILootFunction> functions;
    public final RangeValue rolls;
    public final RangeValue bonusRolls;

    public LootPoolEntry(IContext context, LootPool lootPool) {
        entries = context.registry().convertEntries(context, ((MixinLootPool) lootPool).getEntries());
        conditions = context.registry().convertConditions(context, ((MixinLootPool) lootPool).getConditions());
        functions = context.registry().convertFunctions(context, ((MixinLootPool) lootPool).getFunctions());
        rolls = context.registry().convertNumber(context, ((MixinLootPool) lootPool).getRolls());
        bonusRolls = context.registry().convertNumber(context, ((MixinLootPool) lootPool).getBonusRolls());
    }

    public LootPoolEntry(IContext context, FriendlyByteBuf buf) {
        entries = context.registry().decodeEntries(context, buf);
        conditions = context.registry().decodeConditions(context, buf);
        functions = context.registry().decodeFunctions(context, buf);
        rolls = new RangeValue(buf);
        bonusRolls = new RangeValue(buf);
    }

    public void encode(IContext context, FriendlyByteBuf buf) {
        context.registry().encodeEntries(context, buf, entries);
        context.registry().encodeConditions(context, buf, conditions);
        context.registry().encodeFunctions(context, buf, functions);
        rolls.encode(buf);
        bonusRolls.encode(buf);
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        List<Item> items = new LinkedList<>();

        for (LootEntry entry : entries) {
            items.addAll(entry.collectItems());
        }

        return items;
    }
}
