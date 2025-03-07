package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinLootPoolSingletonContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.List;

public abstract class SingletonEntry extends LootEntry {
    public final List<ILootFunction> functions;
    public final int weight;
    public final int quality;

    public SingletonEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        functions = context.utils().convertFunctions(context, ((MixinLootPoolSingletonContainer) entry).getFunctions());
        weight = ((MixinLootPoolSingletonContainer) entry).getWeight();
        quality = ((MixinLootPoolSingletonContainer) entry).getQuality();
    }

    public SingletonEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        functions = context.utils().decodeFunctions(context, buf);
        weight = buf.readInt();
        quality = buf.readInt();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        context.utils().encodeFunctions(context, buf, functions);
        buf.writeInt(weight);
        buf.writeInt(quality);
    }
}
