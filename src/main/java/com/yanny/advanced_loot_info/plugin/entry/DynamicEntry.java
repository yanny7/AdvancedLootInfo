package com.yanny.advanced_loot_info.plugin.entry;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinDynamicLoot;
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
