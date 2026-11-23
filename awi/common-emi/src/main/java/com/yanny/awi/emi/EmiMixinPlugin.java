package com.yanny.awi.emi;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class EmiMixinPlugin implements IMixinConfigPlugin {
    private static final String EMI_CLASS = "dev/emi/emi/api/EmiPlugin";

    private boolean emiPresent;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(EMI_CLASS);
            emiPresent = true;
        } catch (ClassNotFoundException | IOException e) {
            emiPresent = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return emiPresent;
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
