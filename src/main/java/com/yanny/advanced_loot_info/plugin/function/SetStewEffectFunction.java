package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.api.RangeValue;
import com.yanny.advanced_loot_info.mixin.MixinSetStewEffectFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.translatable;

public class SetStewEffectFunction extends LootConditionalFunction {
    public final Map<ResourceLocation, RangeValue> effectMap;

    public SetStewEffectFunction(IContext context, LootItemFunction function) {
        super(context, function);
        effectMap = ((MixinSetStewEffectFunction) function).getEffectDurationMap().entrySet().stream().collect(Collectors.toMap(
                (e) -> ForgeRegistries.MOB_EFFECTS.getKey(e.getKey()),
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

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_stew_effect")));

        effectMap.forEach((effect, duration) -> {
            components.add(pad(pad + 1, translatable("emi.property.function.set_stew_effect.effect", translatable(ForgeRegistries.MOB_EFFECTS.getValue(effect).getDescriptionId()), duration)));
        });

        return components;
    }
}
