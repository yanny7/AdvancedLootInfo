package com.yanny.ali.test;

public class ConditionTooltipTest {/*
    @Test
    public void testAllOfTooltip() {
        IContext context = mock(IContext.class);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeOptional(Optional.of(10L), FriendlyByteBuf::writeLong);
        new RangeValue(1).encode(buf);
        new RangeValue(8).encode(buf);
        buf.writeOptional(Optional.of(true), FriendlyByteBuf::writeBoolean);
        buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeBoolean);

        assertTooltip(ConditionTooltipUtils.getAllOfTooltip(utils, 0, List.of(
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
        StatePropertiesPredicate propertiesPredicate = mock(StatePropertiesPredicate.class, Mockito.withSettings().extraInterfaces(MixinStatePropertiesPredicate.class));
        StatePropertiesPredicate.ExactPropertyMatcher exactPropertyMatcher = mock(StatePropertiesPredicate.ExactPropertyMatcher.class, Mockito.withSettings().extraInterfaces(MixinStatePropertiesPredicate.ExactPropertyMatcher.class));
        StatePropertiesPredicate.RangedPropertyMatcher rangedPropertyMatcher = mock(StatePropertiesPredicate.RangedPropertyMatcher.class, Mockito.withSettings().extraInterfaces(MixinStatePropertiesPredicate.RangedPropertyMatcher.class));
        MixinStatePropertiesPredicate mixinPropertiesPredicate = ((MixinStatePropertiesPredicate) propertiesPredicate);
        MixinStatePropertiesPredicate.ExactPropertyMatcher mixinExactPropertyMatcher = ((MixinStatePropertiesPredicate.ExactPropertyMatcher) exactPropertyMatcher);
        MixinStatePropertiesPredicate.RangedPropertyMatcher mixinRangedPropertyMatcher = ((MixinStatePropertiesPredicate.RangedPropertyMatcher) rangedPropertyMatcher);

        when(mixinPropertiesPredicate.getProperties()).thenReturn(List.of());
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(0, Blocks.FURNACE, propertiesPredicate), List.of(
                "Block State Property:",
                "  -> Block: Furnace"
        ));

        when(mixinPropertiesPredicate.getProperties()).thenReturn(List.of(
                exactPropertyMatcher,
                rangedPropertyMatcher,
                rangedPropertyMatcher,
                rangedPropertyMatcher,
                rangedPropertyMatcher
        ));
        when(mixinExactPropertyMatcher.getName()).thenReturn("facing");
        when(mixinExactPropertyMatcher.getValue()).thenReturn("east");
        when(mixinRangedPropertyMatcher.getName()).thenReturn("level", "level", "level", "level");
        when(mixinRangedPropertyMatcher.getMinValue()).thenReturn("1", null, "1", null);
        when(mixinRangedPropertyMatcher.getMaxValue()).thenReturn("5", "5", null, null);
        assertTooltip(ConditionTooltipUtils.getBlockStatePropertyTooltip(0, Blocks.BAMBOO, propertiesPredicate), List.of(
                "Block State Property:",
                "  -> Block: Bamboo",
                "  -> State Properties:",
                "    -> facing: east",
                "    -> level: 1-5",
                "    -> level: ≤5",
                "    -> level: ≥1",
                "    -> level: any"
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDamageSourceProperties() {
        MixinDamageSourcePredicate damageSourcePredicate = (MixinDamageSourcePredicate) mock(DamageSourcePredicate.class, withSettings().extraInterfaces(MixinDamageSourcePredicate.class));
        TagPredicate<DamageType> tagPredicate1 = mock(TagPredicate.class, withSettings().extraInterfaces(MixinTagPredicate.class));
        TagPredicate<DamageType> tagPredicate2 = mock(TagPredicate.class, withSettings().extraInterfaces(MixinTagPredicate.class));
        MixinEntityPredicate sourcePredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));
        MixinEntityPredicate directPredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));
        MixinEntityTypePredicate.TypePredicate typePredicate = (MixinEntityTypePredicate.TypePredicate) mock(EntityTypePredicate.TypePredicate.class, withSettings().extraInterfaces(MixinEntityTypePredicate.TypePredicate.class));

        MixinTagPredicate<DamageType> mixinTagPredicate1 = (MixinTagPredicate<DamageType>) tagPredicate1;
        MixinTagPredicate<DamageType> mixinTagPredicate2 = (MixinTagPredicate<DamageType>) tagPredicate2;

        when((EntityType<Warden>)typePredicate.getType()).thenReturn(EntityType.WARDEN);

        when(directPredicate.getEntityType()).thenReturn((EntityTypePredicate.TypePredicate) typePredicate);
        when(directPredicate.getDistanceToPlayer()).thenReturn(DistancePredicate.ANY);
        when(directPredicate.getLocation()).thenReturn(LocationPredicate.ANY);
        when(directPredicate.getSteppingOnLocation()).thenReturn(LocationPredicate.ANY);
        when(directPredicate.getEffects()).thenReturn(MobEffectsPredicate.ANY);
        when(directPredicate.getNbt()).thenReturn(NbtPredicate.ANY);
        when(directPredicate.getFlags()).thenReturn(EntityFlagsPredicate.ANY);
        when(directPredicate.getEquipment()).thenReturn(EntityEquipmentPredicate.ANY);
        when(directPredicate.getSubPredicate()).thenReturn(EntitySubPredicate.ANY);
        when(directPredicate.getVehicle()).thenReturn(EntityPredicate.ANY);
        when(directPredicate.getPassenger()).thenReturn(EntityPredicate.ANY);
        when(directPredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY);

        when(sourcePredicate.getEntityType()).thenReturn(EntityTypePredicate.ANY);
        when(sourcePredicate.getDistanceToPlayer()).thenReturn(DistancePredicate.ANY);
        when(sourcePredicate.getLocation()).thenReturn(LocationPredicate.ANY);
        when(sourcePredicate.getSteppingOnLocation()).thenReturn(LocationPredicate.ANY);
        when(sourcePredicate.getEffects()).thenReturn(MobEffectsPredicate.ANY);
        when(sourcePredicate.getNbt()).thenReturn(NbtPredicate.ANY);
        when(sourcePredicate.getFlags()).thenReturn(EntityFlagsPredicate.ANY);
        when(sourcePredicate.getEquipment()).thenReturn(EntityEquipmentPredicate.ANY);
        when(sourcePredicate.getSubPredicate()).thenReturn(EntitySubPredicate.ANY);
        when(sourcePredicate.getVehicle()).thenReturn(EntityPredicate.ANY);
        when(sourcePredicate.getPassenger()).thenReturn(EntityPredicate.ANY);
        when(sourcePredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY);
        when(sourcePredicate.getTeam()).thenReturn("Blue");

        when(mixinTagPredicate1.getTag()).thenReturn(DamageTypeTags.BYPASSES_ARMOR);
        when(mixinTagPredicate1.getExpected()).thenReturn(true);
        when(mixinTagPredicate2.getTag()).thenReturn(DamageTypeTags.IS_EXPLOSION);
        when(mixinTagPredicate2.getExpected()).thenReturn(false);

        when(damageSourcePredicate.getTags()).thenReturn(List.of(tagPredicate1, tagPredicate2));
        when(damageSourcePredicate.getSourceEntity()).thenReturn((EntityPredicate) sourcePredicate);
        when(damageSourcePredicate.getDirectEntity()).thenReturn((EntityPredicate) directPredicate);

        assertTooltip(ConditionTooltipUtils.getDamageSourcePropertiesTooltip(0, (DamageSourcePredicate) damageSourcePredicate), List.of(
                "Damage Source Properties:",
                "  -> Damage Source:",
                "    -> Tags:",
                "      -> minecraft:bypasses_armor: true",
                "      -> minecraft:is_explosion: false",
                "    -> Direct Entity:",
                "      -> Entity Type: Warden",
                "    -> Source Entity:",
                "      -> Team: Blue"
        ));
    }

    @Test
    public void testEntityPropertiesTooltip() {
        MixinEntityPredicate entityPredicate = (MixinEntityPredicate) mock(EntityPredicate.class, withSettings().extraInterfaces(MixinEntityPredicate.class));

        when(entityPredicate.getEntityType()).thenReturn(EntityTypePredicate.ANY);
        when(entityPredicate.getDistanceToPlayer()).thenReturn(DistancePredicate.ANY);
        when(entityPredicate.getLocation()).thenReturn(LocationPredicate.ANY);
        when(entityPredicate.getSteppingOnLocation()).thenReturn(LocationPredicate.ANY);
        when(entityPredicate.getEffects()).thenReturn(MobEffectsPredicate.ANY);
        when(entityPredicate.getNbt()).thenReturn(NbtPredicate.ANY);
        when(entityPredicate.getFlags()).thenReturn(EntityFlagsPredicate.ANY);
        when(entityPredicate.getEquipment()).thenReturn(EntityEquipmentPredicate.ANY);
        when(entityPredicate.getSubPredicate()).thenReturn(EntitySubPredicate.ANY);
        when(entityPredicate.getVehicle()).thenReturn(EntityPredicate.ANY);
        when(entityPredicate.getPassenger()).thenReturn(EntityPredicate.ANY);
        when(entityPredicate.getTargetedEntity()).thenReturn(EntityPredicate.ANY);
        when(entityPredicate.getTeam()).thenReturn("blue");

        assertTooltip(ConditionTooltipUtils.getEntityPropertiesTooltip(0, LootContext.EntityTarget.KILLER, (EntityPredicate) entityPredicate), List.of(
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
        MixinLocationPredicate locationPredicate = (MixinLocationPredicate) mock(LocationPredicate.class, withSettings().extraInterfaces(MixinLocationPredicate.class));

        when(locationPredicate.getX()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(locationPredicate.getY()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(locationPredicate.getZ()).thenReturn(MinMaxBounds.Doubles.ANY);
        when(locationPredicate.getLight()).thenReturn(LightPredicate.ANY);
        when(locationPredicate.getBlock()).thenReturn(BlockPredicate.ANY);
        when(locationPredicate.getFluid()).thenReturn(FluidPredicate.ANY);
        when(locationPredicate.getSmokey()).thenReturn(true);

        assertTooltip(ConditionTooltipUtils.getLocationCheckTooltip(0, new BlockPos(2, 4, 6), (LocationPredicate) locationPredicate), List.of(
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
        MixinItemPredicate itemPredicate = (MixinItemPredicate) mock(ItemPredicate.class, withSettings().extraInterfaces(MixinItemPredicate.class));

        Set<Item> items = new LinkedHashSet<>();

        items.add(Items.ANDESITE);
        items.add(Items.DIORITE);

        when(itemPredicate.getItems()).thenReturn(items);
        when(itemPredicate.getCount()).thenReturn(MinMaxBounds.Ints.ANY);
        when(itemPredicate.getDurability()).thenReturn(MinMaxBounds.Ints.ANY);
        when(itemPredicate.getEnchantments()).thenReturn(new EnchantmentPredicate[0]);
        when(itemPredicate.getStoredEnchantments()).thenReturn(new EnchantmentPredicate[0]);
        when(itemPredicate.getNbt()).thenReturn(NbtPredicate.ANY);

        assertTooltip(ConditionTooltipUtils.getMatchToolTooltip(0, (ItemPredicate) itemPredicate), List.of(
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
        assertTooltip(ConditionTooltipUtils.getReferenceTooltip(0, new ResourceLocation("test")), List.of("Reference: minecraft:test"));
    }

    @Test
    public void testSurvivesExplosionTooltip() {
        assertTooltip(ConditionTooltipUtils.getSurvivesExplosionTooltip(0), List.of("Must survive explosion"));
    }

    @Test
    public void testTableBonusTooltip() {
        assertTooltip(ConditionTooltipUtils.getTableBonusTooltip(0, Enchantments.MOB_LOOTING, new float[]{0.25F, 0.5555F, 0.99F}), List.of(
                "Table Bonus:",
                "  -> Enchantment: Looting",
                "  -> Values: [0.25, 0.5555, 0.99]" //FIXME to 2 decimal places
        ));
    }

    @Test
    public void testTimeCheckTooltip() {
        assertTooltip(ConditionTooltipUtils.getTimeCheckTooltip(0, 24000L, new RangeValue(5), new RangeValue(10)), List.of(
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
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, true, false), List.of(
                "Weather Check:",
                "  -> Is Raining: true",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, true, null), List.of(
                "Weather Check:",
                "  -> Is Raining: true"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, null, false), List.of(
                "Weather Check:",
                "  -> Is Thundering: false"
        ));
        assertTooltip(ConditionTooltipUtils.getWeatherCheckTooltip(0, null, null), List.of("Weather Check:"));
    }*/
}
