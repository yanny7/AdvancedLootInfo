package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.ILootFunction;
import com.yanny.ali.mixin.MixinSequenceFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SequenceFunction extends LootConditionalFunction {
    public final List<ILootFunction> functions;

    public SequenceFunction(IContext context, LootItemFunction function) {
        super(context, function);
        functions = context.utils().convertFunctions(context, ((MixinSequenceFunction) function).getFunctions());
    }

    public SequenceFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        functions = context.utils().decodeFunctions(context, buf);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        context.utils().encodeFunctions(context, buf, functions);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        if (!functions.isEmpty()) {
            components.add(pad(pad, translatable("ali.type.function.sequence")));
            components.add(pad(pad + 1, translatable("ali.property.function.sequence.function")));
            functions.forEach((f) -> components.addAll(f.getTooltip(pad + 2)));
        }

        return components;
    }
}
