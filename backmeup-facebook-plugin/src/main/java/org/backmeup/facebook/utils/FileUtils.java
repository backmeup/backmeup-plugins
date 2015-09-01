package org.backmeup.facebook.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileUtils
{
	public static String getWayTo(File from, File to)
	{
	    return from.toPath().relativize(to.toPath()).toString();
	}

	public static File resolveRelativePath(File start, String relativePath)
	{
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

	public static void exctractFromJar(String resource, File target, Class<?> root) throws IOException
	{
		try (InputStream is = root.getResourceAsStream(resource); FileOutputStream fos = new FileOutputStream(target))
		{
			int len = 0;
			byte[] data = new byte[4096];
			while ((len = is.read(data)) > 0) {
			    fos.write(data, 0, len);
			}
		}
	}
}
