package facebook.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils
{
	public static String getWayTo(File from, File to)
	{
		String separator = File.separator;
		//Only for Windows
		if (separator.equals("\\"))
			separator = "\\\\";
		StringBuilder sb = new StringBuilder();
		int iterator = 0;
		if (from == null || to == null)
			return "null";
		if (!from.isDirectory())
			from = from.getParentFile();
		String[] fromParts = from.toString().split(separator);
		String[] toParts = to.toString().split(separator);
		boolean uncorrect = false;
		for (String dirs : fromParts)
		{
			if (uncorrect || iterator >= toParts.length || !dirs.equals(toParts[iterator]))
			{
				sb.append("../");
				uncorrect = true;
			}
			iterator++;
		}
		iterator = 0;
		uncorrect = false;
		for (String dirs : toParts)
		{
			if (uncorrect || iterator >= fromParts.length || !dirs.equals(fromParts[iterator]))
			{
				sb.append(toParts[iterator] + "/");
				uncorrect = true;
			}
			iterator++;
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		if ((sb.length() >= 2 && !sb.substring(0, 2).equals("./")) || (sb.length() >= 3 && !sb.subSequence(0, 3).equals("../")))
			sb.insert(0, "./");
		return sb.toString();
	}

	public static File resolveRelativePath(File start, String relativePath)
	{
		if (start == null || relativePath == null)
			return null;
		if (!start.isDirectory())
			start = start.getParentFile();
		StringBuilder sb = new StringBuilder();
		sb.append(start);
		for (String s : relativePath.split("/"))
			if (s.equals(".."))
				start = start.getParentFile();
			else if (!s.equals("."))
				sb.append("/" + s);
		return new File(sb.toString());
	}

	public static void exctractFromJar(String resource, File target, Class<?> root)
	{
		try (InputStream is = root.getResourceAsStream(resource); FileOutputStream fos = new FileOutputStream(target))
		{
			int position = 0;
			byte[] data = new byte[4096];
			while ((position = is.read(data)) > 0)
				fos.write(data, 0, position);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
