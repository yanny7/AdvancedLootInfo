package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.mixin.MixinSetStewEffectFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetStewEffectFunction extends LootConditionalFunction {
    public final Map<ResourceLocation, RangeValue> effectMap;

    public SetStewEffectFunction(IContext context, LootItemFunction function) {
        super(context, function);
        effectMap = ((MixinSetStewEffectFunction) function).getEffectDurationMap().entrySet().stream().collect(Collectors.toMap(
                (e) -> BuiltInRegistries.MOB_EFFECT.getKey(e.getKey()),
                (e) -> context.utils().convertNumber(context, e.getValue())
        ));
    }

    public SetStewEffectFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        int count = buf.readInt();

        effectMap = new HashMap<>();

        for (int i = 0; i < count; i++) {
            effectMap.put(buf.readResourceLocation(), new RangeValue(buf));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeInt(effectMap.size());
        effectMap.forEach((location, level) -> {
            buf.writeResourceLocation(location);
            level.encode(buf);
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_stew_effect")));

        effectMap.forEach((effect, duration) -> {
            String mobEffect = BuiltInRegistries.MOB_EFFECT.getOptional(effect).map(MobEffect::getDescriptionId).orElse("???");
            components.add(pad(pad + 1, translatable("ali.property.function.set_stew_effect.effect", translatable(mobEffect), duration)));
        });

        return components;
    }
}
