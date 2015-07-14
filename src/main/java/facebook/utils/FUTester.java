package facebook.utils;

import java.io.File;

public class FUTester
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		File f1 = new File("/home/richard/Pictures/hh");
		File f2 = new File("/home/richard/Pictures/dummy/0.jpg");
		System.out.println(FileUtils.getWayTo(f1, f2));
		System.out.println(FileUtils.resolveRelativePath(f1, FileUtils.getWayTo(f1, f2)));
		System.out.println(FileUtils.resolveRelativePath(f1, FileUtils.getWayTo(f1, f2)));

	}

}
