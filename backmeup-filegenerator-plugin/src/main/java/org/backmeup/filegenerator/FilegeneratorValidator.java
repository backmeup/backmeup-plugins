package org.backmeup.filegenerator;

import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.Validationable;

public class FilegeneratorValidator implements Validationable {
	public ValidationNotes validate(Properties accessData) {
		ValidationNotes notes = new ValidationNotes();
		
			
		return notes;
	}
}
