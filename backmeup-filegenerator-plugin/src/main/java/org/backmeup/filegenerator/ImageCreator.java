package org.backmeup.filegenerator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class ImageCreator {

	public static BufferedImage generate(int sizeX, int sizeY, int seed) {
		Random random = new Random(seed);

		int x = 0;
		int y = 0;
		int blockSize = 5;

		BufferedImage bi = new BufferedImage(blockSize * sizeX, blockSize * sizeY, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = (Graphics2D) bi.getGraphics();

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				x = i * blockSize;
				y = j * blockSize;

				int red = random.nextInt(255);
				int green = random.nextInt(255);
				int blue = random.nextInt(255);
				Color randomColor = new Color(red, green, blue);

				g.setColor(randomColor);
				g.fillRect(y, x, blockSize, blockSize);
			}
		}

		g.dispose();
		return bi;
	}

	public static void saveToFile(BufferedImage img, String filename)
			throws IOException {
		File file = new File(filename);
		ImageWriter writer = null;
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
		if (iter.hasNext()) {
			writer = (ImageWriter) iter.next();
		}

		ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		writer.setOutput(ios);
		ImageWriteParam param = new JPEGImageWriteParam(
				java.util.Locale.getDefault());

		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(1.0f);
		writer.write(null, new IIOImage(img, null, null), param);
	}
}