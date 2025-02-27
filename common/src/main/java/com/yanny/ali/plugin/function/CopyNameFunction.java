package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyNameFunction;
import com.yanny.ali.plugin.TooltipUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

public class CopyNameFunction extends LootConditionalFunction {
    public final String source;

    public CopyNameFunction(IContext context, LootItemFunction function) {
        super(context, function);
        source = ((MixinCopyNameFunction) function).getSource().getSerializedName();
    }

    public CopyNameFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        source = buf.readUtf();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeUtf(source);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(TooltipUtils.pad(pad, TooltipUtils.translatable("ali.type.function.copy_name")));
        components.add(TooltipUtils.pad(pad + 1, TooltipUtils.translatable("ali.property.function.copy_name.source", TooltipUtils.value(TooltipUtils.translatableType("ali.enum.name_source", source)))));

        return components;
    }
}
