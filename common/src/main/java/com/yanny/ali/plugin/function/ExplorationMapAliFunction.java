package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinExplorationMapFunction;
import com.yanny.ali.plugin.FunctionTooltipUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.List;

public class ExplorationMapAliFunction extends LootConditionalAliFunction {
    public final TagKey<Structure> structure;
    public final Holder<MapDecorationType> mapDecoration;
    public final byte zoom;
    public final int searchRadius;
    public final boolean skipKnownStructures;

    public ExplorationMapAliFunction(IContext context, LootItemFunction function) {
        super(context, function);
        structure = ((MixinExplorationMapFunction) function).getDestination();
        mapDecoration = ((MixinExplorationMapFunction) function).getMapDecoration();
        zoom = ((MixinExplorationMapFunction) function).getZoom();
        searchRadius = ((MixinExplorationMapFunction) function).getSearchRadius();
        skipKnownStructures = ((MixinExplorationMapFunction) function).getSkipKnownStructures();
    }

    public ExplorationMapAliFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        structure = TagKey.create(Registries.STRUCTURE, buf.readResourceLocation());
        mapDecoration = MapDecorationType.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
        zoom = buf.readByte();
        searchRadius = buf.readInt();
        skipKnownStructures = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(structure.location());
        buf.writeJsonWithCodec(ExtraCodecs.JSON, MapDecorationType.CODEC.encodeStart(JsonOps.INSTANCE, mapDecoration).getOrThrow());
        buf.writeByte(zoom);
        buf.writeInt(searchRadius);
        buf.writeBoolean(skipKnownStructures);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        return FunctionTooltipUtils.getExplorationMapTooltip(pad, structure, mapDecoration, zoom, searchRadius, skipKnownStructures);
    }
}
