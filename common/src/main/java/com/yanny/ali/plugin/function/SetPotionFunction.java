package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetPotionFunction;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

public class SetPotionFunction extends LootConditionalFunction {
    public final Potion potion;

    public SetPotionFunction(IContext context, LootItemFunction function) {
        super(context, function);
        potion = ((MixinSetPotionFunction) function).getPotion();
    }

    public SetPotionFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        potion = BuiltInRegistries.POTION.get(buf.readResourceLocation());;
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(BuiltInRegistries.POTION.getKey(potion));
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.pad(pad, TooltipUtils.translatable("ali.type.function.set_potion")));
        components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.function.set_potion.name", potion.getName(""))));

        if (!potion.getEffects().isEmpty()) {
            components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.function.set_potion.effects")));

            potion.getEffects().forEach((effect) -> components.add(TooltipUtils.pad(pad + 2, TooltipUtils.value(TooltipUtils.translatable(effect.getDescriptionId())))));
        }

        return components;
    }
}
