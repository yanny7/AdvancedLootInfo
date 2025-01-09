package com.yanny.advanced_loot_info.network.function;

import com.mojang.datafixers.util.Pair;
import com.yanny.advanced_loot_info.mixin.MixinSetBannerPatternFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.*;

public class SetBannerPatternFunction extends LootConditionalFunction {
    public final boolean append;
    public final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

    public SetBannerPatternFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        append = ((MixinSetBannerPatternFunction) function).getAppend();
        patterns = ((MixinSetBannerPatternFunction) function).getPatterns();
    }

    public SetBannerPatternFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
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
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeBoolean(append);
        buf.writeInt(patterns.size());
        patterns.forEach((pair) -> {
            buf.writeResourceLocation(BuiltInRegistries.BANNER_PATTERN.getKey(pair.getFirst().get()));
            buf.writeInt(pair.getSecond().getId());
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = super.getTooltip(pad);

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
