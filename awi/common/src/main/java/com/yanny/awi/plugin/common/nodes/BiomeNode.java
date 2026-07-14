package com.yanny.awi.plugin.common.nodes;

import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class BiomeNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("biome");

    private final TooltipNode tooltip;
    private final ResourceLocation biomeId;

    public BiomeNode(IServerUtils utils, Biome biome, TooltipNode tooltip, Set<Block> blocks) {
        BiomeGenerationSettings settings = biome.getGenerationSettings();
        List<HolderSet<PlacedFeature>> features = settings.features();

        addChildren(new BaseTerrainNode(utils, blocks));

        for (int i = 0; i < features.size(); i++) {
            HolderSet<PlacedFeature> feature = features.get(i);
            GenerationStep.Decoration step = GenerationStep.Decoration.values()[i];

            addChildren(new GenerationStepNode(utils, step, feature));
        }

        this.tooltip = tooltip;
        biomeId = utils.getServerLevel().registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
    }

    public BiomeNode(IClientUtils utils, RegistryFriendlyByteBuf buf) {
        super(utils, buf);
        tooltip = utils.getTooltipCache().getNodeById(buf.readVarInt());
        biomeId = buf.readResourceLocation();
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(utils.getTooltipCache().getNodeId(tooltip));
        buf.writeResourceLocation(biomeId);
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
}
