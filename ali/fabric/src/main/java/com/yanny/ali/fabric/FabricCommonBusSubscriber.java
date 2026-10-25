package com.yanny.ali.fabric;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricCommonBusSubscriber {
    private static final Identifier AFTER_MODS = Utils.modLoc("after_mods");

    public static void registerEvents(AbstractServer server) {
        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_MODS);
        ServerLifecycleEvents.SERVER_STARTED.register(AFTER_MODS, FabricCommonBusSubscriber::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricCommonBusSubscriber::onServerStopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(FabricCommonBusSubscriber::onReload);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(Utils.modLoc("fake_loot_loader"), getReloadListener(server));
    }

    private static void onServerStarted(MinecraftServer server) {
        PluginManager.getInstance().registerServerEvent(server.overworld());
        CommonAliMod.SERVER.readLootTables(server.reloadableRegistries());
    }

    private static void onServerStopping(MinecraftServer server) {
        PluginManager.getInstance().deregisterServerEvent();
    }

    private static void onReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        if (success) {
            PluginManager.getInstance().reloadServer();
            CommonAliMod.SERVER.readLootTables(server.reloadableRegistries());
        }
    }

    @NotNull
    private static PreparableReloadListener getReloadListener(AbstractServer server) {
        return new PreparableReloadListener() {
            @NotNull
            @Override
            public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
                return server.getFakeLootDataManager(sharedState.get(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY)).reload(sharedState, executor, preparationBarrier, executor2);
            }
        };
    }
}
