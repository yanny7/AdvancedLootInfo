package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinSetPotionFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public class SetPotionFunction extends LootConditionalFunction {
    public final Potion potion;

    public SetPotionFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        potion = ((MixinSetPotionFunction) function).getPotion();
    }

    public SetPotionFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        potion = ForgeRegistries.POTIONS.getValue(buf.readResourceLocation());;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(ForgeRegistries.POTIONS.getKey(potion));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.set_potion.name", potion.getName(""))));

        if (!potion.getEffects().isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_potion.effects")));

            potion.getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
        }

        return components;
    }
}
