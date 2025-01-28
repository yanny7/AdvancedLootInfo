package com.yanny.advanced_loot_info.network;

import com.mojang.logging.LogUtils;
import com.yanny.advanced_loot_info.api.ILootCondition;
import com.yanny.advanced_loot_info.api.ILootFunction;
import com.yanny.advanced_loot_info.manager.PluginManager;
import com.yanny.advanced_loot_info.mixin.*;
import com.yanny.advanced_loot_info.plugin.function.FurnaceSmeltFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class LootUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation UNKNOWN = new ResourceLocation("unknown");

    @NotNull
    public static LootTableEntry parseLoot(LootTable table, LootDataManager manager, LootContext lootContext, List<Item> items, float chance, int quality) {
        MixinLootTable lootTable = (MixinLootTable) table;
        List<LootPoolEntry> lootInfos = new LinkedList<>();
        List<LootPool> pools = lootTable.getPools();

        boolean wasSmelting = Arrays.stream(lootTable.getFunctions()).anyMatch((f) -> f.getType() == LootItemFunctions.FURNACE_SMELT);

        pools.forEach((pool) -> lootInfos.add(parsePool(pool, manager, lootContext, items, wasSmelting)));
        return new LootTableEntry(lootInfos, ofFunction(lootContext, lootTable.getFunctions()), chance, quality);
    }

    @NotNull
    private static LootPoolEntry parsePool(LootPool pool, LootDataManager manager, LootContext context, List<Item> items, boolean wasSmelting) {
        MixinLootPool mixinLootPool = (MixinLootPool) pool;
        RangeValue rolls = RangeValue.of(context, mixinLootPool.getRolls());
        RangeValue bonusRolls = RangeValue.of(context, mixinLootPool.getBonusRolls());
        List<ILootFunction> functions = ofFunction(context, mixinLootPool.getFunctions());
        List<ILootCondition> conditions = ofCondition(context, mixinLootPool.getConditions());

        wasSmelting |= functions.stream().anyMatch((f) -> f instanceof FurnaceSmeltFunction);

        return new LootPoolEntry(parseEntries(mixinLootPool.getEntries(), manager, context, items, wasSmelting), rolls, bonusRolls, functions, conditions);
    }

    @NotNull
    private static List<LootEntry> parseEntries(LootPoolEntryContainer[] entries, LootDataManager manager, LootContext lootContext, List<Item> items, boolean wasSmelting) {
        List<LootEntry> lootInfos = new LinkedList<>();
        int sumWeight = Arrays.stream(entries).filter(entry -> entry instanceof LootPoolSingletonContainer).mapToInt(entry -> ((MixinLootPoolSingletonContainer) entry).getWeight()).sum();

        Function<Integer, Float> getChance = (weight) -> {
            if (weight > 0) {
                return weight / (float) sumWeight;
            } else {
                return Float.NaN;
            }
        };

        for (LootPoolEntryContainer entry : entries) {
            MixinLootPoolEntryContainer mixinLootPool = (MixinLootPoolEntryContainer) entry;
            LootPoolEntryType type = entry.getType();
            List<ILootFunction> functions = List.of();
            List<ILootCondition> conditions = ofCondition(lootContext, mixinLootPool.getConditions());
            int poolWeight = 0;
            int poolQuality = 0;

            if (entry instanceof LootPoolSingletonContainer singletonContainer) {
                MixinLootPoolSingletonContainer poolEntry = (MixinLootPoolSingletonContainer) singletonContainer;
                functions = ofFunction(lootContext, poolEntry.getFunctions());
                poolWeight = poolEntry.getWeight();
                poolQuality = poolEntry.getQuality();
            }

            float chance = getChance.apply(poolWeight);

            //noinspection StatementWithEmptyBody
            if (type == LootPoolEntries.EMPTY) {
                // Skip empty entry
            } else if (type == LootPoolEntries.REFERENCE) {
                ResourceLocation location = ((MixinLootTableReference) entry).getName();
                LootTable table = manager.getElement(LootDataType.TABLE, location);

                if (table != null) {
                    lootInfos.add(parseLoot(table, manager, lootContext, items, chance, poolQuality));
                } else {
                    LOGGER.warn("Invalid LootTable reference {}", location);
                }
            } else if (type == LootPoolEntries.DYNAMIC) {
                LOGGER.warn("Unimplemented dynamic loot entry, skipping");
            } else if (type == LootPoolEntries.TAG) {
                if (entry instanceof TagEntry) {
                    lootInfos.addAll(parseTagEntry((TagEntry) entry, lootContext, items, chance, wasSmelting));
                } else {
                    LOGGER.warn("Invalid entry type, expecting TagEntry, found {}", entry.getClass().getName());
                }
            } else if (type == LootPoolEntries.GROUP) {
                lootInfos.add(new LootGroup(GroupType.ALL, parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, items, wasSmelting), functions, conditions, chance, poolQuality));
            } else if (type == LootPoolEntries.SEQUENCE) {
                lootInfos.add(new LootGroup(GroupType.SEQUENCE, parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, items, wasSmelting), functions, conditions, chance, poolQuality));
            } else if (type == LootPoolEntries.ALTERNATIVES) {
                lootInfos.add(new LootGroup(GroupType.ALTERNATIVES, parseEntries(((MixinCompositeEntryBase) entry).getChildren(), manager, lootContext, items, wasSmelting), functions, conditions, chance, poolQuality));
            } else if (type == LootPoolEntries.ITEM) {
                if (entry instanceof net.minecraft.world.level.storage.loot.entries.LootItem lootItem) {
                    lootInfos.addAll(parseLootItem(lootItem, lootContext, items, chance, wasSmelting));
                } else {
                    LOGGER.warn("Invalid entry type, expecting LootItem, found {}", entry.getClass().getName());
                }
            } else {
                LOGGER.warn("Unknown LootPool entry type {}", type);
            }
        }

        return lootInfos;
    }

    @NotNull
    private static List<LootEntry> parseLootItem(net.minecraft.world.level.storage.loot.entries.LootItem lootItem, LootContext lootContext, List<Item> items, float chance, boolean wasSmelting) {
        List<LootEntry> lootInfos = new LinkedList<>();
        Item item = ((MixinLootItem) lootItem).getItem();
        Tuple<LootItemFunction[], LootItemCondition[]> tuple = handleSmeltingFunction(lootItem, lootContext, items, lootInfos, item, chance, wasSmelting);

        if (!wasSmelting) {
            items.add(item);
            lootInfos.add(new LootItem(
                    ForgeRegistries.ITEMS.getKey(item),
                    ofFunction(lootContext, tuple.getA()),
                    ofCondition(lootContext, tuple.getB()),
                    chance
            ));
        }

        return lootInfos;
    }

    @NotNull
    private static List<LootEntry> parseTagEntry(TagEntry entry, LootContext lootContext, List<Item> items, float chance, boolean wasSmelting) {
        List<LootEntry> lootInfos = new LinkedList<>();
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();

        if (wasSmelting) {
            if (tags != null) {
                for (Item item : tags.getTag(((MixinTagEntry) entry).getTag())) {
                    Tuple<LootItemFunction[], LootItemCondition[]> tuple = handleSmeltingFunction(entry, lootContext, items, lootInfos, item, chance, wasSmelting);

                    if (!wasSmelting) {
                        items.add(item);
                        lootInfos.add(new LootItem(
                                ForgeRegistries.ITEMS.getKey(item),
                                ofFunction(lootContext, tuple.getA()),
                                ofCondition(lootContext, tuple.getB()),
                                chance
                        ));
                    }
                }
            }
        } else {
            MixinTagEntry tagEntry = (MixinTagEntry) entry;
            LootItemFunction[] functions = ((MixinLootPoolSingletonContainer) entry).getFunctions();
            LootItemCondition[] conditions = ((MixinLootPoolEntryContainer) entry).getConditions();
            lootInfos.add(new LootTag(tagEntry.getTag(), ofFunction(lootContext, functions), ofCondition(lootContext, conditions), chance));
        }

        return lootInfos;
    }

    @NotNull
    private static Tuple<LootItemFunction[], LootItemCondition[]> handleSmeltingFunction(LootPoolSingletonContainer container, LootContext lootContext, List<Item> items,
                                                                                         List<LootEntry> lootInfos, Item item, float chance, boolean wasSmelting) {
        LootItemFunction[] functions = ((MixinLootPoolSingletonContainer) container).getFunctions();
        LootItemCondition[] conditions = ((MixinLootPoolEntryContainer) container).getConditions();
        Optional<LootItemFunction> optional = Arrays.stream(functions).filter((f) -> f.getType() == LootItemFunctions.FURNACE_SMELT).findAny();

        if (optional.isPresent() || wasSmelting) {
            Optional<SmeltingRecipe> optionalSmeltingRecipe = lootContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack(item)), lootContext.getLevel());

            if (optionalSmeltingRecipe.isPresent()) {
                SmeltingRecipe recipe = optionalSmeltingRecipe.get();
                Item smeltItem = recipe.getResultItem(null).getItem();

                items.add(smeltItem);

                if (!wasSmelting) {
                    LootItemCondition[] smeltConditions = ((MixinLootItemConditionalFunction) optional.get()).getPredicates();

                    functions = Arrays.stream(functions).filter((f) -> f.getType() != LootItemFunctions.FURNACE_SMELT).toArray(LootItemFunction[]::new);
                    lootInfos.add(new LootItem(
                            ForgeRegistries.ITEMS.getKey(smeltItem),
                            ofFunction(lootContext, functions),
                            ofCondition(lootContext, Stream.concat(Arrays.stream(conditions), Arrays.stream(smeltConditions)).toArray(LootItemCondition[]::new)),
                            chance
                    ));
                    conditions = Stream.concat(Arrays.stream(conditions), Arrays.stream(
                            new LootItemCondition[]{new InvertedLootItemCondition(new net.minecraft.world.level.storage.loot.predicates.AllOfCondition(smeltConditions))}
                    )).toArray(LootItemCondition[]::new);
                } else {
                    lootInfos.add(new LootItem(
                            ForgeRegistries.ITEMS.getKey(smeltItem),
                            ofFunction(lootContext, functions),
                            ofCondition(lootContext, conditions),
                            chance
                    ));
                }
            }
        }

        return new Tuple<>(functions, conditions);
    }

    @NotNull
    private static List<ILootFunction> ofFunction(LootContext lootContext, LootItemFunction[] functions) {
        List<ILootFunction> list = new LinkedList<>();

        for (LootItemFunction function : functions) {
            list.add(PluginManager.REGISTRY.getFunction(lootContext, function));
        }

        return list;
    }

    @NotNull
    public static List<ILootFunction> decodeFunction(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootFunction> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            ResourceLocation key = buf.readResourceLocation();
            list.add(PluginManager.REGISTRY.getFunction(key, buf));
        }

        return list;
    }

    public static void encodeFunction(FriendlyByteBuf buf, List<ILootFunction> list) {
        buf.writeInt(list.size());
        list.forEach((f) -> {
            ResourceLocation key = PluginManager.REGISTRY.functionClassMap.getOrDefault(f.getClass(), UNKNOWN);
            buf.writeResourceLocation(key);
            f.encode(buf);
        });
    }

    @NotNull
    public static List<ILootCondition> ofCondition(LootContext lootContext, LootItemCondition[] conditions) {
        List<ILootCondition> list = new LinkedList<>();

        for (LootItemCondition condition : conditions) {
            list.add(PluginManager.REGISTRY.getCondition(lootContext, condition));
        }

        return list;
    }

    @NotNull
    public static List<ILootCondition> decodeCondition(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ILootCondition> list = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            ResourceLocation key = buf.readResourceLocation();
            list.add(PluginManager.REGISTRY.getCondition(key, buf));
        }

        return list;
    }

    public static void encodeCondition(FriendlyByteBuf buf, List<ILootCondition> list) {
        buf.writeInt(list.size());
        list.forEach((f) -> {
            ResourceLocation key = PluginManager.REGISTRY.conditionClassMap.getOrDefault(f.getClass(), UNKNOWN);
            buf.writeResourceLocation(key);
            f.encode(buf);
        });
    }
}
