package com.yanny.ali.plugin.function;

import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyComponentsFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CopyComponentsAliFunction extends LootConditionalAliFunction {
    public final String source;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<List<ResourceLocation>> include;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final Optional<List<ResourceLocation>> exclude;

    public CopyComponentsAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        source = ((MixinCopyComponentsFunction) function).getSource().getSerializedName();
        include = ((MixinCopyComponentsFunction) function).getInclude().map((f) -> f.stream().map(BuiltInRegistries.DATA_COMPONENT_TYPE::getKey).toList());
        exclude = ((MixinCopyComponentsFunction) function).getInclude().map((f) -> f.stream().map(BuiltInRegistries.DATA_COMPONENT_TYPE::getKey).toList());
    }

    public CopyComponentsAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        source = buf.readUtf();
        include = buf.readOptional((o) -> {
            int count = buf.readInt();
            List<ResourceLocation> locations = new LinkedList<>();

            for (int i = 0; i < count; i++) {
                locations.add(buf.readResourceLocation());
            }

            return locations;
        });
        exclude = buf.readOptional((o) -> {
            int count = buf.readInt();
            List<ResourceLocation> locations = new LinkedList<>();

            for (int i = 0; i < count; i++) {
                locations.add(buf.readResourceLocation());
            }

            return locations;
        });
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeUtf(source);
        buf.writeOptional(include, (b, l) -> {
            b.writeInt(l.size());
            l.forEach(b::writeResourceLocation);
        });
        buf.writeOptional(exclude, (b, l) -> {
            b.writeInt(l.size());
            l.forEach(b::writeResourceLocation);
        });
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();
/* FIXME
        components.add(pad(pad, translatable("ali.type.function.copy_components")));
        components.add(pad(pad + 1, translatable("ali.property.function.copy_components.source", source)));

        include.ifPresent((i) -> {
            if (!i.isEmpty()) {
                components.add(pad(pad + 1, translatable("ali.property.function.copy_components.include")));
                i.forEach(r -> components.add(pad(pad + 2, r)));
            }
        });
        exclude.ifPresent((i) -> {
            if (!i.isEmpty()) {
                components.add(pad(pad + 1, translatable("ali.property.function.copy_components.exclude")));
                i.forEach(r -> components.add(pad(pad + 2, r)));
            }
        });
*/
        return components;
    }
}
