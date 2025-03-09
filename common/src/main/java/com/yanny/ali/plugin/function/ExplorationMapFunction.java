package com.yanny.ali.plugin.function;

import com.mojang.serialization.JsonOps;
import com.yanny.ali.api.IContext;
import com.yanny.ali.mixin.MixinExplorationMapFunction;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.ali.plugin.TooltipUtils.pad;
import static com.yanny.ali.plugin.TooltipUtils.translatable;

public class ExplorationMapFunction extends LootConditionalFunction {
    public final ResourceLocation structure;
    public final Holder<MapDecorationType> mapDecoration;
    public final byte zoom;
    public final int searchRadius;
    public final boolean skipKnownStructures;

    public ExplorationMapFunction(IContext context, LootItemFunction function) {
        super(context, function);
        structure = ((MixinExplorationMapFunction) function).getDestination().location();
        mapDecoration = ((MixinExplorationMapFunction) function).getMapDecoration();
        zoom = ((MixinExplorationMapFunction) function).getZoom();
        searchRadius = ((MixinExplorationMapFunction) function).getSearchRadius();
        skipKnownStructures = ((MixinExplorationMapFunction) function).getSkipKnownStructures();
    }

    public ExplorationMapFunction(IContext context, FriendlyByteBuf buf) {
        super(context, buf);
        structure = buf.readResourceLocation();
        mapDecoration = MapDecorationType.CODEC.decode(JsonOps.INSTANCE, buf.readJsonWithCodec(ExtraCodecs.JSON)).getOrThrow().getFirst();
        zoom = buf.readByte();
        searchRadius = buf.readInt();
        skipKnownStructures = buf.readBoolean();
    }

    @Override
    public void encode(IContext context, FriendlyByteBuf buf) {
        super.encode(context, buf);
        buf.writeResourceLocation(structure);
        buf.writeJsonWithCodec(ExtraCodecs.JSON, MapDecorationType.CODEC.encodeStart(JsonOps.INSTANCE, mapDecoration).getOrThrow());
        buf.writeByte(zoom);
        buf.writeInt(searchRadius);
        buf.writeBoolean(skipKnownStructures);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("ali.type.function.exploration_map")));
        components.add(pad(pad + 1, translatable("ali.property.function.exploration_map.destination", structure)));
        components.add(pad(pad + 1, translatable("ali.property.function.exploration_map.map_decoration")));
        components.add(pad(pad + 2, translatable("ali.property.function.exploration_map.map_decoration.asset_id", mapDecoration.value().assetId())));
        components.add(pad(pad + 2, translatable("ali.property.function.exploration_map.map_decoration.show_on_item_frame", mapDecoration.value().showOnItemFrame())));
        components.add(pad(pad + 2, translatable("ali.property.function.exploration_map.map_decoration.map_color", mapDecoration.value().mapColor())));
        components.add(pad(pad + 2, translatable("ali.property.function.exploration_map.map_decoration.exploration_map_element", mapDecoration.value().explorationMapElement())));
        components.add(pad(pad + 2, translatable("ali.property.function.exploration_map.map_decoration.track_count", mapDecoration.value().trackCount())));
        components.add(pad(pad + 1, translatable("ali.property.function.exploration_map.zoom", zoom)));
        components.add(pad(pad + 1, translatable("ali.property.function.exploration_map.search_radius", searchRadius)));
        components.add(pad(pad + 1, translatable("ali.property.function.exploration_map.skip_known_structures", skipKnownStructures)));

        return components;
    }
}
