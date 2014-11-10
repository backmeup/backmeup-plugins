package org.backmeup.filegenerator.constants;

public class Constants {
	public static final String PROP_GENERATOR_FILES = "generator.files";
	public static final String PROP_GENERATOR_FILES_DEFAULT = "10";
	public static final String PROP_GENERATOR_FILES_DESC = "Set the maximum number of files that should be generated";
	
	public static final String PROP_TEXT = "text";
	public static final String PROP_TEXT_DEFAULT = "true";
	public static final String PROP_TEXT_DESC = "Generate plain text files";
	public static final String PROP_TEXT_PARAGRAPHS = "text.paragraphs";
	public static final String PROP_TEXT_PARAGRAPHS_DEFAULT = "1";
	public static final String PROP_TEXT_PARAGRAPHS_DESC = "Amout of paragraph in the text file";

	public static final String PROP_IMAGE = "image";
	public static final String PROP_IMAGE_DEFAULT = "true";
	public static final String PROP_IMAGE_DESC = "Generate image files";
	public static final String PROP_IMAGE_SIZE = "image.size";
	public static final String PROP_IMAGE_SIZE_DEFAULT = "512";
	public static final String PROP_IMAGE_SIZE_DESC = "Size (in pixel) of the generated image";

	public static final String PROP_PDF = "pdf";
	public static final String PROP_PDF_DEFAULT = "true";
	public static final String PROP_PDF_DESC = "Generate pdf documents";
	public static final String PROP_PDF_PARAGRAPHS = "pdf.paragraphs";
	public static final String PROP_PDF_PARAGRAPHS_DEFAULT = "1";
	public static final String PROP_PDF_PARAGRAPHS_DESC = "Amout of paragraphs in the pdf document";

	public static final String PROP_BINARY = "binary";
	public static final String PROP_BINARY_DEFAULT = "true";
	public static final String PROP_BINARY_DESC = "Generate binary files";
	public static final String PROP_BINARY_SIZE = "binary.size";
	public static final String PROP_BINARY_SIZE_DEFAULT = "1024";
	public static final String PROP_BINARY_SIZE_DESC = "Size (in bytes) of the allocated file";
	
}
