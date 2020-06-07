package be.sysa.pem.reload;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

class KeyStoreMonitoredPaths {
    private static final String SOURCE_PREFIX = "source.";

    private static final Set<Path> set = new HashSet<>();

    static Set<Path> getPaths() {
        return Collections.unmodifiableSet(set);
    }

    @SneakyThrows
    @Synchronized
    static void addPaths(String keyStoreLocation) {
        set.addAll(pathsFromConfiguration(keyStoreLocation));
    }

    private static Set<Path> pathsFromConfiguration(String keyStoreLocation) throws IOException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        @Cleanup InputStream inputStream = resourceLoader.getResource(keyStoreLocation).getInputStream();
        HashSet<Path> set = new HashSet<>();
        // todo check if file found

        Properties properties = new Properties();
        properties.load(inputStream);
        for (final String key : properties.stringPropertyNames()) {
            if (key.startsWith(SOURCE_PREFIX)) {
                String property = properties.getProperty(key);
                getPath(property).ifPresent(set::add);
            }
        }
        return set;
    }

    private static Optional<Path> getPath(final String uri) throws IOException {
        if (uri.startsWith("classpath:")) {
            return Optional.empty();
        } else {
            File file = new File(uri.startsWith("file://") ? uri.substring(7) : uri);
            return file.exists() ? Optional.of(file.getParentFile().toPath()) : Optional.empty();
        }
    }

}
