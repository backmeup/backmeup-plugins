package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.backmeup.plugin.api.storage.DataObject;
import org.xml.sax.ContentHandler;

public class TikaAnalyzer {
	
	private final Tika tika = new Tika();
	
	public Map<String, String> analyze(DataObject dob) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(dob.getBytes());
		Map<String, String> meta = new HashMap<>();
		
		String mimeType = is.markSupported() ? tika.detect(is) : null;

		if (mimeType != null) 
			meta.put(IndexUtils.FIELD_CONTENT_TYPE, mimeType);
		
		try {
			Metadata metadata = new Metadata();
			ContentHandler handler = new BodyContentHandler();
			Parser parser = tika.getParser();
			parser.parse(is, handler, metadata, new ParseContext());
	
			for (String name : metadata.names()) {
				String value = metadata.get(name);
				meta.put(name, value);
			}
		} catch (Exception e) {
			// TODO log
		}
		return meta;
	}

}
