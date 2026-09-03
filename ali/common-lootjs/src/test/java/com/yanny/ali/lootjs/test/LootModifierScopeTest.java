package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.LootType;
import com.almostreliable.lootjs.core.filters.IdFilter;
import com.almostreliable.lootjs.core.filters.ItemFilter;
import com.almostreliable.lootjs.core.filters.LootTableFilter;
import com.almostreliable.lootjs.loot.modifier.LootAction;
import com.almostreliable.lootjs.loot.modifier.LootModifier;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.lootjs.BlockLootModifier;
import com.yanny.ali.lootjs.EntityLootModifier;
import com.yanny.ali.lootjs.TableLootModifier;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootModifierScopeTest {
    @Test
    public void testBlockModifierUsesCapturedPredicate() {
        Assertions.assertFalse(blockModifier(capturing(BlockStatePredicate.Simple.NONE)).predicate(Blocks.STONE));
        Assertions.assertTrue(blockModifier(capturing(BlockStatePredicate.Simple.ALL)).predicate(Blocks.STONE));
        Assertions.assertEquals(ILootModifier.IType.BLOCK, blockModifier(capturing(BlockStatePredicate.Simple.ALL)).getType());
    }

    @Test
    public void testBlockModifierRejectsPredicateWithoutCapturedInstance() {
        Assertions.assertThrows(IllegalStateException.class, () -> blockModifier((state) -> true));
    }

    @Test
    public void testBlockModifierRejectsPredicateWithMultipleCapturedInstances() {
        BlockStatePredicate all = BlockStatePredicate.Simple.ALL;
        BlockStatePredicate none = BlockStatePredicate.Simple.NONE;

        Assertions.assertThrows(IllegalStateException.class, () -> blockModifier((state) -> all.test(state) && none.test(state)));
    }

    @Test
    public void testEntityModifierMatchesListedTypes() {
        EntityLootModifier modifier = entityModifier(EntityType.ZOMBIE, EntityType.SKELETON);

        Assertions.assertTrue(modifier.predicate(entity(EntityType.ZOMBIE)));
        Assertions.assertTrue(modifier.predicate(entity(EntityType.SKELETON)));
        Assertions.assertFalse(modifier.predicate(entity(EntityType.CREEPER)));
        Assertions.assertEquals(ILootModifier.IType.ENTITY, modifier.getType());
    }

    @Test
    public void testEntityModifierWithoutTypesMatchesNothing() {
        Assertions.assertFalse(entityModifier().predicate(entity(EntityType.ZOMBIE)));
    }

    @Test
    public void testTableModifierMatchesAnyFilter() {
        TableLootModifier modifier = tableModifier(
                new LootTableFilter.ByIdFilter(new IdFilter.ByLocation(Identifier.withDefaultNamespace("chests/igloo_chest"))),
                new LootTableFilter.ByIdFilter(new IdFilter.ByPattern(Pattern.compile("minecraft:blocks/.*")))
        );

        Assertions.assertTrue(modifier.predicate(table("chests/igloo_chest")));
        Assertions.assertTrue(modifier.predicate(table("blocks/stone")));
        Assertions.assertFalse(modifier.predicate(table("entities/zombie")));
        Assertions.assertEquals(ILootModifier.IType.LOOT_TABLE, modifier.getType());
    }

    @Test
    public void testTableModifierMatchesLootTypeFilter() {
        TableLootModifier modifier = tableModifier(new LootTableFilter.ByLootType(LootType.BLOCK));

        Assertions.assertTrue(modifier.predicate(table("blocks/stone")));
        Assertions.assertFalse(modifier.predicate(table("entities/zombie")));
    }

    @Test
    public void testTableModifierWithoutFiltersMatchesNothing() {
        Assertions.assertFalse(tableModifier().predicate(table("blocks/stone")));
    }

    @Test
    public void testTypeFilteredModifierMatchesAnyListedType() {
        TableLootModifier modifier = typeModifier(LootType.ENTITY, LootType.BLOCK);

        Assertions.assertTrue(modifier.predicate(table("blocks/stone")));
        Assertions.assertTrue(modifier.predicate(table("entities/zombie")));
        Assertions.assertFalse(modifier.predicate(table("chests/igloo_chest")));
        Assertions.assertEquals(ILootModifier.IType.LOOT_TABLE, modifier.getType());
    }

    @Test
    public void testTypeFilteredModifierWithoutTypesMatchesNothing() {
        Assertions.assertFalse(typeModifier().predicate(table("blocks/stone")));
    }

    private static Predicate<BlockState> capturing(BlockStatePredicate predicate) {
        return predicate::test;
    }

    private static Identifier table(String path) {
        return Identifier.withDefaultNamespace(path);
    }

    private static Entity entity(EntityType<?> type) {
        Entity entity = Mockito.mock(Entity.class);

        Mockito.doReturn(type).when(entity).getType();
        return entity;
    }

    private static LootModifier modifier() {
        return new LootModifier((context) -> true, ConstantValue.exactly(1), List.of(), List.of(), List.<LootAction>of(), "test", ItemFilter.ANY, false);
    }

    private static BlockLootModifier blockModifier(Predicate<BlockState> predicate) {
        return new BlockLootModifier(UTILS, modifier(), new LootModifier.BlockFiltered(predicate));
    }

    @SafeVarargs
    private static EntityLootModifier entityModifier(EntityType<?>... types) {
        return new EntityLootModifier(UTILS, modifier(), new LootModifier.EntityFiltered(HolderSet.direct(
                List.of(types).stream().map(EntityType::builtInRegistryHolder).toList()
        )));
    }

    private static TableLootModifier tableModifier(LootTableFilter... filters) {
        return new TableLootModifier(UTILS, modifier(), new LootModifier.TableFiltered(filters));
    }

    private static TableLootModifier typeModifier(LootType... types) {
        return new TableLootModifier(UTILS, modifier(), new LootModifier.TypeFiltered(types));
    }
}
