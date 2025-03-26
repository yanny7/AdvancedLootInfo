package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetOminousBottleAmplifierFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

public class SetOminousBottleAmplifierAliFunction extends LootConditionalAliFunction {
    public final RangeValue amplifierGenerator;

    public SetOminousBottleAmplifierAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        amplifierGenerator = context.utils().convertNumber(context, ((MixinSetOminousBottleAmplifierFunction) function).getAmplifierGenerator());
    }

    public SetOminousBottleAmplifierAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        amplifierGenerator = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        amplifierGenerator.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();
/* FIXME
        components.add(TooltipUtils.pad(pad, TooltipUtils.translatable("ali.type.function.set_ominous_bottle_amplifier", amplifierGenerator)));
*/
        return components;
    }
}
