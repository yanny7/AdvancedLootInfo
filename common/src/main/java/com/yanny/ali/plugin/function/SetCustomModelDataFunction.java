package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetCustomModelDataFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetCustomModelDataFunction extends LootConditionalFunction {
    public final RangeValue valueProvider;

    public SetCustomModelDataFunction(IContext context, LootItemFunction function) {
        super(context, function);
        valueProvider = context.utils().convertNumber(context, ((MixinSetCustomModelDataFunction) function).getValueProvider());
    }

    public SetCustomModelDataFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        valueProvider = new RangeValue(buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        valueProvider.encode(buf);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_custom_model_data", valueProvider)));

        return components;
    }
}
