package org.backmeup.filegenerator.generator.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.backmeup.filegenerator.FilegeneratorDescriptor;
import org.backmeup.filegenerator.constants.Constants;
import org.backmeup.filegenerator.generator.Generator;
import org.backmeup.model.exceptions.PluginException;

public class ImageGenerator implements Generator {
    private final int sizeX;
    private final int sizeY;
    private final Random random;

    public ImageGenerator() {
        int defaultSize = Integer.parseInt(Constants.PROP_IMAGE_SIZE_DEFAULT);
        this.sizeX = defaultSize;
        this.sizeY = defaultSize;
        this.random = new Random();
    }

    public ImageGenerator(int sizeX, int sizeY, Random random) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.random = random;
    }

    @Override
    public InputStream generate() {
        BufferedImage image = generateImage();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            throw new PluginException(FilegeneratorDescriptor.FILEGENERATOR_ID, "Failed to generate image", e);
        }
    }

    private BufferedImage generateImage() {
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
}