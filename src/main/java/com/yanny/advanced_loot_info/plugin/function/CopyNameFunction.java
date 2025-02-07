package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinCopyNameFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.*;

public class CopyNameFunction extends LootConditionalFunction {
    public final String source;

    public CopyNameFunction(IContext context, LootItemFunction function) {
        super(context, function);
        source = ((MixinCopyNameFunction) function).getSource().name;
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

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.copy_name")));
        components.add(pad(pad + 1, translatable("emi.property.function.copy_name.source", value(translatableType("emi.enum.name_source",
                net.minecraft.world.level.storage.loot.functions.CopyNameFunction.NameSource.getByName(source))))));

        return components;
    }
}
