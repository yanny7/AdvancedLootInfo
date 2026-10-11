package com.yanny.ali.gametest;

import com.yanny.aci.gametest.GoldenFiles;
import com.yanny.ali.Utils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public class LootDataGolden {
    private static final String DEFAULT_VARIANT = "default";
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
        Map<String, String> defaults = LootDataCapture.capture(aliServer, config, server.registryAccess());
        List<String> differences = new ArrayList<>(compare(DEFAULT_VARIANT, defaults));
        GameTestSequence sequence = helper.startSequence();

        for (Variant variant : Stream.concat(VARIANTS.stream(), loaderVariants.stream()).toList()) {
            Runnable[] revert = new Runnable[1];

            reload(sequence, server, () -> revert[0] = variant.apply().apply(config));
            sequence.thenExecute(() -> {
                Map<String, String> changed = GoldenFiles.diff(defaults, LootDataCapture.capture(aliServer, config, server.registryAccess()));

                revert[0].run();

                if (changed.isEmpty()) {
                    differences.add(variant.name() + ": output is identical to " + DEFAULT_VARIANT);
                }

                differences.addAll(compare(variant.name(), changed));
            });
        }

        reload(sequence, server, () -> {});
        sequence.thenExecute(() -> {
            if (!defaults.equals(LootDataCapture.capture(aliServer, config, server.registryAccess()))) {
                differences.add("output after a datapack reload differs from the output of the startup scan");
            }

            differences.addAll(scriptErrors());

            if (!differences.isEmpty()) {
                throw new GameTestAssertException(differences.size() + " golden difference(s): " + differences.subList(0, Math.min(10, differences.size())));
            }
        });
        sequence.thenSucceed();
    }

    @Unmodifiable
    @NotNull
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
    private static List<String> compare(String variant, Map<String, String> actual) {
        return GoldenFiles.fromProperties(Utils.MOD_ID).resolve(variant).compare(actual).stream().map((difference) -> variant + ": " + difference).toList();
    }

    public record Variant(String name, Function<AliConfig, Runnable> apply) {
    }
}
