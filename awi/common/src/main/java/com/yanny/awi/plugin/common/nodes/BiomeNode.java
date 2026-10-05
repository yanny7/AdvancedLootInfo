package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.spawn.SpawnInfo;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.BlockInfo;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import com.yanny.awi.plugin.server.summary.ColumnContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BiomeNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("biome");

    private final TooltipNode tooltip;
    private final ResourceLocation biomeId;
    private final Map<EntityType<?>, TooltipNode> spawns;

    public BiomeNode(IServerUtils utils, Biome biome, TooltipNode tooltip, Set<BlockInfo> blocks, Block defaultBlock, Fluid defaultFluid,
                     ColumnContext columnContext, WorldgenNodeCache nodeCache, @Nullable SpawnInfo spawnInfo) {
        BiomeGenerationSettings settings = biome.getGenerationSettings();
        List<HolderSet<PlacedFeature>> features = settings.features();

        addChildren(new BaseTerrainNode(utils, blocks, defaultBlock, defaultFluid));

        for (int i = 0; i < features.size(); i++) {
            addChildren(nodeCache.getOrCreate(utils, i, features.get(i), columnContext));
        }

        this.tooltip = tooltip;
        biomeId = utils.getServerLevel().registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
        spawns = spawnInfo != null ? spawnInfo.getBiomeSpawns(biomeId) : Collections.emptyMap();
    }

    public BiomeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        biomeId = buf.readResourceLocation();
        int spawnCount = buf.readVarInt();

        spawns = new LinkedHashMap<>();

        for (int i = 0; i < spawnCount; i++) {
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(buf.readResourceLocation());
            TooltipNode conditions = utils.getTooltipCache().getNodeById(buf.readVarInt());

            type.ifPresent((t) -> spawns.put(t, conditions));
        }
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeResourceLocation(biomeId);
        buf.writeVarInt(spawns.size());
        spawns.forEach((type, conditions) -> {
            buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(type));
            buf.writeVarInt(utils.getTooltipCache().getNodeId(conditions));
        });
    }

    @NotNull
    @Override
    public TooltipNode getTooltip() {
        return tooltip;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ResourceLocation getBiomeId() {
        return biomeId;
    }

    @NotNull
    public Map<EntityType<?>, TooltipNode> getSpawns() {
        return spawns;
    }
}
