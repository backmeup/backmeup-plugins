package org.backmeup.facebook.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private FileUtils() {
    }

    public static String getWayTo(File from, File to) {
        if (from == null || to == null) {
            return "null";
        }
        Path p = from.toPath();
        if (!from.isDirectory()) {
            p = p.getParent();
        }
        return p.relativize(to.toPath()).toString();
    }

    public static File resolveRelativePath(File start, String relativePath) {
        if (start == null || relativePath == null) {
            return null;
        }

        Path p = start.toPath();
        if (!start.isDirectory()) {
            p = p.resolveSibling(relativePath);
        } else {
            p = p.resolve(relativePath);
        }
        return p.normalize().toFile();
    }

    public static void exctractFromJar(String resource, File target, Class<?> root) throws IOException {
        ClassLoader loader = root.getClassLoader();
        try (InputStream is = loader.getResourceAsStream(resource); FileOutputStream fos = new FileOutputStream(target)) {
            int len = 0;
            byte[] data = new byte[4096];
            while ((len = is.read(data)) > 0) {
                fos.write(data, 0, len);
            }
        }
    }

    public static List<File> files(File root) {
        List<File> list = new ArrayList<>();
        if (root == null || list == null || !root.isDirectory()) {
            return list;
        }

        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                list.addAll(files(file));
            } else {
                list.add(file);
            }
        }

        return list;
    }
}