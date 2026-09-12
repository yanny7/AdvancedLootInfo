package com.yanny.alicompat.compat.immersiveengineering;

import com.mojang.datafixers.util.Either;
import com.yanny.ali.plugin.common.ReflectionUtils;
import com.yanny.alicompat.accessor.BaseAccessor;
import com.yanny.alicompat.accessor.ClassAccessor;
import com.yanny.alicompat.accessor.FieldAccessor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@ClassAccessor("blusunrize.immersiveengineering.common.world.Villages$LazyItemStack")
public class LazyItemStackAccessor extends BaseAccessor<Object> {
    @FieldAccessor
    private Function<Level, ItemStack> function;

    public LazyItemStackAccessor(Object parent) {
        super(parent);
    }

    @NotNull
    public Either<ItemStack, TagKey<? extends ItemLike>> getItem() {
        List<TagKey> tags = ReflectionUtils.getCapturedInstances(function, TagKey.class);

        if (!tags.isEmpty()) {
            //noinspection unchecked
            return Either.right((TagKey<? extends ItemLike>) tags.get(0));
        }

        List<ItemStack> stacks = ReflectionUtils.getCapturedInstances(function, ItemStack.class);

        return Either.left(stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy());
    }
}
