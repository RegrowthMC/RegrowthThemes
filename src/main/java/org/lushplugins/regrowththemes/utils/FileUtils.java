package org.lushplugins.regrowththemes.utils;

import java.io.File;
import java.nio.file.Path;

public class FileUtils {

    public static File getSafeFile(File file, String path) {
        Path dataFolderPath = file.toPath();
        Path filePath = dataFolderPath.resolve(path).normalize();

        if (!filePath.startsWith(dataFolderPath)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        return filePath.toFile();
    }
}
