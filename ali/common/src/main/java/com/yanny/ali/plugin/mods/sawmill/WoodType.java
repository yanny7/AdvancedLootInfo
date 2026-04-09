package com.yanny.ali.plugin.mods.sawmill;

import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@ClassAccessor("net.mehvahdjukaar.moonlight.api.set.wood.WoodType")
public class WoodType extends BaseAccessor<Object> {
    @FieldAccessor
    public Block planks;
    @FieldAccessor
    public Block log;

    public WoodType(Object parent) {
        super(parent);
    }

    Item getItemOfThis(String key) {
        try {
            return (Item) parent.getClass().getMethod("getItemOfThis", String.class).invoke(parent, key);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
