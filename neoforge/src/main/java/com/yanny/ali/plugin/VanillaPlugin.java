package com.yanny.ali.plugin;

import com.yanny.ali.api.AliEntrypoint;
import com.yanny.ali.api.IClientRegistry;
import com.yanny.ali.api.IClientUtils;
import com.yanny.ali.mixin.MixinCanItemPerformAbility;
import com.yanny.ali.mixin.MixinLootTableIdCondition;
import com.yanny.ali.plugin.client.GenericTooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@AliEntrypoint
public class VanillaPlugin extends Plugin {
    @Override
    public void registerClient(IClientRegistry registry) {
        super.registerClient(registry);
        registry.registerConditionTooltip(CanItemPerformAbility.LOOT_CONDITION_TYPE, VanillaPlugin::getTooltip);
        registry.registerConditionTooltip(LootTableIdCondition.LOOT_TABLE_ID, VanillaPlugin::getLootTableIdTooltip);
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getTooltip(IClientUtils utils, int pad, LootItemCondition condition) {
        MixinCanItemPerformAbility cond = (MixinCanItemPerformAbility) condition;
        return GenericTooltipUtils.getStringTooltip(utils, pad, "ali.type.condition.can_item_perform_ability", cond.getAbility().name());
    }

    @Unmodifiable
    @NotNull
    public static List<Component> getLootTableIdTooltip(IClientUtils utils, int pad, LootTableIdCondition condition) {
        MixinLootTableIdCondition cond = (MixinLootTableIdCondition) condition;
        return GenericTooltipUtils.getResourceLocationTooltip(utils, pad, "ali.type.condition.loot_table_id", cond.getTargetLootTableId());
    }
}
