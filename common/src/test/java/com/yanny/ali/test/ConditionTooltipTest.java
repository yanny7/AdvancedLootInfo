package com.yanny.ali.test;

import com.yanny.ali.api.IContext;
import com.yanny.ali.api.RangeValue;
import com.yanny.ali.plugin.ConditionTooltipUtils;
import com.yanny.ali.plugin.condition.TimeCheckAliCondition;
import com.yanny.ali.plugin.condition.WeatherCheckAliCondition;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.yanny.ali.test.utils.TestUtils.assertTooltip;
import static org.mockito.Mockito.mock;

public class ConditionTooltipTest {
    @Test
    public void testAllOfTooltip() {
        IContext context = mock(IContext.class);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeOptional(Optional.of(10L), FriendlyByteBuf::writeLong);
        new RangeValue(1).encode(buf);
        new RangeValue(8).encode(buf);
        buf.writeOptional(Optional.of(true), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeBoolean);

        assertTooltip(ConditionTooltipUtils.getAllOfTooltip(0, List.of(
                new TimeCheckAliCondition(context, new FriendlyByteBuf(buf)),
                new WeatherCheckAliCondition(context, new FriendlyByteBuf(buf))
        )), List.of(
                "All must pass:",
                "  -> Time Check:",
                "    -> Period: 10",
                "    -> Value: 1 - 8",
                "  -> Weather Check:",
                "    -> Is Raining: true"
        ));
    }

    @Test
    public void testAnyOfTooltip() {
        IContext context = mock(IContext.class);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeOptional(Optional.of(10L), FriendlyByteBuf::writeLong);
        new RangeValue(1).encode(buf);
        new RangeValue(8).encode(buf);
        buf.writeOptional(Optional.of(true), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeBoolean);

        assertTooltip(ConditionTooltipUtils.getAnyOfTooltip(0, List.of(
                new TimeCheckAliCondition(context, new FriendlyByteBuf(buf)),
                new WeatherCheckAliCondition(context, new FriendlyByteBuf(buf))
        )), List.of(
                "Any of:",
                "  -> Time Check:",
                "    -> Period: 10",
                "    -> Value: 1 - 8",
                "  -> Weather Check:",
                "    -> Is Raining: true"
        ));
    }

    @Test
    public void testBlockStatePropertyTooltip() {
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(0, Holder.direct(Blocks.FURNACE), Optional.of(new StatePropertiesPredicate(List.of()))), List.of(
                "Block State Property:",
                "  -> Block: Furnace"
        ));

        List<StatePropertiesPredicate.PropertyMatcher> properties = new LinkedList<>();
        Optional<StatePropertiesPredicate> predicate = Optional.of(new StatePropertiesPredicate(properties));

        properties.add(new StatePropertiesPredicate.PropertyMatcher("facing", new StatePropertiesPredicate.ExactMatcher("east")));
        properties.add(new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.of("1"), Optional.of("5"))));
        properties.add(new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.empty(), Optional.of("5"))));
        properties.add(new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.of("1"), Optional.empty())));
        properties.add(new StatePropertiesPredicate.PropertyMatcher("level", new StatePropertiesPredicate.RangedMatcher(Optional.empty(), Optional.empty())));

        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(0, Holder.direct(Blocks.BAMBOO), predicate), List.of(
                "Block State Property:",
                "  -> Block: Bamboo",
                "  -> State Properties:",
                "    -> facing: east",
                "    -> level: 1-5",
                "    -> level: 5≥",
                "    -> level: ≥1",
                "    -> level: any"
        ));
    }

    @Test
    public void testDamageSourceProperties() {
        Optional<DamageSourcePredicate> predicate = Optional.of(DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(DamageTypeTags.BYPASSES_ARMOR))
                .tag(TagPredicate.isNot(DamageTypeTags.IS_EXPLOSION))
                .direct(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.WARDEN)))
                .source(EntityPredicate.Builder.entity().team("Blue"))
                .build());

        assertTooltip(ConditionTooltipUtils.getDamageSourcePropertiesTooltip(0, predicate), List.of(
                "Damage Source Properties:",
                "  -> Damage Source:",
                "    -> Tags:",
                "      -> minecraft:bypasses_armor: true",
                "      -> minecraft:is_explosion: false",
                "    -> Direct Entity:",
                "      -> Entity Types:",
                "        -> Entity Type: Warden",
                "    -> Source Entity:",
                "      -> Team: Blue"
        ));
    }

    @Test
    public void testEntityPropertiesTooltip() {
        Optional<EntityPredicate> entityPredicate = Optional.of(EntityPredicate.Builder.entity().team("blue").build());

        assertTooltip(ConditionTooltipUtils.getEntityPropertiesTooltip(0, LootContext.EntityTarget.KILLER, entityPredicate), List.of(
            "Entity Properties:",
            "  -> Target: Killer Entity",
            "  -> Predicate:",
            "    -> Team: blue"
        ));
    }

    @Test
    public void testEntityScoresTooltip() {
        Map<String, Tuple<RangeValue, RangeValue>> scores = new LinkedHashMap<>();

        scores.put("single", new Tuple<>(new RangeValue(2), new RangeValue(5)));
        scores.put("double", new Tuple<>(new RangeValue(1), new RangeValue(7)));

        assertTooltip(ConditionTooltipUtils.getEntityScoresTooltip(0, LootContext.EntityTarget.DIRECT_KILLER, Map.of()), List.of(
                "Entity Scores:",
                "  -> Target: Directly Killed By"
        ));
        assertTooltip(ConditionTooltipUtils.getEntityScoresTooltip(0, LootContext.EntityTarget.DIRECT_KILLER, scores), List.of(
                "Entity Scores:",
                "  -> Target: Directly Killed By",
                "  -> Scores:",
                "    -> single: 2 - 5",
                "    -> double: 1 - 7"
        ));
    }

    @Test
    public void testInvertedTooltip() {
        IContext context = mock(IContext.class);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeOptional(Optional.of(10L), FriendlyByteBuf::writeLong);
        new RangeValue(1).encode(buf);
        new RangeValue(8).encode(buf);

        assertTooltip(ConditionTooltipUtils.getInvertedTooltip(0, new TimeCheckAliCondition(context, buf)), List.of(
                "Inverted:",
                "  -> Time Check:",
                "    -> Period: 10",
                "    -> Value: 1 - 8"
        ));
    }

    @Test
    public void testKilledByPlayerTooltip() {
        assertTooltip(ConditionTooltipUtils.getKilledByPlayerTooltip(0), List.of("Must be killed by player"));
    }

    @Test
    public void testLocationCheckTooltip() {
        Optional<LocationPredicate> locationPredicate = Optional.of(LocationPredicate.Builder.location().setSmokey(true).build());

        assertTooltip(ConditionTooltipUtils.getLocationCheckTooltip(0, new BlockPos(2, 4, 6), locationPredicate), List.of(
                "Location Check:",
                "  -> Location:",
                "    -> Smokey: true",
                "  -> Offset:",
                "    -> X: 2",
                "    -> Y: 4",
                "    -> Z: 6"
        ));
    }

    @Test
    public void testItemMatchTooltip() {
        Optional<ItemPredicate> itemPredicate = Optional.of(ItemPredicate.Builder.item().of(Items.ANDESITE, Items.DIORITE).build());

        assertTooltip(ConditionTooltipUtils.getMatchToolTooltip(0, itemPredicate), List.of(
                "Match Tool:",
                "  -> Items:",
                "    -> Item: Andesite",
                "    -> Item: Diorite"
        ));
    }

    @Test
    public void testRandomChanceTooltip() {
        assertTooltip(ConditionTooltipUtils.getRandomChanceTooltip(0, 0.25F), List.of(
                "Random Chance:",
                "  -> Probability: 0.25"
        ));
    }

    @Test
    public void testRandomChanceWithLootingTooltip() {
        assertTooltip(ConditionTooltipUtils.getRandomChanceWithLootingTooltip(0, 0.25F, 5F), List.of(
                "Random Chance With Looting:",
                "  -> Percent: 0.25",
                "  -> Multiplier: 5.0"
        ));
    }

    @Test
    public void testReferenceTooltip() {
        assertTooltip(ConditionTooltipUtils.getReferenceTooltip(0, ResourceKey.create(Registries.PREDICATE, new ResourceLocation("test"))), List.of("Reference: minecraft:test"));
    }

    @Test
    public void testSurvivesExplosionTooltip() {
        assertTooltip(ConditionTooltipUtils.getSurvivesExplosionTooltip(0), List.of("Must survive explosion"));
    }

    @Test
    public void testTableBonusTooltip() {
        List<Float> values = new LinkedList<>();

        values.add(0.25F);
        values.add(0.5555F);
        values.add(0.99F);

        assertTooltip(ConditionTooltipUtils.getTableBonusTooltip(0, Holder.direct(Enchantments.LOOTING), values), List.of(
                "Table Bonus:",
                "  -> Enchantment: Looting",
                "  -> Values: [0.25, 0.5555, 0.99]" //FIXME to 2 decimal places
        ));
    }

    @Test
    public void testTimeCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getTimeCheckTooltip(0, Optional.of(24000L), new RangeValue(5), new RangeValue(10)), List.of(
                "Time Check:",
                "  -> Period: 24000",
                "  -> Value: 5 - 10"
        ));
    }

    @Test
    public void testValueCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getValueCheckTooltip(0, new RangeValue(1, 20), new RangeValue(1, 5), new RangeValue(1, 10)), List.of(
                "Value Check:",
                "  -> Provider: 1-20",
                "  -> Range: 1-5 - 1-10"
        ));
    }

    @Test
    public void testWeatherCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, Optional.of(true), Optional.of(false)), List.of(
                "Weather Check:",
                "  -> Is Raining: true",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, Optional.of(true), Optional.empty()), List.of(
                "Weather Check:",
                "  -> Is Raining: true"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, Optional.empty(), Optional.of(false)), List.of(
                "Weather Check:",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, Optional.empty(), Optional.empty()), List.of("Weather Check:"));
    }
}
