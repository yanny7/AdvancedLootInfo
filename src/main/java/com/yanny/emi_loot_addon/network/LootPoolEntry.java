package com.yanny.emi_loot_addon.network;

import com.yanny.emi_loot_addon.network.value.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LootPoolEntry extends LootGroup {
    public final RangeValue rolls;
    public final RangeValue bonusRolls;

    public LootPoolEntry(List<LootEntry> entries, RangeValue rolls, RangeValue bonusRolls, List<LootFunction> functions, List<LootCondition> conditions) {
        super(entries, functions, conditions);
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
    }

    public LootPoolEntry(@NotNull FriendlyByteBuf buf) {
        super(buf);
        rolls = new RangeValue(buf);
        bonusRolls = new RangeValue(buf);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        rolls.encode(buf);
        bonusRolls.encode(buf);
    }

    @Override
    public Type getType() {
        return Type.POOL;
    }
}
