package com.yanny.ali.plugin.function;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetFireworksFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.storage.loot.functions.ListOperation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class SetFireworksFunction extends LootConditionalFunction {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<ListOperation.StandAlone<FireworkExplosion>> explosions;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<Integer> duration;

    public SetFireworksFunction(IContext context, LootItemFunction function) {
        super(context, function);
        explosions = ((MixinSetFireworksFunction) function).getExplosions();
        duration = ((MixinSetFireworksFunction) function).getFlightDuration();
    }

    public SetFireworksFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        Optional<JsonElement> jsonElement = buf.readOptional((a) -> a.readJsonWithCodec(ExtraCodecs.JSON));
        explosions = jsonElement.flatMap((e) -> ListOperation.StandAlone.codec(FireworkExplosion.CODEC, 256).decode(JsonOps.INSTANCE, e).result()).map(Pair::getFirst);
        duration = buf.readOptional(FriendlyByteBuf::readInt);
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeOptional(explosions, (b, a) -> b.writeJsonWithCodec(ExtraCodecs.JSON, ListOperation.StandAlone.codec(FireworkExplosion.CODEC, 256).encodeStart(JsonOps.INSTANCE, a).getOrThrow()));
        buf.writeOptional(duration, FriendlyByteBuf::writeInt);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.set_fireworks")));
        explosions.ifPresent((e) -> {
            components.add(pad(pad + 1, translatable("ali.property.function.set_fireworks.explosions")));
            components.add(pad(pad + 2, translatable("ali.property.function.set_fireworks.explosions.operation", e.operation().mode().getSerializedName())));

            if (!e.value().isEmpty()) {
                components.add(pad(pad + 2, translatable("ali.property.function.set_fireworks.explosions.list")));
                e.value().forEach((f) -> {
                    components.add(pad(pad + 3, translatable("ali.property.function.set_fireworks.explosions.explosion")));
                    components.add(pad(pad + 4, translatable("ali.property.function.set_fireworks.explosions.explosion.shape", f.shape().getSerializedName())));
                    components.add(pad(pad + 4, translatable("ali.property.function.set_fireworks.explosions.explosion.colors", f.colors())));
                    components.add(pad(pad + 4, translatable("ali.property.function.set_fireworks.explosions.explosion.fade_colors", f.fadeColors())));
                    components.add(pad(pad + 4, translatable("ali.property.function.set_fireworks.explosions.explosion.has_trails", f.hasTrail())));
                    components.add(pad(pad + 4, translatable("ali.property.function.set_fireworks.explosions.explosion.has_twinkle", f.hasTwinkle())));
                });
            }
        });

        return components;
    }
}
