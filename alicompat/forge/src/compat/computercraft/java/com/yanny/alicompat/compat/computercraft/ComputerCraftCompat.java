package com.yanny.alicompat.compat.computercraft;

import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import dan200.computercraft.shared.data.BlockNamedEntityLootCondition;
import dan200.computercraft.shared.data.HasComputerIdLootCondition;
import dan200.computercraft.shared.data.PlayerCreativeLootCondition;
import org.jetbrains.annotations.NotNull;

public class ComputerCraftCompat implements IModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return ComputerCraftLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConditionTooltip(BlockNamedEntityLootCondition.class, ComputerCraftCompat::getBlockNamedEntityTooltip);
        registry.registerConditionTooltip(HasComputerIdLootCondition.class, ComputerCraftCompat::getHasComputerIdTooltip);
        registry.registerConditionTooltip(PlayerCreativeLootCondition.class, ComputerCraftCompat::getPlayerCreativeTooltip);
    }

    @NotNull
    private static TooltipBuilder getBlockNamedEntityTooltip(IServerUtils ignoredUtils, BlockNamedEntityLootCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ComputerCraftLang.Conditions.BLOCK_NAMED);
    }

    @NotNull
    private static TooltipBuilder getHasComputerIdTooltip(IServerUtils ignoredUtils, HasComputerIdLootCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ComputerCraftLang.Conditions.HAS_COMPUTER_ID);
    }

    @NotNull
    private static TooltipBuilder getPlayerCreativeTooltip(IServerUtils ignoredUtils, PlayerCreativeLootCondition ignoredCond) {
        return TooltipBuilder.array(TooltipBuilder::showEmpty, ComputerCraftLang.Conditions.PLAYER_CREATIVE);
    }
}
