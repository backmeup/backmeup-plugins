package org.backmeup.filegenerator.generator.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.backmeup.filegenerator.generator.Generator;

import de.svenjacobs.loremipsum.LoremIpsum;

public class TextGenerator implements Generator {
	private int amount;
	private LoremIpsum loremIpsum;
	
	public TextGenerator() {
		this(1);
	}
	
	public TextGenerator(int amountInParagraphs) {
		this.amount = amountInParagraphs;
		this.loremIpsum = new LoremIpsum();
	}
	
	@Override
	public InputStream generate() {
		String text = loremIpsum.getParagraphs(amount);
		return stringToStream(text);
	}
	
	public String getParagraphs(int amount) {
		return loremIpsum.getParagraphs(amount);
	}
	
	public String getWords(int amount) {
		return loremIpsum.getWords(amount);
	}
	
	private InputStream stringToStream(String input) {
		try {
			InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
			return is;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
