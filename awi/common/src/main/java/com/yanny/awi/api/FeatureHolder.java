package com.yanny.awi.api;

import com.yanny.aci.tooltip.TooltipNode;
import net.minecraft.world.item.Item;

import java.util.List;

public record FeatureHolder(
        List<Item> items,
        TooltipNode conditions
){}

