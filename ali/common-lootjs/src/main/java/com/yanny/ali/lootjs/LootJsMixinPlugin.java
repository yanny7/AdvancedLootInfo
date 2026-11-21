package com.yanny.ali.lootjs;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class LootJsMixinPlugin implements IMixinConfigPlugin {
    private static final String LOOTJS_CLASS = "com/almostreliable/lootjs/LootModificationsAPI";

    private boolean lootJsPresent;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(LOOTJS_CLASS);
            lootJsPresent = true;
        } catch (ClassNotFoundException | IOException e) {
            lootJsPresent = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return lootJsPresent;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
