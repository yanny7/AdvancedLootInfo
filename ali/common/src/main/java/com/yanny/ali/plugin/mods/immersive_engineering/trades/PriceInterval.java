package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$PriceInterval")
public class PriceInterval extends BaseAccessor<Object> {
    @FieldAccessor
    public int min;
    @FieldAccessor
    public int max;

    public PriceInterval(Object parent) {
        super(parent);
    }
}
