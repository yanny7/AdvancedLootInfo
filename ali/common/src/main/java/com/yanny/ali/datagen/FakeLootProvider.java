package com.yanny.ali.datagen;

import net.minecraft.advancements.critereon.*;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
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

    public FakeLootProvider(PackOutput output) {
        pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "fake_loot");
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Map<ResourceLocation, LootTable.Builder> tables = collectLootTables();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        tables.forEach((location, builder) -> {
            Path path = pathProvider.json(location);

            futures.add(DataProvider.saveStable(cachedOutput, LootDataType.TABLE.parser().toJsonTree(builder.build()), path));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @NotNull
    @Override
    public String getName() {
        return "Fake Loot Tables";
    }

    @NotNull
    private Map<ResourceLocation, LootTable.Builder> collectLootTables() {
        Map<ResourceLocation, LootTable.Builder> map = new HashMap<>();

        registerLoot(map, "entities/fox",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedAlwaysDropItem(Items.EMERALD),
                                addEquippedAlwaysDropItem(Items.RABBIT_FOOT),
                                addEquippedAlwaysDropItem(Items.RABBIT_HIDE),
                                addEquippedAlwaysDropItem(Items.EGG),
                                addEquippedAlwaysDropItem(Items.WHEAT),
                                addEquippedAlwaysDropItem(Items.LEATHER),
                                addEquippedAlwaysDropItem(Items.FEATHER)
                        ))
                )
        );
        registerLoot(map, "entities/wandering_trader",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedItem(Items.MILK_BUCKET, EquipmentSlot.MAINHAND),
                                addEquippedItem(Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.INVISIBILITY)))
                        )
                )
        );
        registerLoot(map, "entities/creeper",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addChargedCreeperDropItem(Items.CREEPER_HEAD)
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
                                addChargedCreeperDropItem(Items.PIGLIN_HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.CROSSBOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/zombified_piglin",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_AXE, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.CROSSBOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.WARPED_FUNGUS_ON_A_STICK, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/drowned",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(Items.TRIDENT, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.FISHING_ROD, EquipmentSlot.MAINHAND),
                                addEquippedAlwaysDropItem(Items.NAUTILUS_SHELL)
                        ))
                )
        );
        registerLoot(map, "entities/pillager",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addEquippedEnchantedAndDamagedItem(Items.CROSSBOW, EquipmentSlot.MAINHAND)
                        )
                )
        );
        registerLoot(map, "entities/skeleton",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addChargedCreeperDropItem(Items.SKELETON_SKULL),
                                addEquippedEnchantedAndDamagedItem(Items.BOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/stray",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(Items.BOW, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/vindicator",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(
                                addEquippedEnchantedAndDamagedItem(Items.IRON_AXE, EquipmentSlot.MAINHAND)
                        )
                )
        );
        registerLoot(map, "entities/witch",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedItem(Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.HEALING)),
                                addEquippedItem(Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)),
                                addEquippedItem(Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.SWIFTNESS)),
                                addEquippedItem(Items.POTION, EquipmentSlot.MAINHAND).apply(SetPotionFunction.setPotion(Potions.WATER_BREATHING))
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
                                addEquippedEnchantedAndDamagedItem(Items.STONE_SWORD, EquipmentSlot.MAINHAND)
                        )
                )
        );
        registerLoot(map, "entities/zombie",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addChargedCreeperDropItem(Items.ZOMBIE_HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_SHOVEL, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/zombie_villager",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(Items.IRON_SHOVEL, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );
        registerLoot(map, "entities/husk",
                LootTable.lootTable().withPool(
                        LootPool.lootPool().add(AlternativesEntry.alternatives(
                                addEquippedEnchantedAndDamagedItem(Items.IRON_SHOVEL, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_SWORD, EquipmentSlot.MAINHAND),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.LEATHER_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.GOLDEN_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.CHAINMAIL_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.IRON_BOOTS, EquipmentSlot.FEET),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_HELMET, EquipmentSlot.HEAD),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_CHESTPLATE, EquipmentSlot.CHEST),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_LEGGINGS, EquipmentSlot.LEGS),
                                addEquippedEnchantedAndDamagedItem(Items.DIAMOND_BOOTS, EquipmentSlot.FEET)
                        ))
                )
        );

        return map;
    }

    private static void registerLoot(Map<ResourceLocation, LootTable.Builder> map, String name, LootTable.Builder builder) {
        map.put(new ResourceLocation(name), builder);
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addItem(ItemLike item) {
        return LootItem.lootTableItem(item).when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.085F, 0.01F));
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addEquippedAlwaysDropItem(ItemLike item) {
        return LootItem.lootTableItem(item).when(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().equipment(
                                EntityEquipmentPredicate.Builder.equipment().mainhand(
                                        ItemPredicate.Builder.item().of(item).build()
                                ).build()
                        )
                )
        );
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addEquippedItem(ItemLike item, EquipmentSlot slot) {
        ItemPredicate predicate = ItemPredicate.Builder.item().of(item).build();

        return addItem(item).when(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().equipment(
                                switch (slot) {
                                    case MAINHAND -> EntityEquipmentPredicate.Builder.equipment().mainhand(predicate).build();
                                    case OFFHAND -> EntityEquipmentPredicate.Builder.equipment().offhand(predicate).build();
                                    case FEET -> EntityEquipmentPredicate.Builder.equipment().feet(predicate).build();
                                    case LEGS -> EntityEquipmentPredicate.Builder.equipment().legs(predicate).build();
                                    case CHEST -> EntityEquipmentPredicate.Builder.equipment().chest(predicate).build();
                                    case HEAD -> EntityEquipmentPredicate.Builder.equipment().head(predicate).build();
                                }
                        )
                )
        );
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addEquippedEnchantedAndDamagedItem(ItemLike item, EquipmentSlot slot) {
        float enchantChance = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND ? 0.25F : 0.5F;

        return addEquippedItem(item, slot)
                .apply(EnchantRandomlyFunction.randomEnchantment().when(LootItemRandomChanceCondition.randomChance(enchantChance)))
                .apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.0F, 1.0F)));
    }

    @NotNull
    private static LootPoolSingletonContainer.Builder<?> addChargedCreeperDropItem(ItemLike item) {
        CompoundTag powered = new CompoundTag();

        powered.putBoolean("powered", true);
        return LootItem.lootTableItem(item).when(
                LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.KILLER,
                        EntityPredicate.Builder.entity()
                                .entityType(EntityTypePredicate.of(EntityType.CREEPER))
                                .nbt(new NbtPredicate(powered))
                )
        );
    }
}
