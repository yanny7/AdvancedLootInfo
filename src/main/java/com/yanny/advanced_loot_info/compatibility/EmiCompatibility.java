package com.yanny.advanced_loot_info.compatibility;

import com.yanny.advanced_loot_info.EmiLootMod;
import com.yanny.advanced_loot_info.Utils;
import com.yanny.advanced_loot_info.network.LootGroup;
import com.yanny.advanced_loot_info.network.NetworkUtils;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    public static final ResourceLocation TEXTURE = Utils.modLoc("textures/gui/emi.png");

    @Override
    public void register(EmiRegistry emiRegistry) {
        registerLootTable(emiRegistry);
    }

    private void registerLootTable(EmiRegistry registry) {
        NetworkUtils.Client client = EmiLootMod.INFO_PROPAGATOR.client();
        ClientLevel level = Minecraft.getInstance().level;

        if (client != null && level != null) {
            Map<ResourceLocation, LootGroup> map = new HashMap<>(client.lootEntries.stream().collect(Collectors.toMap((l) -> l.location, l -> l.value)));

            for (Block block : ForgeRegistries.BLOCKS) {
                ResourceLocation location = block.getLootTable();
                LootGroup lootEntry = map.get(location);

                if (lootEntry != null) {
                    registry.addCategory(EmiBlockLoot.CATEGORY);
                    registry.addRecipe(new EmiBlockLoot(new ResourceLocation(location.getNamespace(), "/" + location.getPath()), block, lootEntry));
                    map.remove(location);
                }
            }

            for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES) {
                ResourceLocation location = entityType.getDefaultLootTable();
                LootGroup lootEntry = map.get(location);

                if (lootEntry != null && entityType.create(level) != null) {
                    registry.addCategory(EmiEntityLoot.CATEGORY);
                    registry.addRecipe(new EmiEntityLoot(new ResourceLocation(location.getNamespace(), "/" + location.getPath()), entityType, lootEntry));
                    map.remove(location);
                }
            }

            for (Map.Entry<ResourceLocation, LootGroup> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();
                registry.addCategory(EmiGameplayLoot.CATEGORY);
                registry.addRecipe(new EmiGameplayLoot(new ResourceLocation(location.getNamespace(), "/" + location.getPath()), entry.getValue()));
            }
        }
    }
}
