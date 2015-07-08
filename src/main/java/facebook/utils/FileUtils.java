package facebook.utils;

import java.io.File;

import facebook.storage.SDO;

public class FileUtils
{
	public static String getWayTo(File from, File to)
	{
		StringBuilder sb = new StringBuilder();
		int iterator = 0;
		if (from == null || to == null)
			return "null";
		String[] fromParts = from.toString().split(SDO.SLASH.toString());
		String[] toParts = to.toString().split(SDO.SLASH.toString());
		for (String dirs : fromParts)
		{
			if (iterator >= toParts.length || !dirs.equals(toParts[iterator]))
				sb.append(".." + SDO.SLASH);
			iterator++;
		}
		iterator = 0;
		for (String dirs : toParts)
		{
			if (iterator >= fromParts.length || !dirs.equals(fromParts[iterator]))
				sb.append(toParts[iterator] + SDO.SLASH);
			iterator++;
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
