package facebook.utils;

public class ConsoleDrawer
{
	public static void drawProgress(int length, int progress, boolean first)
	{
		if (!first)
			for (int i = 0; i < length + 2; i++)
				System.out.print("\b");
		StringBuilder out = new StringBuilder();
		out.append("[");
		for (int i = 0; i < length; i++)
		{
			if (i < progress)
				out.append("#");
			else
				out.append("-");
		}
		out.append("]");
		System.out.print(out.toString());
	}
}
