package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetPotionFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

public class SetPotionAliFunction extends LootConditionalAliFunction {
    public final Potion potion;

    public SetPotionAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        potion = ((MixinSetPotionFunction) function).getPotion();
    }

    public SetPotionAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        potion = BuiltInRegistries.POTION.getOptional(buf.readResourceLocation()).orElse(Potions.EMPTY);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(BuiltInRegistries.POTION.getKey(potion));
    }
}
