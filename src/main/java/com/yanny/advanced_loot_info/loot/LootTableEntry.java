package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootEntry;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.mixin.MixinLootTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class LootTableEntry implements ILootEntry {
    public final List<LootPoolEntry> pools;
    public final List<ILootFunction> functions;

    public LootTableEntry(IContext context, LootTable table) {
        pools = ((MixinLootTable) table).getPools().stream().map((pool) -> new LootPoolEntry(context, pool)).toList();
        functions = context.registry().convertFunctions(context, ((MixinLootTable) table).getFunctions());
    }

    public LootTableEntry(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();

        pools = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            pools.add(new LootPoolEntry(context, buf));
        }

        functions = context.registry().decodeFunctions(context, buf);
    }

    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeInt(pools.size());

        for (LootPoolEntry pool : pools) {
            pool.encode(context, buf);
        }

        context.registry().encodeFunctions(context, buf, functions);
    }

    @NotNull
    @Override
    public List<Item> collectItems() {
        List<Item> items = new LinkedList<>();

        for (LootPoolEntry pool : pools) {
            items.addAll(pool.collectItems());
        }

        return items;
    }
}
