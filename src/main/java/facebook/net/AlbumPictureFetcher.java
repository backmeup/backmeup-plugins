package facebook.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.restfb.types.Album;
import com.restfb.types.Photo;

public class AlbumPictureFetcher
{
	public static void fetchInfo(Album album)
	{

		try (FileWriter fw = new FileWriter("/home/richard/" + System.currentTimeMillis()); BufferedReader br = new BufferedReader(new InputStreamReader(new URL(album.getLink()).openStream()));)
		{
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
			}
			System.out.println(sb.toString());
			fw.write(sb.toString());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void fetchPhoto(Photo photo,String parent)
	{
		System.out.println("PhotoLink: " + photo.getSource());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); FileOutputStream fw = new FileOutputStream(parent+"/" + photo.getId() + ".jpg"); BufferedInputStream br = new BufferedInputStream(new URL(photo.getSource()).openStream());)
		{
			byte[] puffer = new byte[1024];
			int i = 0;
			while ((i = br.read(puffer)) != -1)
			{
				baos.write(puffer, 0, i);
			}
			byte[] result = baos.toByteArray();
			fw.write(result);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void drawProgress(int length, int progress)
	{
		if(progress>0)
			for(int i = 0; i < length+2;i++)
				System.out.print("\b");
		StringBuilder out = new StringBuilder();
		out.append("[");
		for(int i = 0; i< length;i++)
		{
			if(i<progress)
				out.append("#");
			else
				out.append("-");
		}
		out.append("]");
		System.out.print(out.toString());
	}
}
