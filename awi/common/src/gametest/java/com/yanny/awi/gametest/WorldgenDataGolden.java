package com.yanny.awi.gametest;

import com.yanny.aci.api.ICoreDataNode;
import com.yanny.aci.gametest.GoldenFiles;
import com.yanny.aci.gametest.GoldenLanguage;
import com.yanny.aci.gametest.GoldenRenderer;
import com.yanny.aci.tooltip.NumberOptions;
import com.yanny.awi.Utils;
import com.yanny.awi.api.IBlockNode;
import com.yanny.awi.api.IClientUtils;
import com.yanny.awi.api.IDataNode;
import com.yanny.awi.configuration.AwiConfig;
import com.yanny.awi.manager.PluginManager;
import com.yanny.awi.network.AbstractServer;
import com.yanny.awi.network.WorldgenDataChunkMessage;
import com.yanny.awi.network.WorldgenDataReader;
import com.yanny.awi.plugin.common.nodes.BiomeNode;
import com.yanny.awi.plugin.common.nodes.GenerationStepNode;
import com.yanny.awi.plugin.common.nodes.LevelStemNode;
import com.yanny.awi.plugin.common.nodes.PlacedFeatureNode;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class WorldgenDataGolden {
    public static void run(GameTestHelper helper, AbstractServer awiServer) {
        if (PluginManager.getInstance().clientRegistry == null) {
            PluginManager.getInstance().registerClientEvent();
        }

        GoldenLanguage.inject(List.of("minecraft", "aci", Utils.MOD_ID));

        List<String> differences = GoldenFiles.fromProperties(Utils.MOD_ID).compare(capture(awiServer, PluginManager.getInstance().commonRegistry.getConfiguration(), helper.getLevel().getServer().registryAccess()));

        if (!differences.isEmpty()) {
            throw new GameTestAssertException(differences.size() + " golden difference(s): " + differences.subList(0, Math.min(10, differences.size())));
        }

        helper.succeed();
    }

    @NotNull
    private static Map<String, String> capture(AbstractServer awiServer, AwiConfig config, RegistryAccess registryAccess) {
        IClientUtils utils = PluginManager.getInstance().clientRegistry;
        RegistryFriendlyByteBuf buf = WorldgenDataReader.decompress(readChunks(awiServer), registryAccess);
        Map<ResourceLocation, LevelStemNode> worldgenData = new HashMap<>();

        try {
            WorldgenDataReader.decode(utils, buf, worldgenData);

            if (buf.isReadable()) {
                throw new GameTestAssertException(buf.readableBytes() + " byte(s) left unread after decoding");
            }
        } finally {
            buf.release();
        }

        Map<String, IDataNode> pages = new TreeMap<>();

        worldgenData.forEach((dimension, levelStem) -> {
            for (IDataNode node : levelStem.nodes()) {
                if (!(node instanceof BiomeNode biomeNode)) {
                    throw new GameTestAssertException("Dimension " + dimension + " holds a " + node.getId() + " node instead of a biome");
                }

                ResourceLocation biome = biomeNode.getBiomeId();

                pages.put(dimension.getNamespace() + "/" + dimension.getPath() + "/" + biome.getNamespace() + "/" + biome.getPath() + ".txt", biomeNode);
            }
        });

        Map<String, String> files = new TreeMap<>();

        pages.forEach((key, node) -> {
            files.put("normal/" + key, renderer(false, config).render(node));
            files.put("advanced/" + key, renderer(true, config).render(node));
        });

        return files;
    }

    private static byte[] readChunks(AbstractServer awiServer) {
        List<WorldgenDataChunkMessage> chunks = awiServer.getChunks();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (chunks.isEmpty()) {
            throw new GameTestAssertException("Server produced no worldgen data");
        }

        for (WorldgenDataChunkMessage chunk : chunks) {
            bos.writeBytes(chunk.data());
        }

        return bos.toByteArray();
    }

    @NotNull
    private static GoldenRenderer renderer(boolean isAdvanced, AwiConfig config) {
        return new GoldenRenderer(isAdvanced, new NumberOptions(isAdvanced, config.showCharts, Locale.ROOT)) {
            @Override
            protected String describe(ICoreDataNode<?> node) {
                return switch (node) {
                    case IBlockNode blockNode -> blockNode.getBlock().map((block) -> BuiltInRegistries.BLOCK.getKey(block).toString(), (tag) -> "#" + tag.location());
                    case PlacedFeatureNode placedFeatureNode -> placedFeatureNode.getFeatureId().toString();
                    case GenerationStepNode generationStepNode -> String.valueOf(generationStepNode.getGenerationStep());
                    case BiomeNode biomeNode -> biomeNode.getBiomeId().toString();
                    default -> null;
                };
            }

            @Override
            protected void appendExtra(StringBuilder builder, String indent, ICoreDataNode<?> node) {
                if (node instanceof BiomeNode biomeNode) {
                    biomeNode.getSpawns().entrySet().stream()
                            .sorted(Comparator.comparing((entry) -> BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString()))
                            .forEach((entry) -> {
                                builder.append(indent).append("  spawn ").append(BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey())).append('\n');
                                appendTooltip(builder, indent + "  ", entry.getValue());
                            });
                }
            }
        };
    }
}
