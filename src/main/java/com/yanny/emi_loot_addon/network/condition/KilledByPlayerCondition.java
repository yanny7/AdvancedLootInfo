package com.yanny.emi_loot_addon.network.condition;

import com.yanny.emi_loot_addon.network.LootCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class KilledByPlayerCondition extends LootCondition {

    public KilledByPlayerCondition(LootContext lootContext, LootItemCondition condition) {
        super(ConditionType.of(condition.getType()));
    }

    public KilledByPlayerCondition(ConditionType type, @NotNull FriendlyByteBuf buf) {
        super(type);
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {

    }
}
