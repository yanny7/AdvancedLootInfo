package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.mixin.MixinSetPotionFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class SetPotionFunction extends LootConditionalFunction {
    public final Potion potion;

    public SetPotionFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        potion = ((MixinSetPotionFunction) function).getPotion();
    }

    public SetPotionFunction(FriendlyByteBuf buf) {
        super(buf);
        potion = ForgeRegistries.POTIONS.getValue(buf.readResourceLocation());;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(ForgeRegistries.POTIONS.getKey(potion));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_potion")));
        components.add(pad(pad + 1, translatable("emi.property.function.set_potion.name", potion.getName(""))));

        if (!potion.getEffects().isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_potion.effects")));

            potion.getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
        }

        return components;
    }
}
