package com.yanny.alicompat.test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class CompatFailureAppender extends AbstractAppender {
    private static final String COMPAT_PACKAGE = "com.yanny.alicompat.compat.";

    private final List<String> failures = new ArrayList<>();

    private CompatFailureAppender() {
        super("CompatFailures", null, null, true, Property.EMPTY_ARRAY);
    }

    @NotNull
    public static CompatFailureAppender attach() {
        CompatFailureAppender appender = new CompatFailureAppender();

        appender.start();
        ((Logger) LogManager.getRootLogger()).addAppender(appender);
        return appender;
    }

    @Override
    public void append(LogEvent event) {
        if (event.getLevel().isMoreSpecificThan(Level.WARN) && event.getLoggerName().startsWith("com.yanny.alicompat")) {
            Optional<String> slug = findSlug(event.getThrown());

            if (slug.isEmpty() || hasTests(slug.get())) {
                failures.add(event.getMessage().getFormattedMessage() + rootCause(event.getThrown()));
            }
        }
    }

    public void detachAndCheck() {
        ((Logger) LogManager.getRootLogger()).removeAppender(this);
        stop();

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Mod compat registration failed:\n" + String.join("\n", failures));
        }
    }

    @NotNull
    private static Optional<String> findSlug(@Nullable Throwable thrown) {
        Stream<StackTraceElement> frames = StackWalker.getInstance().walk((s) -> s.map(StackWalker.StackFrame::toStackTraceElement).toList()).stream();

        for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
            frames = Stream.concat(Arrays.stream(cause.getStackTrace()), frames);
        }

        return frames.map(StackTraceElement::getClassName)
                .filter((name) -> name.startsWith(COMPAT_PACKAGE))
                .map((name) -> name.substring(COMPAT_PACKAGE.length(), name.indexOf('.', COMPAT_PACKAGE.length())))
                .findFirst();
    }

    private static boolean hasTests(String slug) {
        try {
            Path testClasses = Path.of(CompatFailureAppender.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            return Files.isDirectory(testClasses.resolve(COMPAT_PACKAGE.replace('.', '/')).resolve(slug));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to locate test classes", e);
        }
    }

    @NotNull
    private static String rootCause(@Nullable Throwable thrown) {
        if (thrown == null) {
            return "";
        }

        Throwable cause = thrown;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return " (" + cause + ")";
    }
}
