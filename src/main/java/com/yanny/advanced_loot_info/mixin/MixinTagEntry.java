package com.yanny.advanced_loot_info.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagEntry.class)
public interface MixinTagEntry {
    @Accessor
    TagKey<Item> getTag();

    @Accessor
    boolean getExpand();
}
