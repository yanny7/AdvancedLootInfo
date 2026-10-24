package com.yanny.ali.plugin.server.bytecode;

import net.minecraft.core.RegistryAccess;
import org.junit.jupiter.api.Test;

import static com.yanny.ali.plugin.server.bytecode.FakeCallContext.call;
import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class WorldModelTest {
    private static final String LEVEL = "net/minecraft/world/level/Level";

    private final WorldModel model = new WorldModel();
    private final FakeCallContext context = new FakeCallContext();

    @Test
    public void testRegistryAccessOfUnknownIsServerRegistries() {
        context.registries = RegistryAccess.EMPTY;

        assertEquals(new Value.Known(RegistryAccess.EMPTY), model.apply(context, registryAccess(Value.UNKNOWN)));
    }

    @Test
    public void testRegistryAccessWithoutRegistriesHasNoOpinion() {
        assertSame(MethodModel.NO_OPINION, model.apply(context, registryAccess(Value.UNKNOWN)));
    }

    @Test
    public void testRegistryAccessOfKnownHasNoOpinion() {
        context.registries = RegistryAccess.EMPTY;

        assertSame(MethodModel.NO_OPINION, model.apply(context, registryAccess(new Value.Known(new Object()))));
    }

    private static Call registryAccess(Value receiver) {
        return call(INVOKEVIRTUAL, LEVEL, RuntimeNames.REGISTRY_ACCESS.name(), RuntimeNames.REGISTRY_ACCESS.desc(), receiver);
    }
}
