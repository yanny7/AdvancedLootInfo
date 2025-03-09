package com.yanny.ali.plugin.function;

import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyCustomDataFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class CopyCustomDataFunction extends LootConditionalFunction {
    public final ResourceLocation source;
    public final List<net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction.CopyOperation> operations;

    public CopyCustomDataFunction(IContext context, LootItemFunction function) {
        super(context, function);
        source = BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.getKey(((MixinCopyCustomDataFunction) function).getSource().getType());
        operations = ((MixinCopyCustomDataFunction) function).getOperations();
    }

    public CopyCustomDataFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        source = buf.readResourceLocation();

        int count = buf.readInt();

        operations = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            operations.add(net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction.CopyOperation.CODEC.decode(JavaOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(source);
        buf.writeInt(operations.size());
        operations.forEach((o) ->
                buf.writeJsonWithCodec(ExtraCodecs.JSON, net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction.CopyOperation.CODEC.encodeStart(JsonOps.INSTANCE, o).getOrThrow())
        );
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.copy_custom_data")));
        components.add(pad(pad + 1, translatable("ali.property.function.copy_custom_data.source", source)));

        if (!operations.isEmpty()) {
            components.add(pad(pad + 1, translatable("ali.property.function.copy_custom_data.operations")));
            operations.forEach((o) -> {
                components.add(pad(pad + 2, translatable("ali.property.function.copy_custom_data.operation")));
                components.add(pad(pad + 3, translatable("ali.property.function.copy_custom_data.operation.source", o.sourcePath().toString())));
                components.add(pad(pad + 3, translatable("ali.property.function.copy_custom_data.operation.target", o.targetPath().toString())));
                components.add(pad(pad + 3, translatable("ali.property.function.copy_custom_data.operation.operation", o.op().getSerializedName())));
            });
        }

        return components;
    }
}
