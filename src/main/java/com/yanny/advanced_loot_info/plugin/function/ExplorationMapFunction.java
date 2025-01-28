package com.yanny.advanced_loot_info.plugin.function;

import com.yanny.advanced_loot_info.mixin.MixinExplorationMapFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

import java.util.LinkedList;
import java.util.List;

import static com.yanny.advanced_loot_info.compatibility.EmiUtils.pad;
import static com.yanny.advanced_loot_info.compatibility.EmiUtils.translatable;

public class ExplorationMapFunction extends LootConditionalFunction {
    public final ResourceLocation structure;
    public final byte mapDecoration;
    public final byte zoom;
    public final int searchRadius;
    public final boolean skipKnownStructures;

    public ExplorationMapFunction(LootContext lootContext, LootItemFunction function) {
        super(lootContext, function);
        structure = ((MixinExplorationMapFunction) function).getDestination().location();
        mapDecoration = ((MixinExplorationMapFunction) function).getMapDecoration().getIcon();
        zoom = ((MixinExplorationMapFunction) function).getZoom();
        searchRadius = ((MixinExplorationMapFunction) function).getSearchRadius();
        skipKnownStructures = ((MixinExplorationMapFunction) function).getSkipKnownStructures();
    }

    public ExplorationMapFunction(FriendlyByteBuf buf) {
        super(buf);
        structure = buf.readResourceLocation();
        mapDecoration = buf.readByte();
        zoom = buf.readByte();
        searchRadius = buf.readInt();
        skipKnownStructures = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeResourceLocation(structure);
        buf.writeByte(mapDecoration);
        buf.writeByte(zoom);
        buf.writeInt(searchRadius);
        buf.writeBoolean(skipKnownStructures);
    }

    @Override
    public List<Component> getTooltip(int pad) {
        List<Component> components = new LinkedList<>();

        components.add(pad(pad, translatable("emi.type.advanced_loot_info.function.exploration_map")));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.destination", structure)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.map_decoration", MapDecoration.Type.byIcon(mapDecoration))));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.zoom", zoom)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.search_radius", searchRadius)));
        components.add(pad(pad + 1, translatable("emi.property.function.exploration_map.skip_known_structures", skipKnownStructures)));

        return components;
    }
}
