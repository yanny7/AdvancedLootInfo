package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetItemDamageFunction;
import com.yanny.emi_loot_addon.network.RangeValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SetDamageFunction extends LootConditionalFunction {
    public final RangeValue damage;
    public final boolean add;

    public SetDamageFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        damage = RangeValue.of(lootContext, ((MixinSetItemDamageFunction) function).getDamage());
        add = ((MixinSetItemDamageFunction) function).getAdd();
    }

    public SetDamageFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        damage = new RangeValue(buf);
        add = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        damage.encode(buf);
        buf.writeBoolean(add);
    }
}
