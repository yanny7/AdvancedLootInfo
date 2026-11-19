package com.yanny.alicompat.compat.bumblezone;

import com.telepathicgrunt.the_bumblezone.loot.NewLootInjectorApplier;
import com.telepathicgrunt.the_bumblezone.loot.conditions.EssenceOnlySpawn;
import com.telepathicgrunt.the_bumblezone.loot.functions.DropContainerItems;
import com.telepathicgrunt.the_bumblezone.loot.functions.HoneyCompassLocateStructure;
import com.telepathicgrunt.the_bumblezone.loot.functions.TagItemRemovals;
import com.telepathicgrunt.the_bumblezone.loot.functions.UniquifyIfHasItems;
import com.telepathicgrunt.the_bumblezone.modinit.BzEntities;
import com.telepathicgrunt.the_bumblezone.utils.GeneralUtils;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.plugin.glm.IPageLootModifier;
import com.yanny.alicompat.IModCompat;
import com.yanny.alicompat.accessor.PluginUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class BumblezoneCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return BumblezoneLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(EssenceOnlySpawn.class, BumblezoneCompat::getEssenceOnlySpawnTooltip);

        PluginUtils.registerFunctionTooltip(registry, DropContainerItems.class, DropContainerItemsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, UniquifyIfHasItems.class, UniquifyIfHasItemsAccessor::new);
        PluginUtils.registerFunctionTooltip(registry, TagItemRemovals.class, TagItemRemovalsAccessor.class);
        PluginUtils.registerFunctionTooltip(registry, HoneyCompassLocateStructure.class, HoneyCompassLocateStructureAccessor.class);

        PluginUtils.registerItemListing(registry, GeneralUtils.BasicItemTrade.class, BasicItemTradeAccessor.class);

        registry.registerPageResolver(BeeStingerLootInjection.class, BeeStingerLootInjection::test);
        registry.registerPageResolver(DimensionFishingLootInjection.class, DimensionFishingLootInjection::test);
        registry.registerGlobalLootModifiers(BumblezoneCompat::getLootModifiers);

        registry.registerLootTableReference(NewLootInjectorApplier.STINGER_DROP_LOOT_TABLE_RL);
        registry.registerEntityLootTable(BzEntities.COSMIC_CRYSTAL_ENTITY.get(), new ResourceLocation(BumblezoneLang.MOD_ID, "entities/cosmic_crystal"));
    }

    @Unmodifiable
    @NotNull
    private static List<IPageLootModifier> getLootModifiers(IServerUtils utils) {
        return List.of(new BeeStingerLootInjection().getLootModifier(utils), new DimensionFishingLootInjection().getLootModifier(utils));
    }

    @NotNull
    private static TooltipBuilder getEssenceOnlySpawnTooltip(IServerUtils ignoredUtils, EssenceOnlySpawn ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, BumblezoneLang.Conditions.ESSENCE_ONLY_SPAWN);
    }
}
