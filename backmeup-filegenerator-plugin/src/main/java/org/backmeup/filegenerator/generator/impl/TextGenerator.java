package org.backmeup.filegenerator.generator.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.backmeup.filegenerator.constants.Constants;
import org.backmeup.filegenerator.generator.Generator;

import de.svenjacobs.loremipsum.LoremIpsum;

public class TextGenerator implements Generator {
	private final int amount;
	private final LoremIpsum loremIpsum;
	
	public TextGenerator() {
		this(Integer.parseInt(Constants.PROP_TEXT_PARAGRAPHS_DEFAULT));
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
