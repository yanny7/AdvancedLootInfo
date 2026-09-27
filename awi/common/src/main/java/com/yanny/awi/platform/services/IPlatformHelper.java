package com.yanny.awi.platform.services;

import com.yanny.aci.platform.ICorePlatformHelper;
import com.yanny.awi.api.IPlugin;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Optional;

public interface IPlatformHelper extends ICorePlatformHelper<IPlugin> {
        Optional<Holder<Item>> getSpawnEggItem(EntityType<?> entityType);
}