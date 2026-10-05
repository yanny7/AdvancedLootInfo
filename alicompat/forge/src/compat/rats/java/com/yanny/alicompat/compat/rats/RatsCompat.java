package com.yanny.alicompat.compat.rats;

import com.github.alexthe666.rats.RatsMod;
import com.github.alexthe666.rats.server.loot.GenericAddItemLootModifier;
import com.github.alexthe666.rats.server.loot.RatHasPlagueCondition;
import com.github.alexthe666.rats.server.loot.RatHasTogaInRatlantisCondition;
import com.github.alexthe666.rats.server.loot.RatKilledAndHasUpgradeCondition;
import com.github.alexthe666.rats.server.loot.RatlantisLoadedLootCondition;
import com.github.alexthe666.rats.server.misc.PlagueDoctorTrades;
import com.yanny.aci.api.NumberExpr;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.api.TradeLevelInfo;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.GlobalLootModifierUtils;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.ali.plugin.glm.LootPage;
import com.yanny.ali.plugin.glm.ParamState;
import com.yanny.ali.plugin.glm.Verdict;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

public class RatsCompat implements IGlmModCompat {
    private static final int COMMON_LEVEL = 1;
    private static final int RARE_LEVEL = 2;
    private static final int DESPAWNING_LEVEL = 3;
    private static final int COMMON_TRADE_COUNT = 5;
    private static final int RARE_TRADE_COUNT = 3;
    private static final int DESPAWNING_TRADE_COUNT = 3;

    @NotNull
    @Override
    public String targetModId() {
        return RatsLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(RatHasPlagueCondition.class, RatsCompat::getRatHasPlagueTooltip);
        registry.registerConditionTooltip(RatHasTogaInRatlantisCondition.class, RatsCompat::getRatHasTogaInRatlantisTooltip);
        registry.registerConditionTooltip(RatKilledAndHasUpgradeCondition.class, RatsCompat::getRatKilledAndHasUpgradeTooltip);
        registry.registerConditionTooltip(RatlantisLoadedLootCondition.class, RatsCompat::getRatlantisLoadedTooltip);

        registry.registerPageResolver(RatKilledAndHasUpgradeCondition.class, RatsCompat::testRatKilledAndHasUpgrade);

        ResourceLocation plagueDoctorId = new ResourceLocation(RatsLang.MOD_ID, "plague_doctor");

        registry.registerTrades(plagueDoctorId, BuiltInRegistries.ENTITY_TYPE.get(plagueDoctorId), RatsCompat::getPlagueDoctorTrades, RatsCompat::getPlagueDoctorLevelInfo);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, GenericAddItemLootModifier.class, GenericAddItemLootModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getRatHasPlagueTooltip(IServerUtils ignoredUtils, RatHasPlagueCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, RatsLang.Conditions.RAT_HAS_PLAGUE);
    }

    @NotNull
    private static TooltipBuilder getRatHasTogaInRatlantisTooltip(IServerUtils ignoredUtils, RatHasTogaInRatlantisCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, RatsLang.Conditions.HAS_TOGA_AND_IN_RATLANTIS);
    }

    @NotNull
    private static TooltipBuilder getRatKilledAndHasUpgradeTooltip(IServerUtils utils, RatKilledAndHasUpgradeCondition cond) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, cond.upgrade()).build(Lang.Value.ITEM)), RatsLang.Conditions.KILLER_HAS_UPGRADE);
    }

    @NotNull
    private static TooltipBuilder getRatlantisLoadedTooltip(IServerUtils ignoredUtils, RatlantisLoadedLootCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, RatsLang.Conditions.RATLANTIS_LOADED);
    }

    @NotNull
    private static Verdict testRatKilledAndHasUpgrade(IServerUtils utils, RatKilledAndHasUpgradeCondition ignoredCond, LootPage page) {
        if (utils.getParamState(page, LootContextParams.KILLER_ENTITY) == ParamState.DISALLOWED) {
            return Verdict.NO;
        }

        return Verdict.yes(false);
    }

    @NotNull
    private static Int2ObjectMap<VillagerTrades.ItemListing[]> getPlagueDoctorTrades() {
        Int2ObjectMap<VillagerTrades.ItemListing[]> trades = new Int2ObjectOpenHashMap<>(PlagueDoctorTrades.PLAGUE_DOCTOR_TRADES);

        if (!RatsMod.RATLANTIS_DATAPACK_ENABLED) {
            trades.put(DESPAWNING_LEVEL, new VillagerTrades.ItemListing[]{
                    PlagueDoctorTrades.COMBINER_TRADE,
                    PlagueDoctorTrades.SEPARATOR_TRADE,
                    PlagueDoctorTrades.UPGRADE_COMBINED_TRADE,
            });
        }

        return trades;
    }

    @NotNull
    private static TradeLevelInfo getPlagueDoctorLevelInfo(int level) {
        return switch (level) {
            case COMMON_LEVEL -> new TradeLevelInfo(NumberExpr.constant(COMMON_TRADE_COUNT));
            case RARE_LEVEL -> new TradeLevelInfo(NumberExpr.constant(RARE_TRADE_COUNT));
            default -> new TradeLevelInfo(NumberExpr.constant(DESPAWNING_TRADE_COUNT));
        };
    }
}
