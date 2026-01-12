package com.yanny.ali.plugin.mods.immersive_engineering.trades;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.plugin.mods.BaseAccessor;
import com.yanny.ali.plugin.mods.ClassAccessor;
import com.yanny.ali.plugin.mods.FieldAccessor;
import com.yanny.ali.plugin.mods.PluginUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$LazyItemStack")
public class LazyItemStack extends BaseAccessor<Function<Level, ItemStack>> {
    @FieldAccessor
    private Function<Level, ItemStack> function;

    public LazyItemStack(Function<Level, ItemStack> parent) {
        super(parent);
    }

    public ItemStack apply(@Nullable Level level) {
        return parent.apply(level);
    }

    public Either<ItemStack, TagKey<? extends ItemLike>> getItem() {
        List<ItemStack> itemStacks = PluginUtils.getCapturedInstances(function, ItemStack.class);

        if (itemStacks.size() == 1) {
            return Either.left(itemStacks.get(0));
        }

        //noinspection unchecked
        List<TagKey<ItemLike>> tagKeys = (List<TagKey<ItemLike>>)(Object) PluginUtils.getCapturedInstances(function, TagKey.class);

        if (tagKeys.size() == 1) {
            return Either.right(tagKeys.get(0));
        }

        return Either.left(ItemStack.EMPTY);
    }
}
