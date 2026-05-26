package com.yanny.ali.fabric;

import com.yanny.ali.Utils;
import com.yanny.ali.fabric.platform.FabricPlatformHelper;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricCommonBusSubscriber {
    private static boolean serverLoaded = false;

    public static void registerEvents(AbstractServer server) {
        ServerWorldEvents.LOAD.register(FabricCommonBusSubscriber::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricCommonBusSubscriber::onServerStopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(FabricCommonBusSubscriber::onReload);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(getReloadListener(server));
    }

    private static void onServerStarting(MinecraftServer server, ServerLevel world) {
        if (!serverLoaded) { // to be safe, handle only once for world loading (should be called only once for overworld, but who knows?)
            FabricPlatformHelper.PROVIDER = server.registryAccess();
            PluginManager.getInstance().registerServerEvent(server.overworld());
            CommonAliMod.SERVER.readLootTables(server.reloadableRegistries());
            serverLoaded = true;
        }
    }

    private static void onServerStopping(MinecraftServer server) {
        serverLoaded = false;
        PluginManager.getInstance().deregisterServerEvent();
    }

    private static void onReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        if (success) {
            PluginManager.getInstance().reloadServer();
            CommonAliMod.SERVER.readLootTables(server.reloadableRegistries());
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
