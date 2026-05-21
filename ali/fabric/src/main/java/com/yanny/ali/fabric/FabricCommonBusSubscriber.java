package com.yanny.ali.fabric;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricCommonBusSubscriber {
    private static boolean serverLoaded = false;

    public static void registerEvents(AbstractServer server) {
        ServerWorldEvents.LOAD.register(FabricCommonBusSubscriber::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(FabricCommonBusSubscriber::onServerStopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(FabricCommonBusSubscriber::onReload);
        ServerPlayConnectionEvents.JOIN.register(FabricCommonBusSubscriber::onPlayerLogIn);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloader(Utils.modLoc("fake_loot_loader"), getReloadListener(server));
    }

    private static void onServerStarting(MinecraftServer server, ServerLevel world) {
        if (!serverLoaded) { // to be safe, handle only once for world loading (should be called only once for overworld, but who knows?)
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

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CommonAliMod.SERVER.syncLootTables(player);
            }
        }
    }

    private static void onPlayerLogIn(ServerGamePacketListenerImpl event, PacketSender sender, MinecraftServer server) {
        CommonAliMod.SERVER.syncLootTables(event.player);
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
