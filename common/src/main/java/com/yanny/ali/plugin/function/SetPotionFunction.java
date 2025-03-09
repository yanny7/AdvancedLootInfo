package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetPotionFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.*;

public class SetPotionFunction extends LootConditionalFunction {
    public final Holder<Potion> potion;

    public SetPotionFunction(IContext context, LootItemFunction function) {
        super(context, function);
        potion = ((MixinSetPotionFunction) function).getPotion();
    }

    public SetPotionFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        potion = BuiltInRegistries.POTION.getHolderOrThrow(buf.readResourceKey(Registries.POTION));
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceKey(potion.unwrap().orThrow());
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_potion")));
        components.add(pad(pad + 1, translatable("ali.property.function.set_potion.name", Potion.getName(Optional.of(potion), ""))));

        if (!potion.value().getEffects().isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.function.set_potion.effects")));
            potion.value().getEffects().forEach((effect) -> components.add(pad(pad + 2, value(translatable(effect.getDescriptionId())))));
        }

        return components;
    }
}
