package com.yanny.ali.test;

import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.ILootTableIdConditionPredicate;
import com.yanny.ali.plugin.server.LootConditionTypes;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GlobalLootModifierTest {
    private static final String ENTITY = "ENTITY";
    private static final String BLOCK = "BLOCK";
    private static final String LOOT_TABLE = "LOOT_TABLE";
    private static final String NONE = "NONE";

    private static final ResourceLocation DUNGEON = new ResourceLocation("chests/simple_dungeon");
    private static final ResourceLocation FORTRESS = new ResourceLocation("chests/nether_bridge");

    private static final ILootTableIdConditionPredicate TABLE_ID = new ILootTableIdConditionPredicate() {
        @Override
        public boolean isLootTableIdCondition(LootItemCondition condition) {
            return condition instanceof TableIdCondition;
        }

        @Override
        public ResourceLocation getTargetLootTableId(LootItemCondition condition) {
            return ((TableIdCondition) condition).id();
        }
    };

    @Test
    public void bareEntityConditionResolvesToEntity() {
        Result result = resolve(entity(EntityType.ZOMBIE));

        assertEquals(ENTITY, result.type());
        assertTrue(result.matches(entityOf(EntityType.ZOMBIE)));
        assertFalse(result.matches(entityOf(EntityType.CREEPER)));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void entityTagConditionResolvesToEntity() {
        Result result = resolve(entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(EntityTypeTags.SKELETONS))));

        assertEquals(ENTITY, result.type());
        assertTrue(result.matches(entityOf(EntityType.SKELETON)));
        assertTrue(result.matches(entityOf(EntityType.WITHER_SKELETON)));
        assertTrue(result.matches(entityOf(EntityType.STRAY)));
        assertFalse(result.matches(entityOf(EntityType.ZOMBIE)));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void entityConditionOnKillerIsNotADestination() {
        Result result = resolve(entity(LootContext.EntityTarget.KILLER, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(EntityType.ZOMBIE))));

        assertEquals(NONE, result.type());
    }

    @Test
    public void entityConditionWithoutTypeIsNotADestination() {
        assertEquals(NONE, resolve(entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity())).type());
        assertEquals(NONE, resolve(entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true).build()))).type());
    }

    @Test
    public void bareBlockConditionResolvesToBlock() {
        Result result = resolve(block(Blocks.FURNACE));

        assertEquals(BLOCK, result.type());
        assertTrue(result.matches(Blocks.FURNACE));
        assertFalse(result.matches(Blocks.BLAST_FURNACE));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void tableIdConditionResolvesToLootTable() {
        Result result = resolve(table(DUNGEON));

        assertEquals(LOOT_TABLE, result.type());
        assertTrue(result.matches(DUNGEON));
        assertFalse(result.matches(FORTRESS));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void unrelatedConditionsSurviveIntoTheTooltip() {
        assertEquals(List.of("LootItemRandomChanceCondition"),
                resolve(entity(EntityType.ZOMBIE), LootItemRandomChanceCondition.randomChance(0.5F).build()).retained());
        assertEquals(List.of("MatchTool", "LootItemKilledByPlayerCondition"),
                resolve(block(Blocks.WHEAT), MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.DIAMOND_HOE)).build(),
                        LootItemKilledByPlayerCondition.killedByPlayer().build()).retained());
    }

    @Test
    public void entityPredicateDetailIsDroppedFromTheTooltip_D1() {
        Result result = resolve(entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                .entityType(EntityTypePredicate.of(EntityType.ZOMBIE))
                .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true).build())));

        assertEquals(ENTITY, result.type());
        assertTrue(result.matches(entityOf(EntityType.ZOMBIE)));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void blockStatePropertiesAreDroppedFromTheTooltip_D1() {
        Result result = resolve(blockWithAge(Blocks.WHEAT, 7));

        assertEquals(BLOCK, result.type());
        assertTrue(result.matches(Blocks.WHEAT));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void allOfIsNotDescendedInto_D2() {
        assertEquals(NONE, resolve(allOf(entity(EntityType.ZOMBIE))).type());
        assertEquals(NONE, resolve(allOf(block(Blocks.FURNACE))).type());
        assertEquals(NONE, resolve(allOf(table(DUNGEON))).type());
        assertEquals(NONE, resolve(allOf(
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.DIAMOND_PICKAXE)).build(),
                block(Blocks.STONE)
        )).type());
    }

    @Test
    public void anyOfIsDescendedInto() {
        Result result = resolve(anyOf(entity(EntityType.ZOMBIE), entity(EntityType.CREEPER)));

        assertEquals(ENTITY, result.type());
        assertTrue(result.matches(entityOf(EntityType.ZOMBIE)));
        assertTrue(result.matches(entityOf(EntityType.CREEPER)));
        assertFalse(result.matches(entityOf(EntityType.SKELETON)));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void nestedAnyOfIsDescendedInto() {
        Result result = resolve(anyOf(anyOf(block(Blocks.FURNACE)), block(Blocks.STONE)));

        assertEquals(BLOCK, result.type());
        assertTrue(result.matches(Blocks.FURNACE));
        assertTrue(result.matches(Blocks.STONE));
        assertFalse(result.matches(Blocks.DIRT));
    }

    @Test
    public void anyOfAcrossKindsKeepsOnlyTheFirstKind_D3() {
        List<LootItemCondition> conditions = List.of(anyOf(entity(EntityType.ZOMBIE), table(DUNGEON)));
        Result result = resolve(conditions.toArray(new LootItemCondition[0]));

        assertEquals(ENTITY, result.type());
        assertTrue(result.matches(entityOf(EntityType.ZOMBIE)));
        assertTrue(GlobalLootModifierUtils.tablePredicate(conditions, TABLE_ID));
        assertTrue(GlobalLootModifierUtils.tablePredicate(conditions, DUNGEON, TABLE_ID));
        assertEquals(List.of(), result.retained());
    }

    @Test
    public void andListTakesTheFirstKindByTypePriority_D4() {
        assertEquals(BLOCK, resolve(block(Blocks.FURNACE), table(DUNGEON)).type());
        assertEquals(BLOCK, resolve(table(DUNGEON), block(Blocks.FURNACE)).type());
        assertEquals(ENTITY, resolve(block(Blocks.FURNACE), entity(EntityType.ZOMBIE)).type());
        assertEquals(ENTITY, resolve(table(DUNGEON), entity(EntityType.ZOMBIE)).type());
    }

    @Test
    public void andListOfContradictingEntitiesMatchesBoth_D4() {
        Result result = resolve(entity(EntityType.ZOMBIE), entity(EntityType.CREEPER));

        assertEquals(ENTITY, result.type());
        assertTrue(result.matches(entityOf(EntityType.ZOMBIE)));
        assertTrue(result.matches(entityOf(EntityType.CREEPER)));
    }

    @Test
    public void andListOfContradictingBlocksMatchesBoth_D4() {
        Result result = resolve(block(Blocks.FURNACE), block(Blocks.STONE));

        assertEquals(BLOCK, result.type());
        assertTrue(result.matches(Blocks.FURNACE));
        assertTrue(result.matches(Blocks.STONE));
    }

    @Test
    public void invertedIsNotDescendedInto() {
        assertEquals(NONE, resolve(inverted(entity(EntityType.ZOMBIE))).type());
        assertEquals(NONE, resolve(inverted(block(Blocks.FURNACE))).type());
        assertEquals(NONE, resolve(inverted(table(DUNGEON))).type());
    }

    @Test
    public void locationCheckIsNotADestination() {
        assertEquals(NONE, resolve(LocationCheck.checkLocation(LocationPredicate.Builder.location()
                .setBlock(net.minecraft.advancements.critereon.BlockPredicate.Builder.block().of(Blocks.DEEPSLATE).build())).build()).type());
    }

    @Test
    public void conditionReferenceIsNotADestination() {
        assertEquals(NONE, resolve(ConditionReference.conditionReference(new ResourceLocation("test")).build()).type());
    }

    @Test
    public void unresolvableConditionsAreADeadEnd_D8() {
        assertEquals(NONE, resolve().type());
        assertEquals(NONE, resolve(MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.DIAMOND_PICKAXE)).build()).type());
        assertEquals(NONE, resolve(LootItemRandomChanceCondition.randomChance(0.5F).build()).type());
        assertEquals(NONE, resolve(LootItemKilledByPlayerCondition.killedByPlayer().build()).type());
        assertEquals(NONE, resolve(new ModCondition()).type());
    }

    @Test
    public void modConditionAloneWithBlockStillResolves() {
        Result result = resolve(new ModCondition(), block(Blocks.FURNACE));

        assertEquals(BLOCK, result.type());
        assertEquals(List.of("ModCondition"), result.retained());
    }

    @NotNull
    private static Result resolve(LootItemCondition... conditions) {
        List<String> retained = new ArrayList<>();
        Optional<ILootModifier<?>> modifier = GlobalLootModifierUtils.getLootModifier(List.of(conditions), (c) -> {
            retained.clear();
            c.forEach((condition) -> retained.add(condition.getClass().getSimpleName()));
            return List.of();
        }, TABLE_ID);

        modifier.ifPresent(ILootModifier::getOperations);
        return new Result(modifier, retained);
    }

    @NotNull
    private static LootItemCondition entity(EntityType<?> type) {
        return entity(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(type)));
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
    private static LootItemCondition table(ResourceLocation id) {
        return new TableIdCondition(id);
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

    private static Entity entityOf(EntityType<?> type) {
        Entity entity = mock(Entity.class);

        doReturn(type).when(entity).getType();
        return entity;
    }

    private record Result(Optional<ILootModifier<?>> modifier, List<String> retained) {
        String type() {
            if (modifier.isEmpty()) {
                return NONE;
            }

            ILootModifier.IType<?> type = modifier.get().getType();

            if (type instanceof ILootModifier.IType.EntityType) {
                return ENTITY;
            } else if (type instanceof ILootModifier.IType.BlockType) {
                return BLOCK;
            } else {
                return LOOT_TABLE;
            }
        }

        boolean matches(Object value) {
            //noinspection unchecked
            return ((ILootModifier<Object>) modifier.orElseThrow()).predicate(value);
        }
    }

    private record TableIdCondition(ResourceLocation id) implements LootItemCondition {
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
}
