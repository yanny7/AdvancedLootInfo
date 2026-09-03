package com.yanny.ali.lootjs.test;

import com.almostreliable.lootjs.loot.condition.LootItemConditionWrapper;
import com.yanny.aci.tooltip.TooltipBuilder;
import com.yanny.ali.api.IServerUtils;
import com.yanny.ali.lootjs.mixin.MixinLootItemConditionWrapper;
import com.yanny.ali.lootjs.server.LootJsConditionTooltipUtils;
import com.yanny.ali.manager.PluginManager;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.mockito.Mockito;

import java.util.function.BiFunction;

import static org.mockito.Mockito.withSettings;

public class LootJsTestUtils {
    public static <T> T mock(Class<T> type, Class<?>... accessors) {
        return Mockito.mock(type, withSettings().extraInterfaces(accessors));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static LootItemConditionWrapper wrap(LootItemCondition condition) {
        LootItemConditionWrapper wrapper = mock(LootItemConditionWrapper.class, MixinLootItemConditionWrapper.class);

        Mockito.when(((MixinLootItemConditionWrapper) wrapper).getCondition()).thenReturn(condition);
        PluginManager.getInstance().serverRegistry.registerConditionTooltip((Class) wrapper.getClass(),
                (BiFunction) (BiFunction<IServerUtils, LootItemConditionWrapper, TooltipBuilder>) LootJsConditionTooltipUtils::lootItemConditionWrapperTooltip);
        return wrapper;
    }
}
