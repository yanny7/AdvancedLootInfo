package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinToggleTooltips;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.ToggleTooltips;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ToggleTooltipsAliFunction extends LootConditionalAliFunction {
    public final Map<ToggleTooltips.ComponentToggle<?>, Boolean> values;

    public ToggleTooltipsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        values = ((MixinToggleTooltips) function).getValues();
    }

    public ToggleTooltipsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        values = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ToggleTooltips.ComponentToggle<?> key = MixinToggleTooltips.getToggleCodec().decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
            boolean value = buf.readBoolean();
            values.put(key, value);
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(values.size());
        values.forEach((key, value) -> {
            buf.writeJsonWithCodec(ExtraCodecs.JSON, MixinToggleTooltips.getToggleCodec().encodeStart(JsonOps.INSTANCE, key).getOrThrow());
            buf.writeBoolean(value);
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();
/* FIXME
        components.add(pad(pad, translatable("ali.type.function.toggle_tooltips")));

        if (!values.isEmpty()) {
            values.forEach((key, value) -> components.add(pad(pad + 1, keyValue(Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(key.type())), value))));
        };
*/
        return components;
    }
}
