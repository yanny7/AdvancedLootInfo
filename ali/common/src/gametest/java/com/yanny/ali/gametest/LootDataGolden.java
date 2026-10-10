package com.yanny.ali.gametest;

import com.yanny.aci.api.ICoreDataNode;
import com.yanny.aci.gametest.GoldenFiles;
import com.yanny.aci.gametest.GoldenRenderer;
import com.yanny.aci.tooltip.NumberOptions;
import com.yanny.ali.Utils;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import com.yanny.ali.network.LootDataChunkMessage;
import com.yanny.ali.network.LootDataReader;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LootDataGolden {
    private static final String DEFAULT_VARIANT = "default";
    private static final String REMOVED_FILE = "removed.txt";
    private static final List<Variant> VARIANTS = List.of(
            new Variant("show_default_block_loot", (config) -> {
                boolean old = config.hideDefaultBlockLoot;

                config.hideDefaultBlockLoot = false;
                return () -> config.hideDefaultBlockLoot = old;
            }),
            new Variant("show_entities_without_loot", (config) -> {
                boolean old = config.showEntitiesWithoutLoot;

                config.showEntitiesWithoutLoot = true;
                return () -> config.showEntitiesWithoutLoot = old;
            })
    );

    public static final int TIMEOUT_TICKS = 6000;

    public static void run(GameTestHelper helper, AbstractServer aliServer, List<Variant> loaderVariants) {
        if (PluginManager.getInstance().clientRegistry == null) {
            PluginManager.getInstance().registerClientEvent();
        }

        MinecraftServer server = helper.getLevel().getServer();
        AliConfig config = PluginManager.getInstance().commonRegistry.getConfiguration();
        Map<String, String> defaults = capture(aliServer, config);
        List<String> differences = new ArrayList<>(compare(DEFAULT_VARIANT, defaults));
        GameTestSequence sequence = helper.startSequence();

        for (Variant variant : Stream.concat(VARIANTS.stream(), loaderVariants.stream()).toList()) {
            Runnable[] revert = new Runnable[1];

            reload(sequence, server, () -> revert[0] = variant.apply().apply(config));
            sequence.thenExecute(() -> {
                Map<String, String> changed = diff(defaults, capture(aliServer, config));

                revert[0].run();

                if (changed.isEmpty()) {
                    differences.add(variant.name() + ": output is identical to " + DEFAULT_VARIANT);
                }

                differences.addAll(compare(variant.name(), changed));
            });
        }

        reload(sequence, server, () -> {});
        sequence.thenExecute(() -> {
            if (!defaults.equals(capture(aliServer, config))) {
                differences.add("output after a datapack reload differs from the output of the startup scan");
            }

            differences.addAll(scriptErrors());

            if (!differences.isEmpty()) {
                throw new GameTestAssertException(differences.size() + " golden difference(s): " + differences.subList(0, Math.min(10, differences.size())));
            }
        });
        sequence.thenSucceed();
    }

    private static List<String> scriptErrors() {
        return ScriptType.SERVER.console.errors.stream().map((line) -> "KubeJS: " + line.getText()).toList();
    }

    private static void reload(GameTestSequence sequence, MinecraftServer server, Runnable before) {
        List<CompletableFuture<Void>> future = new ArrayList<>(1);

        sequence.thenExecute(() -> {
            before.run();
            future.add(server.reloadResources(server.getPackRepository().getSelectedIds()));
        });
        sequence.thenWaitUntil(() -> {
            if (!future.get(0).isDone()) {
                throw new GameTestAssertException("Waiting for datapack reload");
            }

            future.get(0).join();
        });
    }

    @NotNull
    private static Map<String, String> capture(AbstractServer aliServer, AliConfig config) {
        IClientUtils utils = PluginManager.getInstance().clientRegistry;
        FriendlyByteBuf buf = LootDataReader.decompress(readChunks(aliServer));
        Map<ResourceLocation, IDataNode> lootData = new HashMap<>();
        Map<ResourceLocation, IDataNode> tradeData = new HashMap<>();

        try {
            LootDataReader.decode(utils, buf, lootData, tradeData);

            if (buf.isReadable()) {
                throw new GameTestAssertException(buf.readableBytes() + " byte(s) left unread after decoding");
            }
        } finally {
            buf.release();
        }

        Map<String, IDataNode> pages = new TreeMap<>();

        lootData.forEach((location, node) -> pages.put("loot/" + location.getNamespace() + "/" + location.getPath() + ".txt", node));
        tradeData.forEach((location, node) -> pages.put("trade/" + location.getNamespace() + "/" + location.getPath() + ".txt", node));

        Map<String, String> files = new TreeMap<>();

        pages.forEach((key, node) -> {
            files.put("normal/" + key, renderer(false, config).render(node));
            files.put("advanced/" + key, renderer(true, config).render(node));
        });

        return files;
    }

    private static byte[] readChunks(AbstractServer aliServer) {
        List<LootDataChunkMessage> chunks = aliServer.getChunks();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (chunks.isEmpty()) {
            throw new GameTestAssertException("Server produced no loot data");
        }

        for (LootDataChunkMessage chunk : chunks) {
            bos.writeBytes(chunk.data());
        }

        return bos.toByteArray();
    }

    @NotNull
    private static GoldenRenderer renderer(boolean isAdvanced, AliConfig config) {
        return new GoldenRenderer(isAdvanced, new NumberOptions(isAdvanced, config.showCharts, Locale.ROOT)) {
            @Override
            protected String describe(ICoreDataNode<?> node) {
                return node instanceof IItemNode itemNode ? itemNode.getItem().map(LootDataGolden::stackToString, (tag) -> "#" + tag.location()) : null;
            }
        };
    }

    @NotNull
    private static String stackToString(ItemStack stack) {
        String text = BuiltInRegistries.ITEM.getKey(stack.getItem()) + " x" + stack.getCount();

        return stack.hasTag() ? text + " " + stack.getTag() : text;
    }

    @NotNull
    private static Map<String, String> diff(Map<String, String> defaults, Map<String, String> actual) {
        Map<String, String> files = new TreeMap<>();
        String removed = defaults.keySet().stream().filter((key) -> !actual.containsKey(key)).map((key) -> key + "\n").collect(Collectors.joining());

        actual.forEach((key, value) -> {
            if (!value.equals(defaults.get(key))) {
                files.put(key, value);
            }
        });

        if (!removed.isEmpty()) {
            files.put(REMOVED_FILE, removed);
        }

        return files;
    }

    @NotNull
    private static List<String> compare(String variant, Map<String, String> actual) {
        return GoldenFiles.fromProperties(Utils.MOD_ID).resolve(variant).compare(actual).stream().map((difference) -> variant + ": " + difference).toList();
    }

    public record Variant(String name, Function<AliConfig, Runnable> apply) {
    }
}
