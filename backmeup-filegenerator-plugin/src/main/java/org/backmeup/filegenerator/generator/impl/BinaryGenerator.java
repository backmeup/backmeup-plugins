package org.backmeup.filegenerator.generator.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import org.backmeup.filegenerator.constants.Constants;
import org.backmeup.filegenerator.generator.Generator;

public class BinaryGenerator implements Generator {
    private final int size;
    private final Random random;

    public BinaryGenerator() {
        this.size = Integer.parseInt(Constants.PROP_BINARY_SIZE_DEFAULT);
        this.random = new Random();
    }

    public BinaryGenerator(int size, Random random) {
        this.size = size;
        this.random = random;
    }

    @Override
    public InputStream generate() {
        byte[] data = new byte[size];
        random.nextBytes(data);
        return new ByteArrayInputStream(data);
    }
}
