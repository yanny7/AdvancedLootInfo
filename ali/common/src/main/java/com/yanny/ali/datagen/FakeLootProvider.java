package com.yanny.ali.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FakeLootProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registryLookup;

    public FakeLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "fake_loot");
        this.registryLookup = registryLookup;
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return registryLookup.thenCompose((provider) -> {
            Map<Identifier, LootTable.Builder> tables = collectLootTables(provider);
            List<CompletableFuture<?>> futures = new ArrayList<>();
            RegistryOps<JsonElement> registryOps = provider.createSerializationContext(JsonOps.INSTANCE);

            tables.forEach((location, builder) -> {
                Path path = pathProvider.json(location);

                futures.add(DataProvider.saveStable(cachedOutput, LootDataType.TABLE.codec().encodeStart(registryOps, builder.build()).getOrThrow(), path));
            });

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @NotNull
    @Override
    public String getName() {
        return "Fake Loot Tables";
    }

    @NotNull
    private Map<Identifier, LootTable.Builder> collectLootTables(HolderLookup.Provider provider) {
        Map<Identifier, LootTable.Builder> map = new HashMap<>();

        registerLoot(map, "entities/fox",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedAlwaysDropItem(provider, Items.EMERALD),
                                addEquippedAlwaysDropItem(provider, Items.RABBIT_FOOT),
                                addEquippedAlwaysDropItem(provider, Items.RABBIT_HIDE),
                                addEquippedAlwaysDropItem(provider, Items.EGG),
                                addEquippedAlwaysDropItem(provider, Items.WHEAT),
                                addEquippedAlwaysDropItem(provider, Items.LEATHER),
                                addEquippedAlwaysDropItem(provider, Items.FEATHER)
                        ))
                )
        );
        registerLoot(map, "entities/wandering_trader",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedItem(provider, Items.MILK_BUCKET, EquipmentSlot.MAINHAND),
                                addEquippedItem(provider, Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.INVISIBILITY)))
                        )
                )
        );
        registerLoot(map, "entities/creeper",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addChargedCreeperDropItem(provider, Items.CREEPER_HEAD)
                        )
                )
        );
//        registerLoot(map, "entities/goat",
//                LootTable.lootTable().withPool(
//                        LootPool.lootPool().add(
//                                LootItem.lootTableItem(Items.GOAT_HORN)
//                        ) // FIXME add ram into block predicate (block has tag)
//                )
//        );
        registerLoot(map, "entities/piglin",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addChargedCreeperDropItem(provider, Items.PIGLIN_HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CROSSBOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/zombified_piglin",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_AXE, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CROSSBOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.WARPED_FUNGUS_ON_A_STICK, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/drowned",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(provider, Items.TRIDENT, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.FISHING_ROD, EquipmentSlot.MAINHAND),
                                addEquippedAlwaysDropItem(provider, Items.NAUTILUS_SHELL)
                        ))
                )
        );
        registerLoot(map, "entities/pillager",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addEquippedEnchantedAndDamagedItem(provider, Items.CROSSBOW, EquipmentSlot.MAINHAND)
                        )
                )
        );
        registerLoot(map, "entities/skeleton",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addChargedCreeperDropItem(provider, Items.SKELETON_SKULL),
                                addEquippedEnchantedAndDamagedItem(provider, Items.BOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/bogged",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(provider, Items.BOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/stray",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(provider, Items.BOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/vindicator",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_AXE, EquipmentSlot.MAINHAND)
                        )
                )
        );
        registerLoot(map, "entities/witch",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedItem(provider, Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.HEALING)),
                                addEquippedItem(provider, Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)),
                                addEquippedItem(provider, Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.SWIFTNESS)),
                                addEquippedItem(provider, Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.WATER_BREATHING))
                        ))
                )
        );
        registerLoot(map, "entities/wither",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                LootItem.lootTableItem(Items.NETHER_STAR)
                        )
                )
        );
        registerLoot(map, "entities/wither_skeleton",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addEquippedEnchantedAndDamagedItem(provider, Items.STONE_SWORD, EquipmentSlot.MAINHAND)
                        )
                )
        );
        registerLoot(map, "entities/zombie",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addChargedCreeperDropItem(provider, Items.ZOMBIE_HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_SHOVEL, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/zombie_villager",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_SHOVEL, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/husk",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_SHOVEL, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(provider, Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );

        return map;
    }

    private static void registerLoot(Map<Identifier, LootTable.Builder> map, String name, LootTable.Builder builder) {
        map.put(Identifier.withDefaultNamespace(name), builder);
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addItem(HolderLookup.Provider provider, ItemLike item) {
        return LootItem.lootTableItem(item).when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(provider, 0.085F, 0.01F));
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addEquippedAlwaysDropItem(HolderLookup.Provider provider, ItemLike item) {
        return LootItem.lootTableItem(item).when(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().equipment(
                                EntityEquipmentPredicate.Builder.equipment().mainhand(
                                        ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), item)
                                ).build()
                        )
                )
        );
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addEquippedItem(HolderLookup.Provider provider, ItemLike item, EquipmentSlot slot) {
        ItemPredicate.Builder predicate = ItemPredicate.Builder.item().of(provider.lookupOrThrow(Registries.ITEM), item);

        return addItem(provider, item).when(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().equipment(
                                switch (slot) {
                                    case MAINHAND -> EntityEquipmentPredicate.Builder.equipment().mainhand(predicate).build();
                                    case OFFHAND -> EntityEquipmentPredicate.Builder.equipment().offhand(predicate).build();
                                    case FEET -> EntityEquipmentPredicate.Builder.equipment().feet(predicate).build();
                                    case LEGS -> EntityEquipmentPredicate.Builder.equipment().legs(predicate).build();
                                    case CHEST -> EntityEquipmentPredicate.Builder.equipment().chest(predicate).build();
                                    case HEAD -> EntityEquipmentPredicate.Builder.equipment().head(predicate).build();
                                    case BODY ->  EntityEquipmentPredicate.Builder.equipment().body(predicate).build();
                                    case SADDLE -> throw new IllegalStateException();
                                }
                        )
                )
        );
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addEquippedEnchantedAndDamagedItem(HolderLookup.Provider provider, ItemLike item, EquipmentSlot slot) {
        float enchantChance = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND ? 0.25F : 0.5F;

        return addEquippedItem(provider, item, slot)
                .apply(EnchantRandomlyFunction.randomEnchantment().when(LootItemRandomChanceCondition.randomChance(enchantChance)))
                .apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.0F, 1.0F)));
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addChargedCreeperDropItem(HolderLookup.Provider provider, ItemLike item) {
        CompoundTag powered = new CompoundTag();

        powered.putBoolean("powered", true);
        return LootItem.lootTableItem(item).when(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.ATTACKER,
                        EntityPredicate.Builder.entity()
                                .entityType(EntityTypePredicate.of(provider.lookupOrThrow(Registries.ENTITY_TYPE), EntityTypes.CREEPER))
                                .nbt(new NbtPredicate(powered))
                )
        );
    }
}
