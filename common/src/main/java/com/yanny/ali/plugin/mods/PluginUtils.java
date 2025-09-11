package com.yanny.ali.plugin.mods;

import com.mojang.logging.LogUtils;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class PluginUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends IFunctionTooltip> void registerFunctionTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemFunction> functionClass = (Class<LootItemFunction>) Class.forName(classAnnotation.value());
                registry.registerFunctionTooltip(functionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register function tooltip for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for function tooltip " + clazz.getName());
        }
    }

    public static <T extends IConditionTooltip> void registerConditionTooltip(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootItemCondition> conditionClass = (Class<LootItemCondition>) Class.forName(classAnnotation.value());
                registry.registerConditionTooltip(conditionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).getTooltip(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register condition tooltip for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for condition tooltip " + clazz.getName());
        }
    }

    public static <T extends IEntry> void registerEntry(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<LootPoolEntryContainer> conditionClass = (Class<LootPoolEntryContainer>) Class.forName(classAnnotation.value());
                registry.registerEntry(conditionClass, (u, e, r, w, f, c) -> ReflectionUtils.copyClassData(clazz, e).create(u, r, w, f, c));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register entry for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for entry " + clazz.getName());
        }
    }

    public static <T extends IItemListing> void registerItemListing(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<VillagerTrades.ItemListing> conditionClass = (Class<VillagerTrades.ItemListing>) Class.forName(classAnnotation.value());
                registry.registerItemListing(conditionClass, (u, c, t) -> ReflectionUtils.copyClassData(clazz, c).getNode(u, t));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register item listing for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("Missing ClassAccessor annotation for item listing {}" + clazz.getName());
        }
    }

    public static <T extends IItemListing> void registerItemListingCollector(IServerRegistry registry, Class<T> clazz) {
        ClassAccessor classAnnotation = clazz.getAnnotation(ClassAccessor.class);

        if (classAnnotation != null) {
            try {
                //noinspection unchecked
                Class<VillagerTrades.ItemListing> conditionClass = (Class<VillagerTrades.ItemListing>) Class.forName(classAnnotation.value());
                registry.registerItemListingCollector(conditionClass, (u, c) -> ReflectionUtils.copyClassData(clazz, c).collectItems(u));
            } catch (Throwable e) {
                LOGGER.warn("Failed to register item listing collector for {} with error {}", classAnnotation.value(), e.getMessage());
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Missing ClassAccessor annotation for item listing collector {}", clazz.getName());
        }
    }

    public static <T extends ItemLike> List<Item> getItems(IServerUtils utils, TagKey<T> tag) {
        ServerLevel level = utils.getServerLevel();

        if (level != null) {
            Registry<T> registry = level.registryAccess().registryOrThrow(tag.registry());

            return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false).map(Holder::value).map(ItemLike::asItem).toList();
        }

        return Collections.emptyList();
    }
}
