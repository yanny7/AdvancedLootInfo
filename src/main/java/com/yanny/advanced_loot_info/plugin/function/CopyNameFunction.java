package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.mixin.MixinCopyNameFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class CopyNameFunction extends LootConditionalFunction {
    public final String source;

    public CopyNameFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        source = ((MixinCopyNameFunction) function).getSource().name;
    }

    public CopyNameFunction(FriendlyByteBuf buf) {
        super(buf);
        source = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
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
