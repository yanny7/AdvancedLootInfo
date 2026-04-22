package com.yanny.awi.api;

import net.minecraft.world.item.Item;

import java.util.List;

public record FeatureHolder(
        List<Item> items,
        ITooltipNode conditions
){}

