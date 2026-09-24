package com.yanny.alicompat.compat.occultism;

import com.klikli_dev.occultism.loot.AddItemModifier;
import com.klikli_dev.occultism.registry.OccultismFoods;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerRegistry;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.language.Lang;
import com.yanny.ali.plugin.glm.IGlobalLootModifierPlugin;
import com.yanny.alicompat.IGlmModCompat;
import com.yanny.alicompat.accessor.GlmAccessorUtils;
import org.jetbrains.annotations.NotNull;

public class OccultismCompat implements IGlmModCompat {
    @NotNull
    @Override
    public String targetModId() {
        return OccultismLang.MOD_ID;
    }

    @Override
    public void registerServer(IServerRegistry registry) {
        registry.registerConsumeEffectTooltip(OccultismFoods.DamageItemConsumeEffect.class, OccultismCompat::getDamageItemTooltip);
    }

    @Override
    public void registerGlobalLootModifier(IGlobalLootModifierPlugin.IRegistry registry) {
        GlmAccessorUtils.registerGlobalLootModifier(registry, AddItemModifier.class, AddItemModifierAccessor.class);
    }

    @NotNull
    private static TooltipBuilder getDamageItemTooltip(IServerUtils utils, OccultismFoods.DamageItemConsumeEffect effect) {
        return TooltipBuilder.array((b) -> b.add(utils.getValueTooltip(utils, effect.amount()).build(Lang.Value.AMOUNT)), OccultismLang.ConsumeEffects.DAMAGE_ITEM);
    }
}
