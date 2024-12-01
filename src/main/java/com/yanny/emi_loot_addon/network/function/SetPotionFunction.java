package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetPotionFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class SetPotionFunction extends LootConditionalFunction {
    public final ResourceLocation potion;

    public SetPotionFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        potion = ForgeRegistries.POTIONS.getKey(((MixinSetPotionFunction) function).getPotion());
    }

    public SetPotionFunction(FunctionType type, @NotNull FriendlyByteBuf buf) {
        super(type, buf);
        potion = buf.readResourceLocation();
    }

    @Override
    public void encode(@NotNull FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(potion);
    }
}
