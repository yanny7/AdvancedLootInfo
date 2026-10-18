package com.yanny.ali.fabric;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricCommonBusSubscriber {
    private static final ResourceLocation AFTER_MODS = Utils.modLoc("after_mods");

    public static void registerEvents(AbstractServer server) {
        ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_MODS);
        ServerLifecycleEvents.SERVER_STARTED.register(AFTER_MODS, FabricCommonBusSubscriber::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricCommonBusSubscriber::onServerStopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(FabricCommonBusSubscriber::onReload);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(getReloadListener(server));
    }

    private static void onServerStarted(MinecraftServer server) {
        PluginManager.getInstance().registerServerEvent(server.overworld());
        CommonAliMod.SERVER.readLootTables(server.getLootData());
    }

    private static void onServerStopping(MinecraftServer server) {
        PluginManager.getInstance().deregisterServerEvent();
    }

    private static void onReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        if (success) {
            PluginManager.getInstance().reloadServer();
            CommonAliMod.SERVER.readLootTables(server.getLootData());
        }
    }

    @NotNull
    private static IdentifiableResourceReloadListener getReloadListener(AbstractServer server) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Utils.modLoc("fake_loot_loader");
            }

            @NotNull
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                return server.getFakeLootDataManager().reload(preparationBarrier,  resourceManager, profilerFiller, profilerFiller2, executor, executor2);
            }
        };
    }
}
