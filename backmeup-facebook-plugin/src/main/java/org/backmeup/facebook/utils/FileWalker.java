package org.backmeup.facebook.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Iterates over a given directory and returns its items recursively
 *
 */
public class FileWalker {

    public List<File> walk(String path) {

        List<File> ret = new ArrayList<File>();
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return ret;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                List<File> retChild = walk(f.getAbsolutePath());
                ret.addAll(retChild);
            } else {
                ret.add(f);
            }
        }
        return ret;
    }
}