package com.yanny.emi_loot_addon.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LootInfo extends LootEntry {
    public final ResourceLocation item;
    public final float chance;

    public LootInfo(@NotNull FriendlyByteBuf buf) {
        super(buf);
        item = buf.readResourceLocation();
        chance = buf.readFloat();
    }

    public LootInfo(ResourceLocation item, List<LootFunction> functions, List<LootCondition> conditions, float chance) {
        super(functions, conditions);
        this.item = item;
        this.chance = chance;
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(item);
        buf.writeFloat(chance);
    }

    @Override
    public Type getType() {
        return Type.INFO;
    }

    @Override
    public String toString() {
        return String.format("%s (%f)", item, chance);
    }
}
