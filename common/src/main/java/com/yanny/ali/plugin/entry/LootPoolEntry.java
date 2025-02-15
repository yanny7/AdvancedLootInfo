package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.*;
import com.yanny.ali.mixin.MixinLootPool;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public final class LootPoolEntry implements ILootEntry {
    public final List<ILootEntry> entries;
    public final List<ILootCondition> conditions;
    public final List<ILootFunction> functions;
    public final RangeValue rolls;
    public final RangeValue bonusRolls;

    public LootPoolEntry(IContext context, LootPool lootPool) {
        entries = context.utils().convertEntries(context, ((MixinLootPool) lootPool).getEntries());
        conditions = context.utils().convertConditions(context, ((MixinLootPool) lootPool).getConditions());
        functions = context.utils().convertFunctions(context, ((MixinLootPool) lootPool).getFunctions());
        rolls = context.utils().convertNumber(context, ((MixinLootPool) lootPool).getRolls());
        bonusRolls = context.utils().convertNumber(context, ((MixinLootPool) lootPool).getBonusRolls());
    }

    public LootPoolEntry(IContext context, FriendlyByteBuf buf) {
        entries = context.utils().decodeEntries(context, buf);
        conditions = context.utils().decodeConditions(context, buf);
        functions = context.utils().decodeFunctions(context, buf);
        rolls = new RangeValue(buf);
        bonusRolls = new RangeValue(buf);
    }

    public void encode(IContext context, FriendlyByteBuf buf) {
        context.utils().encodeEntries(context, buf, entries);
        context.utils().encodeConditions(context, buf, conditions);
        context.utils().encodeFunctions(context, buf, functions);
        rolls.encode(buf);
        bonusRolls.encode(buf);
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        List<Item> items = new LinkedList<>();

        for (ILootEntry entry : entries) {
            items.addAll(entry.collectItems());
        }

        return items;
    }
}
