package com.yanny.emi_loot_addon.compatibility;

import com.yanny.emi_loot_addon.EmiLootMod;
import com.yanny.emi_loot_addon.Utils;
import com.yanny.emi_loot_addon.network.LootGroup;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@EmiEntrypoint
public class EmiCompatibility implements EmiPlugin {
    public static final ResourceLocation TEXTURE = Utils.modLoc("textures/gui/emi.png");

    @Override
    public void register(EmiRegistry emiRegistry) {
        registerLootTable(emiRegistry);
    }

    private void registerLootTable(EmiRegistry registry) {
        Map<ResourceLocation, LootGroup> map = new HashMap<>(EmiLootMod.INFO_PROPAGATOR.client().lootEntries.stream().collect(Collectors.toMap((l) -> l.location, l -> l.value)));

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

            if (lootEntry != null && entityType.create(Minecraft.getInstance().level) != null) {
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

    public static <T extends Recipe<?>> ResourceLocation ref(RegistryObject<RecipeType<T>> recipeType) {
        return new ResourceLocation(recipeType.get().toString());
    }
}
