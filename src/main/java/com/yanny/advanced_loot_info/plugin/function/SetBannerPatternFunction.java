package com.yanny.advanced_loot_info.plugin.function;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.api.IContext;
import com.yanny.advanced_loot_info.mixin.MixinSetBannerPatternFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.emi.EmiUtils.*;

public class SetBannerPatternFunction extends LootConditionalFunction {
    public final boolean append;
    public final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

    public SetBannerPatternFunction(IContext context, LootItemFunction function) {
        super(context, function);
        append = ((MixinSetBannerPatternFunction) function).getAppend();
        patterns = ((MixinSetBannerPatternFunction) function).getPatterns();
    }

    public SetBannerPatternFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        append = buf.readBoolean();
        int count = buf.readInt();
        patterns = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            patterns.add(new Pair<>(
                    BuiltInRegistries.BANNER_PATTERN.getHolder(ResourceKey.create(Registries.BANNER_PATTERN, buf.readResourceLocation())).get(),
                    DyeColor.byId(buf.readInt())
            ));
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeBoolean(append);
        buf.writeInt(patterns.size());
        patterns.forEach((pair) -> {
            buf.writeResourceLocation(BuiltInRegistries.BANNER_PATTERN.getKey(pair.getFirst().get()));
            buf.writeInt(pair.getSecond().getId());
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.set_banner_pattern")));
        components.add(pad(pad + 1, translatable("emi.property.function.set_banner_pattern.append", append)));

        if (!patterns.isEmpty()) {
            components.add(pad(pad + 1, translatable("emi.property.function.set_banner_pattern.patterns")));

            patterns.forEach((pair) -> {
                components.add(pad(pad + 2, value(BuiltInRegistries.BANNER_PATTERN.getKey(pair.getFirst().get()))));
                components.add(pad(pad + 3, translatable("emi.property.function.set_banner_pattern.color", pair.getSecond().getName())));
            });
        }

        return components;
    }
}
