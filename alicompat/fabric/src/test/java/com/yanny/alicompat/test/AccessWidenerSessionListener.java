package com.yanny.alicompat.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.fabricmc.classtweaker.api.ClassTweaker;
import net.fabricmc.classtweaker.api.ClassTweakerReader;
import net.fabricmc.classtweaker.classvisitor.AccessWidenerClassVisitor;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class AccessWidenerSessionListener implements LauncherSessionListener {
    @Override
    public void launcherSessionOpened(LauncherSession session) {
        ClassTweaker widener = ClassTweaker.newInstance();
        ClassTweakerReader reader = ClassTweakerReader.create(widener);

        try {
            Enumeration<URL> modJsons = AccessWidenerSessionListener.class.getClassLoader().getResources("fabric.mod.json");

            while (modJsons.hasMoreElements()) {
                URL modJson = modJsons.nextElement();
                JsonElement accessWidener;

                try (InputStream stream = modJson.openStream()) {
                    accessWidener = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject().get("accessWidener");
                }

                if (accessWidener != null) {
                    try (InputStream stream = new URL(modJson, accessWidener.getAsString()).openStream()) {
                        reader.read(stream.readAllBytes(), "named");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read access wideners", e);
        }

        Instrumentation instrumentation = ByteBuddyAgent.install();
        List<String> loaded = Arrays.stream(instrumentation.getAllLoadedClasses()).map(c -> c.getName().replace('.', '/')).filter(widener.getTargets()::contains).toList();

        if (!loaded.isEmpty()) {
            throw new IllegalStateException("Classes loaded before their access wideners were applied: " + loaded);
        }

        instrumentation.addTransformer(new Transformer(widener));
    }

    private record Transformer(ClassTweaker widener) implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            if (className == null || !widener.getTargets().contains(className)) {
                return null;
            }

            ClassWriter writer = new ClassWriter(0);

            new ClassReader(classfileBuffer).accept(new AccessWidenerClassVisitor(Opcodes.ASM9, writer, widener), 0);
            return writer.toByteArray();
        }
    }
}
