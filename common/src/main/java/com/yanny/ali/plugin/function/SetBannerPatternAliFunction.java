package com.yanny.ali.plugin.function;

import com.mojang.datafixers.util.Pair;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinSetBannerPatternFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
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
import java.util.Objects;

public class SetBannerPatternAliFunction extends LootConditionalAliFunction {
    public final boolean append;
    public final List<Pair<Holder<BannerPattern>, DyeColor>> patterns;

    public SetBannerPatternAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        append = ((MixinSetBannerPatternFunction) function).getAppend();
        patterns = ((MixinSetBannerPatternFunction) function).getPatterns();
    }

    public SetBannerPatternAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        append = buf.readBoolean();
        int count = buf.readInt();
        patterns = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            patterns.add(new Pair<>(
                    BuiltInRegistries.BANNER_PATTERN.getHolder(ResourceKey.create(Registries.BANNER_PATTERN, buf.readResourceLocation())).orElseThrow(),
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
            buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.BANNER_PATTERN.getKey(pair.getFirst().value()), "Banner Pattern is not registered!"));
            buf.writeInt(pair.getSecond().getId());
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getSetBannerPatternTooltip(pad, append, patterns);
    }
}
