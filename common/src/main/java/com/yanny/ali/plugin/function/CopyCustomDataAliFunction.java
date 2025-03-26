package com.yanny.ali.plugin.function;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinCopyCustomDataFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.CopyCustomDataFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CopyCustomDataAliFunction extends LootConditionalAliFunction {
    public final NbtProvider source;
    public final List<CopyCustomDataFunction.CopyOperation> operations;

    public CopyCustomDataAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        source = ((MixinCopyCustomDataFunction) function).getSource();
        operations = ((MixinCopyCustomDataFunction) function).getOperations();
    }

    public CopyCustomDataAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        source = Objects.requireNonNull(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.get(buf.readResourceLocation())).codec().compressedDecode(JavaOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow();

        int count = buf.readInt();

        operations = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            operations.add(CopyCustomDataFunction.CopyOperation.CODEC.decode(JavaOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst());
        }
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE.getKey(source.getType())));
        buf.writeJsonWithCodec(ExtraCodecs.JSON, ((MapCodec<NbtProvider>)source.getType().codec()).encode(source, (DynamicOps<JsonElement>)(Object) JavaOps.INSTANCE, JsonOps.INSTANCE.mapBuilder()).build(JsonOps.INSTANCE.empty()).getOrThrow());
        buf.writeInt(operations.size());
        operations.forEach((o) ->
                buf.writeJsonWithCodec(ExtraCodecs.JSON, CopyCustomDataFunction.CopyOperation.CODEC.encodeStart(JsonOps.INSTANCE, o).getOrThrow())
        );
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getCopyCustomData(pad, source, operations);
    }
}
