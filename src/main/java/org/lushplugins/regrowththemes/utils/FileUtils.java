package org.lushplugins.regrowththemes.utils;

import java.io.File;
import java.nio.file.Path;

public class FileUtils {

    public static File getSafeFile(Path dir, String path) {
        Path filePath = dir.resolve(path).normalize();
        if (!filePath.startsWith(dir)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        return filePath.toFile();
    }
}
