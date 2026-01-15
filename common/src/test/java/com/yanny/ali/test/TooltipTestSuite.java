package com.yanny.ali.test;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.yanny.ali.api.*;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.manager.PluginManager;
import com.yanny.ali.plugin.server.LootConditionTypes;
import com.yanny.ali.plugin.server.LootFunctionTypes;
import com.yanny.ali.test.utils.TestUtils;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import oshi.util.tuples.Pair;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Suite
@SelectClasses({
        GenericTooltipTest.class,
        RegistriesTooltipTest.class,
        ConditionTooltipTest.class,
        FunctionTooltipTest.class,
        EntryTooltipTest.class,
        EntitySubPredicateTooltipTest.class,
        ItemSubPredicateTooltipTest.class,
        DataComponentTooltipTest.class,
        ConsumeEffectTooltipTest.class,
        IngredientTooltipTest.class,
        TooltipTest.class,
        ServerUtilsTest.class
})
public class TooltipTestSuite {
    public static IServerUtils UTILS;
    public static HolderLookup.Provider LOOKUP;

    private static Set<String> UNUSED;
    private static final Logger LOGGER = LogUtils.getLogger();

    @BeforeSuite
    static void beforeAllTests() throws NoSuchFieldException, IllegalAccessException {
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();

        injectLootFunction();
        injectLootCondition();

        ResourceManager resourceManager = loadClientResources();
        Pair<Language, Set<String>> pair = TestUtils.loadDefaultLanguage(resourceManager);

        Language.inject(pair.getA());
        UNUSED = pair.getB();
        LOOKUP = VanillaRegistries.createLookup();

        PluginManager.registerCommonEvent();
        PluginManager.registerClientEvent();
        PluginManager.registerServerEvent();
        UTILS = new IServerUtils() {
            @Override
            public List<Entity> createEntities(EntityType<?> type, Level level) {
                return PluginManager.SERVER_REGISTRY.createEntities(type, level);
            }

            @Override
            public <T extends LootPoolEntryContainer> List<Item> collectItems(IServerUtils utils, T entry) {
                return PluginManager.SERVER_REGISTRY.collectItems(utils, entry);
            }

            @Override
            public <T extends LootItemFunction> List<Item> collectItems(IServerUtils utils, List<Item> items, T function) {
                return PluginManager.SERVER_REGISTRY.collectItems(utils, items, function);
            }

            @Override
            public <T extends LootPoolEntryContainer> IServerRegistry.EntryFactory<T> getEntryFactory(IServerUtils utils, T type) {
                return PluginManager.SERVER_REGISTRY.getEntryFactory(utils, type);
            }

            @Override
            public <T extends LootItemFunction> ITooltipNode getFunctionTooltip(IServerUtils utils, T function) {
                return PluginManager.SERVER_REGISTRY.getFunctionTooltip(utils, function);
            }

            @Override
            public <T extends LootItemCondition> ITooltipNode getConditionTooltip(IServerUtils utils, T condition) {
                return PluginManager.SERVER_REGISTRY.getConditionTooltip(utils, condition);
            }

            @Override
            public <T extends Ingredient> ITooltipNode getIngredientTooltip(IServerUtils utils, T ingredient) {
                return PluginManager.SERVER_REGISTRY.getIngredientTooltip(utils, ingredient);
            }

            @Override
            public <T> IKeyTooltipNode getValueTooltip(IServerUtils utils, @Nullable T value) {
                return PluginManager.SERVER_REGISTRY.getValueTooltip(utils, value);
            }

            @Override
            public <T extends ItemSubPredicate> ITooltipNode getItemSubPredicateTooltip(IServerUtils utils, T predicate) {
                return PluginManager.SERVER_REGISTRY.getItemSubPredicateTooltip(utils, predicate);
            }

            @Override
            public <T extends EntitySubPredicate> ITooltipNode getEntitySubPredicateTooltip(IServerUtils utils, T predicate) {
                return PluginManager.SERVER_REGISTRY.getEntitySubPredicateTooltip(utils, predicate);
            }

            @Override
            public ITooltipNode getDataComponentTypeTooltip(IServerUtils utils, DataComponentType<?> type, Object value) {
                return PluginManager.SERVER_REGISTRY.getDataComponentTypeTooltip(utils, type, value);
            }

            @Override
            public <T extends ConsumeEffect> ITooltipNode getConsumeEffectTooltip(IServerUtils utils, T effect) {
                return PluginManager.SERVER_REGISTRY.getConsumeEffectTooltip(utils, effect);
            }

            @Override
            public <T extends LootItemFunction> void applyCountModifier(IServerUtils utils, T function, Map<Holder<Enchantment>, Map<Integer, RangeValue>> count) {
                PluginManager.SERVER_REGISTRY.applyCountModifier(utils, function, count);
            }

            @Override
            public <T extends LootItemCondition> void applyChanceModifier(IServerUtils utils, T condition, Map<Holder<Enchantment>, Map<Integer, RangeValue>> chance) {
                PluginManager.SERVER_REGISTRY.applyChanceModifier(utils, condition, chance);
            }

            @Override
            public <T extends LootItemFunction> ItemStack applyItemStackModifier(IServerUtils utils, T function, ItemStack itemStack) {
                return PluginManager.SERVER_REGISTRY.applyItemStackModifier(utils, function, itemStack);
            }

            @Override
            public <T extends VillagerTrades.ItemListing> IDataNode getItemListing(IServerUtils utils, T entry, ITooltipNode condition) {
                return PluginManager.SERVER_REGISTRY.getItemListing(utils, entry, condition);
            }

            @Override
            public <T extends VillagerTrades.ItemListing> Pair<List<Item>, List<Item>> collectItems(IServerUtils utils, T entry) {
                return PluginManager.SERVER_REGISTRY.collectItems(utils, entry);
            }

            @Override
            public RangeValue convertNumber(IServerUtils utils, @Nullable NumberProvider numberProvider) {
                return PluginManager.SERVER_REGISTRY.convertNumber(utils, numberProvider);
            }

            @Nullable
            @Override
            public ServerLevel getServerLevel() {
                return PluginManager.SERVER_REGISTRY.getServerLevel();
            }

            @Nullable
            @Override
            public LootContext getLootContext() {
                return PluginManager.SERVER_REGISTRY.getLootContext();
            }

            @Nullable
            @Override
            public ResourceLocation getCurrentLootTable() {
                return null;
            }

            @Override
            public LootTable getLootTable(Either<ResourceLocation, LootTable> location) {
                return PluginManager.SERVER_REGISTRY.getLootTable(location);
            }

            @Override
            public AliConfig getConfiguration() {
                AliConfig config = PluginManager.SERVER_REGISTRY.getConfiguration();

                config.showInGameNames = false;
                return config;
            }

            @Nullable
            @Override
            public HolderLookup.Provider lookupProvider() {
                return LOOKUP;
            }
        };

        LOGGER.info("----- Translation keys ({}) -----", UNUSED.size());
    }

    @AfterSuite
    static void afterAllTests() {
        LOGGER.info("----- Unused translation keys ({}) -----", UNUSED.size());
        UNUSED.stream().sorted().forEach(LOGGER::info);
    }

    @NotNull
    private static ResourceManager loadClientResources() {
        LanguageManager languageManager = new LanguageManager("en_us", (lang) -> {});
        ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);

        resourceManager.registerReloadListener(languageManager);

        Path resourcePackDirectory = new File("src/test/resources").toPath();
        ClientPackSource clientpacksource = new ClientPackSource(resourcePackDirectory.resolve("assets"), LevelStorageSource.parseValidator(resourcePackDirectory.resolve("allowed_symlinks.txt")));
        PackRepository resourcePackRepository = new PackRepository(clientpacksource);

        resourcePackRepository.reload();
        resourcePackRepository.setSelected(List.of("vanilla"));

        List<PackResources> list = resourcePackRepository.openAllSelected();
        ReloadInstance reloadinstance = resourceManager.createReload(Util.backgroundExecutor(), Util.backgroundExecutor(), CompletableFuture.completedFuture(Unit.INSTANCE), list);

        CompletableFuture<?> completableFuture = reloadinstance.done();

        try {
            completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to load resources for test setup", e);
        }

        return resourceManager;
    }

    private static void injectLootFunction() {
        try {
            Field frozenField = MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.set(BuiltInRegistries.LOOT_FUNCTION_TYPE, false);
            Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ResourceKey.create(Registries.LOOT_FUNCTION_TYPE, ResourceLocation.withDefaultNamespace("unknown")), LootFunctionTypes.UNUSED);
            frozenField.set(BuiltInRegistries.LOOT_FUNCTION_TYPE, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void injectLootCondition() {
        try {
            Field frozenField = MappedRegistry.class.getDeclaredField("frozen");
            frozenField.setAccessible(true);
            frozenField.set(BuiltInRegistries.LOOT_CONDITION_TYPE, false);
            Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, ResourceKey.create(Registries.LOOT_CONDITION_TYPE, ResourceLocation.withDefaultNamespace("unknown")), LootConditionTypes.UNUSED);
            frozenField.set(BuiltInRegistries.LOOT_CONDITION_TYPE, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
