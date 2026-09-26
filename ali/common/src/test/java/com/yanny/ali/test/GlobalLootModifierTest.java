package com.yanny.ali.test;

import com.mojang.serialization.MapCodec;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.configuration.AliConfig;
import com.yanny.ali.plugin.glm.*;
import com.yanny.ali.plugin.server.LootConditionTypes;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.yanny.ali.test.TooltipTestSuite.LOOKUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GlobalLootModifierTest {
    private static final Identifier DUNGEON = Identifier.withDefaultNamespace("chests/simple_dungeon");
    private static final Identifier FORTRESS = Identifier.withDefaultNamespace("chests/nether_bridge");
    private static final Identifier PREDICATE = Identifier.fromNamespaceAndPath("test", "predicate");
    private static final Map<LootContext, Identifier> QUERIED_IDS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<LootContext, Identifier> EXTENSION_IDS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final HolderGetter.Provider LOOT_DATA = new HolderGetter.Provider() {
        @NotNull
        @Override
        public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> registry) {
            return Optional.empty();
        }

        @NotNull
        @Override
        public <T> Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
            if (!key.identifier().equals(PREDICATE)) {
                return Optional.empty();
            }

            //noinspection unchecked
            Holder.Reference<T> reference = mock(Holder.Reference.class);

            doReturn(LootItemRandomChanceCondition.randomChance(0.5F).build()).when(reference).value();
            return Optional.of(reference);
        }
    };

    private static final IServerUtils UTILS = serverUtils(List.of(GlobalLootModifierTest::prepareTableId), null);

    @Test
    public void paramStatesFollowThePageParameterSet() {
        LootPage chest = page(DUNGEON, LootContextParamSets.CHEST);

        assertEquals(ParamState.REQUIRED, GlobalLootModifierUtils.getParamState(chest, LootContextParams.ORIGIN));
        assertEquals(ParamState.OPTIONAL, GlobalLootModifierUtils.getParamState(chest, LootContextParams.THIS_ENTITY));
        assertEquals(ParamState.DISALLOWED, GlobalLootModifierUtils.getParamState(chest, LootContextParams.BLOCK_STATE));
        assertEquals(ParamState.GENERIC, GlobalLootModifierUtils.getParamState(page(DUNGEON, LootContextParamSets.ALL_PARAMS), LootContextParams.ORIGIN));
    }

    @Test
    public void bareEntityConditionBindsItsEntityPages() {
        LootItemCondition condition = entity(EntityType.ZOMBIE);

        assertEquals(Verdict.yes(true), verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.CREEPER), condition));
        assertEquals(List.of(), retained(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void entityTagConditionBindsEveryTaggedType() {
        LootItemCondition condition = entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityTypeTags.SKELETONS)));

        assertEquals(Match.YES, match(entityPage(EntityType.SKELETON), condition));
        assertEquals(Match.YES, match(entityPage(EntityType.WITHER_SKELETON), condition));
        assertEquals(Match.NO, match(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void entityConditionOnPageSharedByTypesIsNotExplained() {
        LootPage page = new LootPage(EntityType.ZOMBIE.getDefaultLootTable().orElseThrow().identifier(), LootContextParamSets.ENTITY, List.of(), List.of(EntityType.ZOMBIE, EntityType.HUSK), List::of);

        assertEquals(Verdict.yes(false), verdict(page, entity(EntityType.ZOMBIE)));
    }

    @Test
    public void entityConditionOnKillerIsUndecidedWhereTheKillerIsAllowed() {
        LootItemCondition condition = entity(LootContext.EntityTarget.ATTACKER, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.ZOMBIE)));

        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.SKELETON), condition));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.STONE), condition));
    }

    @Test
    public void entityConditionWithoutTypeIsDecidedByItsParts() {
        assertEquals(Verdict.yes(true), verdict(entityPage(EntityType.ZOMBIE), entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity())));
        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true)))));
    }

    @Test
    public void entitySubPredicateNeedsAResolver() {
        LootItemCondition condition = entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.SLIME))
                .subPredicate(SlimePredicate.sized(MinMaxBounds.Ints.atLeast(2))));
        IServerUtils resolving = serverUtils(List.of(), Verdict.yes(true));

        assertEquals(Verdict.yes(false), verdict(entityPage(EntityType.SLIME), condition));
        assertEquals(Verdict.yes(true), resolving.testPage(resolving, condition, entityPage(EntityType.SLIME)));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void bareBlockConditionBindsItsBlockPages() {
        LootItemCondition condition = block(Blocks.FURNACE);

        assertEquals(Verdict.yes(true), verdict(blockPage(Blocks.FURNACE), condition));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.BLAST_FURNACE), condition));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
    }

    @Test
    public void tableIdIsSuppliedByPreparers() {
        LootItemCondition condition = new TableIdCondition(DUNGEON);
        IServerUtils unprepared = serverUtils(List.of(), null);

        assertEquals(Verdict.yes(true), verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.NO, verdict(page(FORTRESS, LootContextParamSets.CHEST), condition));
        assertEquals(List.of(), retained(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.NO, unprepared.testPage(unprepared, condition, page(DUNGEON, LootContextParamSets.CHEST)));
    }

    @Test
    public void conditionsOfDifferentKindsAllConstrainThePage() {
        LootItemCondition[] conditions = {block(Blocks.FURNACE), new TableIdCondition(DUNGEON)};

        assertEquals(Match.YES, match(new LootPage(DUNGEON, LootContextParamSets.BLOCK, List.of(Blocks.FURNACE), List.of(), List::of), conditions));
        assertEquals(Match.NO, match(blockPage(Blocks.FURNACE), conditions));
    }

    @Test
    public void unrelatedConditionsSurviveIntoTheTooltip() {
        assertEquals(List.of("LootItemRandomChanceCondition"),
                retained(entityPage(EntityType.ZOMBIE), entity(EntityType.ZOMBIE), LootItemRandomChanceCondition.randomChance(0.5F).build()));
        assertEquals(List.of("MatchTool"),
                retained(blockPage(Blocks.WHEAT), block(Blocks.WHEAT), MatchTool.toolMatches(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.DIAMOND_HOE)).build()));
    }

    @Test
    public void entityPredicateDetailSurvivesIntoTheTooltip() {
        LootItemCondition condition = entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.ZOMBIE))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true)));

        assertEquals(Match.YES, match(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(List.of("LootItemEntityPropertyCondition"), retained(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void blockStatePropertiesSurviveIntoTheTooltip() {
        LootItemCondition condition = blockWithAge(Blocks.WHEAT, 7);

        assertEquals(Match.YES, match(blockPage(Blocks.WHEAT), condition));
        assertEquals(List.of("LootItemBlockStatePropertyCondition"), retained(blockPage(Blocks.WHEAT), condition));
    }

    @Test
    public void allOfNeedsEveryTermDecided() {
        assertEquals(Verdict.yes(true), verdict(blockPage(Blocks.FURNACE), allOf(block(Blocks.FURNACE))));

        LootItemCondition condition = allOf(MatchTool.toolMatches(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.DIAMOND_PICKAXE)).build(), block(Blocks.STONE));

        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.DIRT), condition));
    }

    @Test
    public void anyOfBindsEachBranch() {
        LootItemCondition condition = anyOf(entity(EntityType.ZOMBIE), entity(EntityType.CREEPER));

        assertEquals(Verdict.yes(false), verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.yes(false), verdict(entityPage(EntityType.CREEPER), condition));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.SKELETON), condition));
        assertEquals(List.of("AnyOfCondition"), retained(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void nestedAnyOfIsDescendedInto() {
        LootItemCondition condition = anyOf(anyOf(block(Blocks.FURNACE)), block(Blocks.STONE));

        assertEquals(Match.YES, match(blockPage(Blocks.FURNACE), condition));
        assertEquals(Match.YES, match(blockPage(Blocks.STONE), condition));
        assertEquals(Match.NO, match(blockPage(Blocks.DIRT), condition));
    }

    @Test
    public void anyOfAcrossKindsKeepsBothKinds() {
        LootItemCondition condition = anyOf(entity(EntityType.ZOMBIE), block(Blocks.STONE));

        assertEquals(Match.YES, match(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Match.YES, match(blockPage(Blocks.STONE), condition));
        assertEquals(Match.UNKNOWN, match(blockPage(Blocks.DIRT), condition));
    }

    @Test
    public void andListOfContradictingEntitiesMatchesNothing() {
        assertEquals(Match.NO, match(entityPage(EntityType.ZOMBIE), entity(EntityType.ZOMBIE), entity(EntityType.CREEPER)));
        assertEquals(Match.NO, match(entityPage(EntityType.CREEPER), entity(EntityType.ZOMBIE), entity(EntityType.CREEPER)));
    }

    @Test
    public void andListOfContradictingBlocksMatchesNothing() {
        assertEquals(Match.NO, match(blockPage(Blocks.FURNACE), block(Blocks.FURNACE), block(Blocks.STONE)));
        assertEquals(Match.NO, match(blockPage(Blocks.STONE), block(Blocks.FURNACE), block(Blocks.STONE)));
    }

    @Test
    public void invertedExcludesThePagesItsTermIsExplainedOn() {
        LootItemCondition condition = inverted(entity(EntityType.ZOMBIE));

        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.yes(false), verdict(entityPage(EntityType.CREEPER), condition));
        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), inverted(entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.ZOMBIE))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))))));
    }

    @Test
    public void probeTrapsRandomLevelAndAllowedParameters() {
        LootPage chest = page(DUNGEON, LootContextParamSets.CHEST);

        assertEquals(Verdict.UNKNOWN, verdict(chest, LootItemRandomChanceCondition.randomChance(0.5F).build()));
        assertEquals(Verdict.UNKNOWN, verdict(chest, new LevelCondition()));
        assertEquals(Verdict.UNKNOWN, verdict(chest, LocationCheck.checkLocation(LocationPredicate.Builder.inDimension(net.minecraft.world.level.Level.NETHER)).build()));
        assertEquals(Verdict.UNKNOWN, verdict(chest, new HasParamCondition()));
        assertEquals(Verdict.yes(true), verdict(page(DUNGEON, LootContextParamSets.GIFT), new HasParamCondition()));
        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.STONE), MatchTool.toolMatches(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.DIAMOND_PICKAXE)).build()));
        assertEquals(Verdict.NO, verdict(chest, MatchTool.toolMatches(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.DIAMOND_PICKAXE)).build()));
    }

    @Test
    public void killedByPlayerIsExcludedWhereNoPlayerCanBePresent() {
        LootItemCondition condition = LootItemKilledByPlayerCondition.killedByPlayer().build();

        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
    }

    @Test
    public void damageSourceIsExcludedWhereNoDamageSourceIsAllowed() {
        LootItemCondition condition = DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()).build();

        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void conditionReferenceResolvesStaticData() {
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.CHEST), ConditionReference.conditionReference(ResourceKey.create(Registries.PREDICATE, PREDICATE)).build()));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), ConditionReference.conditionReference(ResourceKey.create(Registries.PREDICATE, Identifier.fromNamespaceAndPath("test", "missing"))).build()));
    }

    @Test
    public void tautologyIsExplainedEverywhere() {
        assertEquals(Verdict.yes(true), verdict(page(DUNGEON, LootContextParamSets.CHEST), new ModCondition()));
        assertEquals(List.of(), retained(page(DUNGEON, LootContextParamSets.CHEST), new ModCondition()));
    }

    @Test
    public void onlyUndecidedConditionsLeaveTheModifierUnbound() {
        LootPage chest = page(DUNGEON, LootContextParamSets.CHEST);

        assertEquals(Match.UNKNOWN, match(chest));
        assertEquals(Match.UNKNOWN, match(chest, LootItemRandomChanceCondition.randomChance(0.5F).build()));
        assertEquals(Match.YES, match(chest, new TableIdCondition(DUNGEON), LootItemRandomChanceCondition.randomChance(0.5F).build()));
    }

    @Test
    public void modifierObjectIsDecidedByItsResolverOnly() {
        assertEquals(Match.UNKNOWN, match(page(DUNGEON, LootContextParamSets.CHEST), new ModModifier(null)));
        assertEquals(Match.YES, match(page(DUNGEON, LootContextParamSets.CHEST), new ModModifier(DUNGEON)));
        assertEquals(Match.NO, match(page(FORTRESS, LootContextParamSets.CHEST), new ModModifier(DUNGEON)));
    }

    @Test
    public void monsterGemModifierStaysOffTypedNonEntityPages() {
        Object modifier = new ModModifier(Identifier.fromNamespaceAndPath("minecraft", "any"));
        LootItemCondition[] conditions = {
                entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(SlimePredicate.sized(MinMaxBounds.Ints.atLeast(1)))),
                LootItemKilledByPlayerCondition.killedByPlayer().build(),
        };
        IServerUtils utils = serverUtils(List.of(), null);

        assertEquals(Match.NO, utilsMatch(utils, page(Identifier.fromNamespaceAndPath("minecraft", "any"), LootContextParamSets.CHEST), modifier, conditions));
        assertEquals(Match.YES, utilsMatch(utils, new LootPage(Identifier.fromNamespaceAndPath("minecraft", "any"), LootContextParamSets.ENTITY, List.of(), List.of(EntityType.ZOMBIE), List::of), modifier, conditions));
    }

    @Test
    public void contextSetConditionNeedsEveryRequiredParameter() {
        assertEquals(Verdict.yes(true), verdict(blockPage(Blocks.STONE), new HasContextSetCondition(LootContextParamSets.BLOCK)));
        assertEquals(Verdict.yes(true), verdict(entityPage(EntityType.ZOMBIE), new HasContextSetCondition(LootContextParamSets.ENTITY)));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), new HasContextSetCondition(LootContextParamSets.BLOCK)));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.PIGLIN_BARTER), new HasContextSetCondition(LootContextParamSets.GIFT)));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.CHEST), new HasContextSetCondition(LootContextParamSets.PIGLIN_BARTER)));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), new HasContextSetCondition(LootContextParamSets.BLOCK)));
    }

    @Test
    public void toolAndEntityConditionIsUndecidedOnlyWhereTheToolIsRolled() {
        LootItemCondition condition = new DirectUpgradeCondition();

        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.FISHING), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.GIFT), condition));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), condition));
    }

    @Test
    public void blockEntityConditionIsExcludedWhereNoBlockEntityCanBePresent() {
        LootItemCondition condition = new NamedBlockEntityCondition();

        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.CHEST), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
    }

    @Test
    public void killerConditionFollowsTheKillerParameter() {
        LootItemCondition condition = new KilledByRealPlayerCondition();

        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.ARCHAEOLOGY), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.GIFT), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.PIGLIN_BARTER), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
    }

    @Test
    public void configChanceIsUndecidedEverywhere() {
        LootItemCondition condition = new ConfigChanceCondition(0.5F);

        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
    }

    @Test
    public void configValueIsAConstantOnEveryPage() {
        LootPage generic = page(DUNGEON, LootContextParamSets.ALL_PARAMS);

        assertEquals(Verdict.yes(true), verdict(generic, new ConfigValueCondition(true)));
        assertEquals(Verdict.NO, verdict(generic, new ConfigValueCondition(false)));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), new ConfigValueCondition(false)));
        assertEquals(List.of("LootItemRandomChanceCondition"), retained(generic, new ConfigValueCondition(true), LootItemRandomChanceCondition.randomChance(0.5F).build()));
        assertEquals(Match.NO, match(page(DUNGEON, LootContextParamSets.CHEST), new TableIdCondition(DUNGEON), new ConfigValueCondition(false)));
    }

    @Test
    public void strictReadOfDisallowedParameterIsUndecided() {
        LootItemCondition condition = new StrictBlockStateCondition(Blocks.STONE);

        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.UNKNOWN, verdict(blockPage(Blocks.STONE), condition));
    }

    @Test
    public void trapIsNotSwallowedByExceptionHandlers() {
        LootItemCondition condition = new SwallowingCondition();

        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
        assertEquals(Verdict.yes(true), verdict(page(DUNGEON, LootContextParamSets.GIFT), condition));
    }

    @Test
    public void luckIsNotTrapped() {
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), new LuckCondition()));
    }

    @Test
    public void tablePatternIsDecidedOnEveryPageKind() {
        LootItemCondition chests = new TablePatternCondition(Pattern.compile("^minecraft:chests/"));
        LootItemCondition stone = new TablePatternCondition(Pattern.compile("^minecraft:blocks/stone$"));

        assertEquals(Verdict.yes(true), verdict(page(DUNGEON, LootContextParamSets.CHEST), chests));
        assertEquals(Verdict.yes(true), verdict(page(FORTRESS, LootContextParamSets.ALL_PARAMS), chests));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.STONE), chests));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), chests));
        assertEquals(Verdict.yes(true), verdict(blockPage(Blocks.STONE), stone));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.DIRT), stone));
        assertEquals(List.of(), retained(page(DUNGEON, LootContextParamSets.CHEST), chests));
    }

    @Test
    public void everyPreparerFillsItsOwnCarrier() {
        LootItemCondition forge = new TableIdCondition(DUNGEON);
        LootItemCondition extension = new ExtensionTableIdCondition(DUNGEON);
        LootPage chest = page(DUNGEON, LootContextParamSets.CHEST);
        IServerUtils both = serverUtils(List.of(GlobalLootModifierTest::prepareTableId, GlobalLootModifierTest::prepareExtensionTableId), null);

        assertEquals(Verdict.NO, verdict(chest, extension));
        assertEquals(Verdict.yes(true), both.testPage(both, forge, chest));
        assertEquals(Verdict.yes(true), both.testPage(both, extension, chest));
        assertEquals(Verdict.NO, both.testPage(both, extension, page(FORTRESS, LootContextParamSets.CHEST)));
    }

    @Test
    public void pageResolverWithoutOpinionFallsBackToTheProbe() {
        LootItemCondition condition = new BlockSetCondition(Set.of(Blocks.FURNACE, Blocks.BLAST_FURNACE));
        LootPage shared = new LootPage(DUNGEON, LootContextParamSets.BLOCK, List.of(Blocks.FURNACE, Blocks.STONE), List.of(), List::of);

        assertEquals(Verdict.yes(true), verdict(blockPage(Blocks.FURNACE), condition));
        assertEquals(Verdict.yes(false), verdict(shared, condition));
        assertEquals(Verdict.NO, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), condition));
        assertEquals(Verdict.NO, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(page(DUNGEON, LootContextParamSets.ALL_PARAMS), condition));
    }

    @Test
    public void allOfIsDescendedPastAnUndecidedTerm() {
        LootItemCondition condition = allOf(LootItemRandomChanceCondition.randomChance(0.5F).build(), new KilledByRealPlayerCondition());

        assertEquals(Verdict.NO, verdict(blockPage(Blocks.STONE), condition));
        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), condition));
        assertEquals(Verdict.yes(true), verdict(page(DUNGEON, LootContextParamSets.CHEST), allOf(new TableIdCondition(DUNGEON), new ConfigValueCondition(true))));
    }

    @Test
    public void anyOfMixesDecidedAndUndecidedTerms() {
        LootPage block = blockPage(Blocks.STONE);

        assertEquals(Verdict.UNKNOWN, verdict(block, anyOf(new ConfigChanceCondition(0.5F), new KilledByRealPlayerCondition())));
        assertEquals(Verdict.NO, verdict(block, anyOf(new ConfigValueCondition(false), new KilledByRealPlayerCondition())));
        assertEquals(Verdict.yes(false), verdict(block, anyOf(new ConfigValueCondition(true), new ConfigChanceCondition(0.5F))));
        assertEquals(Verdict.yes(false), verdict(page(DUNGEON, LootContextParamSets.CHEST), anyOf(new ConfigValueCondition(false), new TableIdCondition(DUNGEON))));
        assertEquals(List.of("AnyOfCondition"), retained(page(DUNGEON, LootContextParamSets.CHEST), anyOf(new ConfigValueCondition(false), new TableIdCondition(DUNGEON))));
    }

    @Test
    public void invertedModConditions() {
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), inverted(new ConfigValueCondition(true))));
        assertEquals(Verdict.yes(false), verdict(page(DUNGEON, LootContextParamSets.CHEST), inverted(new ConfigValueCondition(false))));
        assertEquals(Verdict.yes(false), verdict(blockPage(Blocks.STONE), inverted(new KilledByRealPlayerCondition())));
        assertEquals(Verdict.UNKNOWN, verdict(entityPage(EntityType.ZOMBIE), inverted(new KilledByRealPlayerCondition())));
        assertEquals(Verdict.NO, verdict(page(DUNGEON, LootContextParamSets.CHEST), inverted(new TableIdCondition(DUNGEON))));
        assertEquals(Verdict.yes(false), verdict(page(FORTRESS, LootContextParamSets.CHEST), inverted(new TableIdCondition(DUNGEON))));
        assertEquals(List.of("InvertedLootItemCondition"), retained(page(FORTRESS, LootContextParamSets.CHEST), inverted(new TableIdCondition(DUNGEON))));
    }

    @Test
    public void subPredicateResolverBindsMonsterGemsToMonsterPages() {
        IServerUtils utils = resolvingServerUtils(List.of(), (u, p, page) -> p instanceof MonsterPredicate
                ? GlobalLootModifierUtils.testEntityTypes(page, (t) -> t.getCategory() == MobCategory.MONSTER, true)
                : null);
        Object modifier = new PatternModifier(Pattern.compile(".*"));
        LootItemCondition monster = entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(new MonsterPredicate()));
        LootItemCondition[] conditions = {monster, LootItemKilledByPlayerCondition.killedByPlayer().build()};

        assertEquals(Match.YES, utilsMatch(utils, entityPage(EntityType.ZOMBIE), modifier, conditions));
        assertEquals(Match.NO, utilsMatch(utils, entityPage(EntityType.COW), modifier, conditions));
        assertEquals(Match.NO, utilsMatch(utils, blockPage(Blocks.STONE), modifier, conditions));
        assertEquals(Match.NO, utilsMatch(utils, page(DUNGEON, LootContextParamSets.CHEST), modifier, conditions));
        assertEquals(Match.YES, utilsMatch(utils, page(DUNGEON, LootContextParamSets.ALL_PARAMS), modifier, conditions));
        assertEquals(Verdict.yes(true), utils.testPage(utils, monster, entityPage(EntityType.ZOMBIE)));
    }

    @Test
    public void scavengingDropStaysOnItsMobWithTheUndecidedPartsInTheTooltip() {
        LootItemCondition knife = entity(LootContext.EntityTarget.ATTACKER, EntityPredicate.Builder.entity()
                .equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().of(LOOKUP.lookupOrThrow(Registries.ITEM), Items.IRON_SWORD))));
        LootItemCondition onFire = entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .flags(EntityFlagsPredicate.Builder.flags().setOnFire(true)));
        LootItemCondition chance = LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(LOOKUP, 0.5F, 0.1F).build();
        LootItemCondition[] conditions = {knife, entity(EntityType.PIG), onFire, chance};

        assertEquals(Match.YES, match(entityPage(EntityType.PIG), (Object[]) conditions));
        assertEquals(Match.NO, match(entityPage(EntityType.COW), (Object[]) conditions));
        assertEquals(Match.NO, match(blockPage(Blocks.STONE), (Object[]) conditions));
        assertEquals(Match.UNKNOWN, match(page(DUNGEON, LootContextParamSets.ALL_PARAMS), (Object[]) conditions));
        assertEquals(List.of("LootItemEntityPropertyCondition", "LootItemEntityPropertyCondition", "LootItemRandomChanceWithEnchantedBonusCondition"),
                retained(entityPage(EntityType.PIG), conditions));
    }

    @Test
    public void burningMobDropIsExcludedByItsBlacklistAndOffEntityPages() {
        LootItemCondition fire = DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FIRE))).build();
        LootItemCondition blacklist = inverted(entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.SKELETON))));
        LootItemCondition onFire = entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .flags(EntityFlagsPredicate.Builder.flags().setOnFire(true)));
        LootItemCondition[] conditions = {fire, LootItemRandomChanceCondition.randomChance(0.5F).build(), blacklist, onFire};

        assertEquals(Match.YES, match(entityPage(EntityType.ZOMBIE), (Object[]) conditions));
        assertEquals(Match.NO, match(entityPage(EntityType.SKELETON), (Object[]) conditions));
        assertEquals(Match.NO, match(page(DUNGEON, LootContextParamSets.CHEST), (Object[]) conditions));
        assertEquals(Match.NO, match(blockPage(Blocks.STONE), (Object[]) conditions));
    }

    @NotNull
    private static IServerUtils serverUtils(List<ILootContextPreparer> preparers, @Nullable Verdict subPredicateVerdict) {
        return resolvingServerUtils(preparers, (u, p, page) -> subPredicateVerdict);
    }

    @NotNull
    private static IServerUtils resolvingServerUtils(List<ILootContextPreparer> preparers, IEntitySubPredicateResolver<EntitySubPredicate> subPredicateResolver) {
        IServerUtils utils = mock(IServerUtils.class);

        doAnswer((i) -> GlobalLootModifierUtils.testPage(i.getArgument(0), i.getArgument(1), i.getArgument(2), resolver(i.getArgument(1)), preparers, LOOT_DATA))
                .when(utils).testPage(any(), any(), any());
        doAnswer((i) -> GlobalLootModifierUtils.getParamState(i.getArgument(0), i.getArgument(1))).when(utils).getParamState(any(), any());
        doAnswer((i) -> subPredicateResolver.test(i.getArgument(0), i.getArgument(1), i.getArgument(2))).when(utils).testEntitySubPredicate(any(), any(), any());
        doReturn(new AliConfig()).when(utils).getConfiguration();
        return utils;
    }

    @Nullable
    private static IPageResolver<Object> resolver(Object value) {
        if (value instanceof LootItemBlockStatePropertyCondition) {
            return (u, v, p) -> GlobalLootModifierUtils.testBlockStateProperty(u, (LootItemBlockStatePropertyCondition) v, p);
        } else if (value instanceof LootItemEntityPropertyCondition) {
            return (u, v, p) -> GlobalLootModifierUtils.testEntityProperty(u, (LootItemEntityPropertyCondition) v, p);
        } else if (value instanceof DamageSourceCondition) {
            return (u, v, p) -> GlobalLootModifierUtils.testDamageSource(u, (DamageSourceCondition) v, p);
        } else if (value instanceof ModModifier) {
            return (u, v, p) -> ((ModModifier) v).table() == null ? null : GlobalLootModifierUtils.testTable(p, ((ModModifier) v).table()::equals, true);
        } else if (value instanceof PatternModifier) {
            return (u, v, p) -> GlobalLootModifierUtils.testTable(p, (id) -> ((PatternModifier) v).pattern().matcher(id.toString()).find(), true);
        } else if (value instanceof BlockSetCondition) {
            return (u, v, p) -> p.blocks().isEmpty() ? null : GlobalLootModifierUtils.testBlocks(p, ((BlockSetCondition) v).blocks()::contains, true);
        }

        return null;
    }

    private static void prepareTableId(IServerUtils ignoredUtils, LootContext context, LootPage page) {
        QUERIED_IDS.put(context, page.tableId());
    }

    private static void prepareExtensionTableId(IServerUtils ignoredUtils, LootContext context, LootPage page) {
        EXTENSION_IDS.put(context, page.tableId());
    }

    @NotNull
    private static Verdict verdict(LootPage page, LootItemCondition condition) {
        return UTILS.testPage(UTILS, condition, page);
    }

    @NotNull
    private static Match match(LootPage page, Object... terms) {
        Object modifier = terms.length > 0 && terms[0] instanceof ModModifier ? terms[0] : null;
        LootItemCondition[] conditions = Stream.of(terms).filter(LootItemCondition.class::isInstance).toArray(LootItemCondition[]::new);

        return utilsMatch(UTILS, page, modifier, conditions);
    }

    @NotNull
    private static Match utilsMatch(IServerUtils utils, LootPage page, @Nullable Object modifier, LootItemCondition... conditions) {
        return GlobalLootModifierUtils.getLootModifier(utils, modifier, List.of(conditions), (p, c) -> List.of()).test(page).match();
    }

    @NotNull
    private static List<String> retained(LootPage page, LootItemCondition... conditions) {
        List<String> retained = new ArrayList<>();

        IPageLootModifier modifier = GlobalLootModifierUtils.getLootModifier(UTILS, null, List.of(conditions), (p, c) -> {
            c.forEach((condition) -> retained.add(condition.getClass().getSimpleName()));
            return List.of();
        });

        modifier.getOperations(page, modifier.test(page));
        return retained;
    }

    @NotNull
    private static LootPage page(Identifier tableId, ContextKeySet paramSet) {
        return new LootPage(tableId, paramSet, List.of(), List.of(), List::of);
    }

    @NotNull
    private static LootPage blockPage(Block block) {
        return new LootPage(block.getLootTable().orElseThrow().identifier(), LootContextParamSets.BLOCK, List.of(block), List.of(), List::of);
    }

    @NotNull
    private static LootPage entityPage(EntityType<?> type) {
        return new LootPage(type.getDefaultLootTable().orElseThrow().identifier(), LootContextParamSets.ENTITY, List.of(), List.of(type), List::of);
    }

    @NotNull
    private static LootItemCondition entity(EntityType<?> type) {
        return entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(LOOKUP.lookupOrThrow(Registries.ENTITY_TYPE), type)));
    }

    @NotNull
    private static LootItemCondition entity(LootContext.EntityTarget target, EntityPredicate.Builder predicate) {
        return LootItemEntityPropertyCondition.hasProperties(target, predicate).build();
    }

    @NotNull
    private static LootItemCondition block(Block block) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build();
    }

    @NotNull
    private static LootItemCondition blockWithAge(Block block, int age) {
        return LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.AGE_7, age))
                .build();
    }

    @NotNull
    private static LootItemCondition allOf(LootItemCondition... conditions) {
        return AllOfCondition.allOf(builders(conditions)).build();
    }

    @NotNull
    private static LootItemCondition anyOf(LootItemCondition... conditions) {
        return AnyOfCondition.anyOf(builders(conditions)).build();
    }

    @NotNull
    private static LootItemCondition inverted(LootItemCondition condition) {
        return InvertedLootItemCondition.invert(() -> condition).build();
    }

    private static LootItemCondition.Builder[] builders(LootItemCondition... conditions) {
        return Stream.of(conditions).map((c) -> (LootItemCondition.Builder) () -> c).toArray(LootItemCondition.Builder[]::new);
    }

    private record TableIdCondition(Identifier id) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return id.equals(QUERIED_IDS.get(lootContext));
        }
    }

    private record ModCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return true;
        }
    }

    private record LevelCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.getLevel().isRaining();
        }
    }

    private record HasParamCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.hasParameter(LootContextParams.THIS_ENTITY);
        }
    }

    private record ModModifier(@Nullable Identifier table) {}

    private record PatternModifier(Pattern pattern) {}

    private record HasContextSetCondition(ContextKeySet set) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            for (ContextKey<?> param : set.required()) {
                if (!lootContext.hasParameter(param)) {
                    return false;
                }
            }

            return true;
        }
    }

    private record DirectUpgradeCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.hasParameter(LootContextParams.TOOL)
                    && lootContext.hasParameter(LootContextParams.THIS_ENTITY)
                    && lootContext.getParameter(LootContextParams.TOOL).is(Items.DIAMOND_PICKAXE);
        }
    }

    private record NamedBlockEntityCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof Nameable nameable && nameable.hasCustomName();
        }
    }

    private record KilledByRealPlayerCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.getOptionalParameter(LootContextParams.ATTACKING_ENTITY) instanceof Player;
        }
    }

    private record ConfigChanceCondition(float chance) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.getRandom().nextFloat() < chance;
        }
    }

    private record ConfigValueCondition(boolean value) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return value;
        }
    }

    private record StrictBlockStateCondition(Block block) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.getParameter(LootContextParams.BLOCK_STATE).is(block);
        }
    }

    private record SwallowingCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            try {
                return lootContext.hasParameter(LootContextParams.THIS_ENTITY);
            } catch (Exception e) {
                return false;
            }
        }
    }

    private record LuckCondition() implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return lootContext.getLuck() > 0;
        }
    }

    private record TablePatternCondition(Pattern pattern) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            Identifier id = QUERIED_IDS.get(lootContext);
            return id != null && pattern.matcher(id.toString()).find();
        }
    }

    private record ExtensionTableIdCondition(Identifier id) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            return id.equals(EXTENSION_IDS.get(lootContext));
        }
    }

    private record BlockSetCondition(Set<Block> blocks) implements LootItemCondition {
        @NotNull
        @Override
        public LootItemConditionType getType() {
            return LootConditionTypes.UNUSED;
        }

        @Override
        public boolean test(LootContext lootContext) {
            BlockState state = lootContext.getOptionalParameter(LootContextParams.BLOCK_STATE);
            return state != null && blocks.contains(state.getBlock());
        }
    }

    private record MonsterPredicate() implements EntitySubPredicate {
        @Override
        public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
            return entity instanceof Monster;
        }

        @NotNull
        @Override
        public MapCodec<MonsterPredicate> codec() {
            return MapCodec.unit(this);
        }
    }
}
