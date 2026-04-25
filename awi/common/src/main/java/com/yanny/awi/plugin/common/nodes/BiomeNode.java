package com.yanny.awi.plugin.common.nodes;

import com.yanny.awi.Utils;
import com.yanny.awi.api.IServerUtils;
import com.yanny.awi.api.ITooltipNode;
import com.yanny.awi.api.ListNode;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BiomeNode extends ListNode {
    public static final ResourceLocation ID = Utils.modLoc("biome");

    public BiomeNode(IServerUtils utils, Biome biome) {
        BiomeGenerationSettings settings = biome.getGenerationSettings();
        List<HolderSet<PlacedFeature>> features = settings.features();

        for (int i = 0; i < features.size(); i++) {
            HolderSet<PlacedFeature> feature = features.get(i);
            GenerationStep.Decoration step = GenerationStep.Decoration.values()[i];

            addChildren(new PlacedFeatureNode(utils, step, feature));
        }
    }

    @Override
    public void encodeNode(IServerUtils utils, RegistryFriendlyByteBuf buf) {

    }

    @NotNull
    @Override
    public ITooltipNode getTooltip() {
        return null;
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
