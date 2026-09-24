package com.yanny.alicompat.compat.placebo;

import com.yanny.aci.language.CoreLang;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.alicompat.IModCompat;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import org.jetbrains.annotations.NotNull;

public class PlaceboCompat implements IModCompat {
    private static final String MOD_ID = "placebo";

    @NotNull
    @Override
    public String targetModId() {
        return MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerValueTooltip(DynamicHolder.class, PlaceboCompat::getDynamicHolderTooltip);
        registry.registerValueTooltip(DynamicHolderSet.class, PlaceboCompat::getDynamicHolderSetTooltip);
    }

    @NotNull
    private static TooltipBuilder getDynamicHolderTooltip(IServerUtils utils, DynamicHolder<?> holder) {
        return utils.getValueTooltip(utils, holder.getId());
    }

    @NotNull
    private static TooltipBuilder getDynamicHolderSetTooltip(IServerUtils utils, DynamicHolderSet<?> holderSet) {
        return holderSet.unwrap().map((tag) -> TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, tag.id()).key(CoreLang.Utils.TAG))),
                (holders) -> utils.getValueTooltip(utils, holders));
    }
}
