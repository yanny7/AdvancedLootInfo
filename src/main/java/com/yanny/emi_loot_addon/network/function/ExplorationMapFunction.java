package com.yanny.emi_loot_addon.network.function;

import com.yanny.emi_loot_addon.mixin.MixinExplorationMapFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

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

    public ExplorationMapFunction(FunctionType type, FriendlyByteBuf buf) {
        super(type, buf);
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
}
