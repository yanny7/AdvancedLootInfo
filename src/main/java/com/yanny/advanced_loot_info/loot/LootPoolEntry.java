package com.yanny.advanced_loot_info.loot;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.api.RangeValue;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public final class LootPoolEntry extends LootGroup {
    public final RangeValue rolls;
    public final RangeValue bonusRolls;

    public LootPoolEntry(List<LootEntry> entries, RangeValue rolls, RangeValue bonusRolls, List<ILootFunction> functions, List<ILootCondition> conditions) {
        super(GroupType.RANDOM, entries, functions, conditions, Float.NaN, 0);
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
    }

    public LootPoolEntry(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        rolls = new RangeValue(buf);
        bonusRolls = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        rolls.encode(buf);
        bonusRolls.encode(buf);
    }

    @Override
    public EntryType getType() {
        return EntryType.POOL;
    }
}
