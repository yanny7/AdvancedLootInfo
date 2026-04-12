package com.yanny.ali.fabric;

import com.yanny.ali.Utils;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.network.AbstractServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
        ServerPlayConnectionEvents.JOIN.register(FabricCommonBusSubscriber::onPlayerLogIn);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(Utils.MOD_ID, "fake_loot_loader");
            }

            @NotNull
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
                return server.getFakeLootDataManager().reload(preparationBarrier,  resourceManager, profilerFiller, profilerFiller2, executor, executor2);
            }
        });
    }

    private static void onServerStarting(MinecraftServer server, ServerLevel world) {
        if (!serverLoaded) { // to be safe, handle only once for world loading (should be called only once for overworld, but who knows?)
            PluginManager.registerServerEvent();
            CommonAliMod.SERVER.readLootTables(server.getLootData(), server.overworld());
            serverLoaded = true;
        }
    }

    private static void onServerStopping(MinecraftServer server) {
        serverLoaded = false;
        PluginManager.deregisterServerEvent();
    }

    private static void onReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        if (success) {
            PluginManager.reloadServer();
            CommonAliMod.SERVER.readLootTables(server.getLootData(), server.overworld());

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CommonAliMod.SERVER.syncLootTables(player);
            }
        }
    }

    private static void onPlayerLogIn(ServerGamePacketListenerImpl event, PacketSender sender, MinecraftServer server) {
        CommonAliMod.SERVER.syncLootTables(event.player);
    }
}
