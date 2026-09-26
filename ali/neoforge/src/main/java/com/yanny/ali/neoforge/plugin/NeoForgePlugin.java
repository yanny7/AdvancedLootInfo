package com.yanny.ali.neoforge.plugin;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.yanny.aci.api.RangeValue;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.aci.tooltip.TooltipNode;
import com.yanny.ali.api.*;
import com.yanny.ali.language.Lang;
import com.yanny.ali.neoforge.mixin.*;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.trades.ItemsToItemsNode;
import com.yanny.ali.plugin.glm.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.crafting.*;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AliEntrypoint
public class NeoForgePlugin implements IGlobalLootModifierPlugin {
    @NotNull
    @Override
    public String getModId() {
        return "neoforge";
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(CanItemPerformAbility.class, NeoForgePlugin::getCanToolPerformActionTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.class, NeoForgePlugin::getLootTableIdTooltip);

        registry.registerLootContextPreparer(NeoForgePlugin::prepareLootContext);

        registry.registerIngredientUnwrapper(NeoForgePlugin::unwrapCustomIngredient);

        registry.registerValueTooltip(CompoundIngredient.class, NeoForgeIngredientTooltipUtils::getCompoundIngredientTooltip);
        registry.registerValueTooltip(DifferenceIngredient.class, NeoForgeIngredientTooltipUtils::getDifferenceIngredientTooltip);
        registry.registerValueTooltip(IntersectionIngredient.class, NeoForgeIngredientTooltipUtils::getIntersectionIngredientTooltip);
        registry.registerValueTooltip(DataComponentIngredient.class, NeoForgeIngredientTooltipUtils::getDataComponentIngredientTooltip);
        registry.registerValueTooltip(BlockTagIngredient.class, NeoForgeIngredientTooltipUtils::getBlockTagIngredientTooltip);

        registry.registerItemListing(BasicItemListing.class, NeoForgePlugin::getBasicItemListingNode);

        registry.registerGlobalLootModifiers(NeoForgePlugin::registerLootModifiers);

        registry.registerValueTooltip(ItemAbility.class, NeoForgePlugin::getItemAbilityTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IRegistry registry) {
        registry.registerGlobalLootModifier(AddTableLootModifier.class, NeoForgePlugin::getAddTableLootModifier);
    }

    @Nullable
    private static ICustomIngredient unwrapCustomIngredient(Ingredient ingredient) {
        return ingredient.isCustom() ? ingredient.getCustomIngredient() : null;
    }

    @NotNull
    public static ItemsToItemsNode getBasicItemListingNode(IServerUtils utils, BasicItemListing listing, TooltipNode condition) {
        MixinBasicItemListing accessor = (MixinBasicItemListing) listing;

        return new ItemsToItemsNode(
                utils,
                Either.left(accessor.getPrice()),
                new RangeValue(accessor.getPrice().getCount()),
                Either.left(accessor.getPrice2()),
                new RangeValue(accessor.getPrice2().getCount()),
                Either.left(accessor.getForSale()),
                new RangeValue(accessor.getForSale().getCount()),
                accessor.getMaxTrades(),
                accessor.getXp(),
                accessor.getPriceMult(),
                condition
        );
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getCanToolPerformActionTooltip(IServerUtils utils, CanItemPerformAbility condition) {
        MixinCanItemPerformAbility cond = (MixinCanItemPerformAbility) condition;
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getAbility())), Lang.Conditions.CAN_ITEM_PERFORM_ABILITY);
    }

    @Unmodifiable
    @NotNull
    public static TooltipBuilder getLootTableIdTooltip(IServerUtils utils, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.getTargetLootTableId())), Lang.Conditions.LOOT_TABLE_ID);
    }

    private static TooltipBuilder getItemAbilityTooltip(IServerUtils utils, ItemAbility ability) {
        return utils.getValueTooltip(utils, ability.name());
    }

    @NotNull
    private static List<IPageLootModifier> registerLootModifiers(IServerUtils utils) {
        return GlobalLootModifierCollector.collect(utils, MixinNeoForgeEventHandler.getLootModifierManager().getAllLootMods().stream().map((m) -> wrap(utils, m)).toList());
    }

    @NotNull
    private static Optional<IPageLootModifier> getAddTableLootModifier(IServerUtils utils, AddTableLootModifier modifier) {
        List<LootItemCondition> conditionList = Arrays.asList(((MixinLootModifier) modifier).getAliConditions());

        return Optional.of(GlobalLootModifierUtils.getLootModifier(utils, modifier, conditionList, (page, c) -> {
            TooltipNode tooltip = TooltipBuilder.array((b) -> b
                            .add(TooltipBuilder.keyOnly(Lang.Group.ALL))
                            .add(utils.getValueTooltip(utils, c))
                    )
                    .build();
            IDataNode node = NodeUtils.getReferenceNode(utils, ((MixinAddTableLootModifier) modifier).getTable().identifier(), c, tooltip);
            return List.of(new IOperation.AddOperation((i) -> true, node));
        }));
    }

    private static void prepareLootContext(IServerUtils ignoredUtils, LootContext context, LootPage page) {
        context.setQueriedLootTableId(page.tableId());
    }

    @NotNull
    private static IGlobalLootModifierWrapper wrap(IServerUtils utils, IGlobalLootModifier modifier) {
        return new GlobalLootModifierWrapper(
                NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.getKey(modifier.codec()),
                modifier,
                LootModifier.class,
                () -> Arrays.asList(((MixinLootModifier) modifier).getAliConditions()),
                () -> serialize(utils, modifier)
        );
    }

    @NotNull
    private static JsonElement serialize(IServerUtils utils, IGlobalLootModifier modifier) {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, utils.lookupProvider());
        //noinspection unchecked
        MapCodec<IGlobalLootModifier> codec = ((MapCodec<IGlobalLootModifier>) modifier.codec());
        return codec.codec().encodeStart(registryOps, modifier).getPartialOrThrow();
    }
}
