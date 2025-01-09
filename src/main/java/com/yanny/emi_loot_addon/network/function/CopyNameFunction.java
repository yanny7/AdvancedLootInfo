package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinCopyNameFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

import static com.yanny.emi_loot_addon.compatibility.EmiUtils.*;

public class CopyNameFunction extends LootConditionalFunction {
    public final String source;

    public CopyNameFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        source = ((MixinCopyNameFunction) function).getSource().name;
    }

    public CopyNameFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
        source = buf.readUtf();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeUtf(source);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

        components.add(pad(pad + 1, translatable("emi.property.function.copy_name.source", value(translatableType("emi.enum.name_source",
                net.minecraft.world.level.storage.loot.functions.CopyNameFunction.NameSource.getByName(source))))));

        return components;
    }
}
