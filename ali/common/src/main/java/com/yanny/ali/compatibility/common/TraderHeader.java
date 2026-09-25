package com.yanny.ali.compatibility.common;

import com.yanny.aci.api.Rect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TraderHeader(@Nullable Item spawnEgg, Rect spawnEggRect, Component title, @Nullable Component titleTooltip,
                           Rect titleRect, List<Rect> pois, Component acceptsLabel, Rect acceptsLabelRect, List<Rect> accepts, int height,
                           int minContentHeight) {
}
