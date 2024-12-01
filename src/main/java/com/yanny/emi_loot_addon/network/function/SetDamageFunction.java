package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetItemDamageFunction;
import com.yanny.emi_loot_addon.network.LootUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import org.jetbrains.annotations.NotNull;

public class SetDamageFunction extends LootConditionalFunction {
    public final float[] damage;
    public final boolean add;

    public SetDamageFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        damage = LootUtils.getFloat(lootContext, ((MixinSetItemDamageFunction) function).getDamage());
        add = ((MixinSetItemDamageFunction) function).getAdd();
    }

    public SetDamageFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        damage = new float[]{buf.readFloat(), buf.readFloat()};
        add = buf.readBoolean();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeFloat(damage[0]);
        buf.writeFloat(damage[1]);
        buf.writeBoolean(add);
    }
}
