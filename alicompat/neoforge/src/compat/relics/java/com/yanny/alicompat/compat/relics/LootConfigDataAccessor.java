package com.yanny.alicompat.compat.relics;

import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;

@ClassAccessor("it.hurts.sskirillss.relics.config.LootConfigData")
public class LootConfigDataAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    double relicGenChance;

    public LootConfigDataAccessor(Object parent) {
        super(parent);
    }
}
