package com.yanny.ali.plugin;

import com.yanny.ali.api.IUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class EntryTooltipUtils {

    @NotNull
    public static List<Component> getLootItemTooltip(IUtils utils, int pad, LootPoolEntryContainer entry) {
        List<Component> components = new LinkedList<>();
        LootItem e = (LootItem) entry;



        return components;
    }
}
