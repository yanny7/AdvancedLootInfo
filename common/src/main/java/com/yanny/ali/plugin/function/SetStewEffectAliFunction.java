package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetStewEffectFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SetStewEffectAliFunction extends LootConditionalAliFunction {
    public final Map<MobEffect, RangeValue> effectMap;

    public SetStewEffectAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        effectMap = ((MixinSetStewEffectFunction) function).getEffectDurationMap().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                (e) -> context.utils().convertNumber(context, e.getValue())
        ));
    }

    public SetStewEffectAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        effectMap = new HashMap<>();

        for (int i = 0; i < count; i++) {
            effectMap.put(BuiltInRegistries.MOB_EFFECT.get(buf.readResourceLocation()), new RangeValue(buf));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(effectMap.size());
        effectMap.forEach((location, level) -> {
            buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(location)));
            level.encode(buf);
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetStewEffectTooltip(pad, effectMap);
    }
}
