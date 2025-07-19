package com.yanny.ali.manager;

import com.yanny.ali.api.IDataNode;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@FunctionalInterface
public interface IOnDoneListener {
    void onDone(Map<ResourceLocation, IDataNode> lootData);
}
