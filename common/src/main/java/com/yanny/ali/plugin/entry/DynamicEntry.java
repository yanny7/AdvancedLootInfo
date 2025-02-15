package com.yanny.ali.plugin.entry;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinDynamicLoot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

public class DynamicEntry extends SingletonEntry {
    public final ResourceLocation name;

    public DynamicEntry(IContext context, LootPoolEntryContainer entry) {
        super(context, entry);
        name = ((MixinDynamicLoot) entry).getName();
    }

    public DynamicEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        name = buf.readResourceLocation();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(name);
    }
}
