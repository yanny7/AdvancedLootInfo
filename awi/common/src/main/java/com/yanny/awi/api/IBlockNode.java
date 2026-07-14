package com.yanny.awi.api;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public interface IBlockNode {
    @NotNull
    Block getBlock();

    float getChance();
}
