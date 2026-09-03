package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.core.*;
import com.almostreliable.lootjs.filters.ResourceLocationFilter;
import com.yanny.ali.api.ILootModifier;
import com.yanny.ali.lootjs.BlockLootModifier;
import com.yanny.ali.lootjs.EntityLootModifier;
import com.yanny.ali.lootjs.TableLootModifier;
import com.yanny.ali.lootjs.TypeLootModifier;
import com.yanny.ali.lootjs.mixin.*;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.yanny.ali.lootjs.test.LootJsTestUtils.mock;
import static com.yanny.ali.test.TooltipTestSuite.UTILS;

public class LootModifierScopeTest {
    @Test
    public void testBlockModifierWithoutPredicateMatchesEverything() {
        BlockLootModifier modifier = blockModifier(null);

        Assertions.assertTrue(modifier.predicate(Blocks.STONE));
        Assertions.assertTrue(modifier.predicate(Blocks.DIRT));
        Assertions.assertEquals(ILootModifier.IType.BLOCK, modifier.getType());
    }

    @Test
    public void testBlockModifierUsesCapturedPredicate() {
        BlockStatePredicate none = BlockStatePredicate.Simple.NONE;
        BlockStatePredicate all = BlockStatePredicate.Simple.ALL;

        Assertions.assertFalse(blockModifier(capturing(none)).predicate(Blocks.STONE));
        Assertions.assertTrue(blockModifier(capturing(all)).predicate(Blocks.STONE));
    }

    @Test
    public void testBlockModifierRejectsPredicateWithoutCapturedInstance() {
        Predicate<BlockState> notCapturing = (state) -> true;

        Assertions.assertThrows(IllegalStateException.class, () -> blockModifier(notCapturing));
    }

    @Test
    public void testBlockModifierRejectsPredicateWithMultipleCapturedInstances() {
        BlockStatePredicate all = BlockStatePredicate.Simple.ALL;
        BlockStatePredicate none = BlockStatePredicate.Simple.NONE;
        Predicate<BlockState> both = (state) -> all.test(state) && none.test(state);

        Assertions.assertThrows(IllegalStateException.class, () -> blockModifier(both));
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
                new ResourceLocationFilter.ByLocation(new ResourceLocation("minecraft", "chests/igloo_chest")),
                new ResourceLocationFilter.ByPattern(Pattern.compile("minecraft:blocks/.*"))
        );

        Assertions.assertTrue(modifier.predicate(new ResourceLocation("minecraft", "chests/igloo_chest")));
        Assertions.assertTrue(modifier.predicate(new ResourceLocation("minecraft", "blocks/stone")));
        Assertions.assertFalse(modifier.predicate(new ResourceLocation("minecraft", "entities/zombie")));
        Assertions.assertEquals(ILootModifier.IType.LOOT_TABLE, modifier.getType());
    }

    @Test
    public void testTableModifierWithoutFiltersMatchesNothing() {
        Assertions.assertFalse(tableModifier().predicate(new ResourceLocation("minecraft", "blocks/stone")));
    }

    @Test
    public void testTypeModifierMatchesPathPerContextType() {
        Assertions.assertTrue(typeModifier(LootContextType.BLOCK).predicate(table("blocks/stone")));
        Assertions.assertFalse(typeModifier(LootContextType.BLOCK).predicate(table("entities/zombie")));
        Assertions.assertTrue(typeModifier(LootContextType.ENTITY).predicate(table("entities/zombie")));
        Assertions.assertTrue(typeModifier(LootContextType.CHEST).predicate(table("chests/igloo_chest")));
        Assertions.assertTrue(typeModifier(LootContextType.FISHING).predicate(table("gameplay/fishing/fish")));
        Assertions.assertTrue(typeModifier(LootContextType.GIFT).predicate(table("gameplay/hero_of_the_village/farmer_gift")));
        Assertions.assertTrue(typeModifier(LootContextType.PIGLIN_BARTER).predicate(table("gameplay/piglin_bartering")));
        Assertions.assertEquals(ILootModifier.IType.LOOT_TABLE, typeModifier(LootContextType.BLOCK).getType());
    }

    @Test
    public void testTypeModifierNeverMatchesUnsupportedContextTypes() {
        Assertions.assertFalse(typeModifier(LootContextType.UNKNOWN).predicate(table("blocks/stone")));
        Assertions.assertFalse(typeModifier(LootContextType.ADVANCEMENT_ENTITY).predicate(table("blocks/stone")));
        Assertions.assertFalse(typeModifier(LootContextType.ADVANCEMENT_REWARD).predicate(table("blocks/stone")));
        Assertions.assertFalse(typeModifier().predicate(table("blocks/stone")));
    }

    @Test
    public void testTypeModifierMatchesAnyListedContextType() {
        TypeLootModifier modifier = typeModifier(LootContextType.ENTITY, LootContextType.BLOCK);

        Assertions.assertTrue(modifier.predicate(table("blocks/stone")));
        Assertions.assertTrue(modifier.predicate(table("entities/zombie")));
        Assertions.assertFalse(modifier.predicate(table("chests/igloo_chest")));
    }

    private static Predicate<BlockState> capturing(BlockStatePredicate predicate) {
        return predicate::test;
    }

    private static ResourceLocation table(String path) {
        return new ResourceLocation("minecraft", path);
    }

    private static Entity entity(EntityType<?> type) {
        Entity entity = Mockito.mock(Entity.class);

        Mockito.doReturn(type).when(entity).getType();
        return entity;
    }

    private static BlockLootModifier blockModifier(Predicate<BlockState> predicate) {
        LootModificationByBlock modification = mock(LootModificationByBlock.class, MixinLootModificationByBlock.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinCompositeLootAction) modification).getHandlers()).thenReturn(List.of());
        Mockito.when(((MixinLootModificationByBlock) modification).getPredicate()).thenReturn(predicate);
        return new BlockLootModifier(UTILS, modification);
    }

    private static EntityLootModifier entityModifier(EntityType<?>... types) {
        LootModificationByEntity modification = mock(LootModificationByEntity.class, MixinLootModificationByEntity.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinCompositeLootAction) modification).getHandlers()).thenReturn(List.of());
        Mockito.when(((MixinLootModificationByEntity) modification).getEntities()).thenReturn(new HashSet<>(List.of(types)));
        return new EntityLootModifier(UTILS, modification);
    }

    private static TableLootModifier tableModifier(ResourceLocationFilter... filters) {
        LootModificationByTable modification = mock(LootModificationByTable.class, MixinLootModificationByTable.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinCompositeLootAction) modification).getHandlers()).thenReturn(List.of());
        Mockito.when(((MixinLootModificationByTable) modification).getFilters()).thenReturn(filters);
        return new TableLootModifier(UTILS, modification);
    }

    private static TypeLootModifier typeModifier(LootContextType... types) {
        LootModificationByType modification = mock(LootModificationByType.class, MixinLootModificationByType.class, MixinCompositeLootAction.class);

        Mockito.when(((MixinCompositeLootAction) modification).getHandlers()).thenReturn(List.of());
        Mockito.when(((MixinLootModificationByType) modification).getTypes()).thenReturn(List.of(types));
        return new TypeLootModifier(UTILS, modification);
    }
}
