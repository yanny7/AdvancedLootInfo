package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootEntry;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinLootTable;
import com.yanny.ali.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class LootTableEntry implements ILootEntry {
    public final List<LootPoolEntry> pools;
    public final List<ILootFunction> functions;

    public LootTableEntry(IContext context, LootTable table) {
        List<LootPool> lootPool = Services.PLATFORM.getLootPools(table);
        pools = lootPool.stream().map((pool) -> new LootPoolEntry(context, pool)).toList();
        functions = context.utils().convertFunctions(context, ((MixinLootTable) table).getFunctions());
    }

    public LootTableEntry(IContext context, FriendlyByteBuf buf) {
        int count = buf.readInt();

        pools = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            pools.add(new LootPoolEntry(context, buf));
        }

        functions = context.utils().decodeFunctions(context, buf);
    }

    public void encode(IContext context, FriendlyByteBuf buf) {
        buf.writeInt(pools.size());

        for (LootPoolEntry pool : pools) {
            pool.encode(context, buf);
        }

        context.utils().encodeFunctions(context, buf, functions);
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
