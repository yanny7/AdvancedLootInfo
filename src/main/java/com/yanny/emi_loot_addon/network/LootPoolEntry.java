package com.yanny.emi_loot_addon.network;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LootPoolEntry extends LootGroup {
    public final int[] rolls;
    public final float[] bonusRolls;

    public LootPoolEntry(List<LootEntry> entries, int[] rolls, float[] bonusRolls, List<LootFunction> functions, List<LootCondition> conditions) {
        super(entries, functions, conditions);
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
    }

    public LootPoolEntry(@NotNull FriendlyByteBuf buf) {
        super(buf);
        rolls = buf.readVarIntArray();
        bonusRolls = new float[]{buf.readFloat(), buf.readFloat()};
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarIntArray(rolls);
        buf.writeFloat(bonusRolls[0]);
        buf.writeFloat(bonusRolls[1]);
    }

    @Override
    public Type getType() {
        return Type.POOL;
    }
}
