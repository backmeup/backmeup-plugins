package org.backmeup.filegenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.backmeup.filegenerator.constants.Constants;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;

public class FilegeneratorValidator implements Validationable {

	@Override
	public boolean hasRequiredProperties() {
		return true;
	}

	@Override
	public List<RequiredInputField> getRequiredProperties() {
		List<RequiredInputField> inputs = new ArrayList<>();
		
		inputs.add(new RequiredInputField(Constants.PROP_GENERATOR_FILES, Constants.PROP_GENERATOR_FILES, Constants.PROP_GENERATOR_FILES_DESC, false, 8, Type.Number, Constants.PROP_GENERATOR_FILES_DEFAULT));
		
		inputs.add(new RequiredInputField (Constants.PROP_TEXT, Constants.PROP_TEXT, Constants.PROP_TEXT_DESC, true, 0, Type.Bool, Constants.PROP_TEXT_DEFAULT));
		inputs.add(new RequiredInputField (Constants.PROP_TEXT_PARAGRAPHS, Constants.PROP_TEXT_PARAGRAPHS, Constants.PROP_TEXT_PARAGRAPHS_DESC, false, 1, Type.Number, Constants.PROP_TEXT_PARAGRAPHS_DEFAULT));
		
		inputs.add(new RequiredInputField (Constants.PROP_IMAGE, Constants.PROP_IMAGE, Constants.PROP_IMAGE_DESC, true, 2, Type.Bool, Constants.PROP_IMAGE_DEFAULT));
		inputs.add(new RequiredInputField (Constants.PROP_IMAGE_SIZE, Constants.PROP_IMAGE_SIZE, Constants.PROP_IMAGE_SIZE_DESC, false, 3, Type.Number, Constants.PROP_IMAGE_SIZE_DEFAULT));
		
		inputs.add(new RequiredInputField (Constants.PROP_PDF, Constants.PROP_PDF, Constants.PROP_PDF_DESC, true, 4, Type.Bool, Constants.PROP_PDF_DEFAULT));
		inputs.add(new RequiredInputField (Constants.PROP_PDF_PARAGRAPHS, Constants.PROP_PDF_PARAGRAPHS, Constants.PROP_PDF_PARAGRAPHS_DESC, false, 5, Type.Number, Constants.PROP_PDF_PARAGRAPHS_DEFAULT));
		
		inputs.add(new RequiredInputField (Constants.PROP_BINARY, Constants.PROP_BINARY, Constants.PROP_BINARY_DESC, true, 6, Type.Bool, Constants.PROP_BINARY_DEFAULT));
		inputs.add(new RequiredInputField (Constants.PROP_BINARY_SIZE, Constants.PROP_BINARY_SIZE, Constants.PROP_BINARY_SIZE_DESC, false, 7, Type.Number, Constants.PROP_BINARY_SIZE_DEFAULT));
		
	    return inputs;
	}

	@Override
	public ValidationNotes validateProperties(Map<String, String> properties) {
		ValidationNotes notes = new ValidationNotes();
		
		// Validate properties:
		// 1: Check if all required keys are present and 
		// 2: check if values match required types (and bounds?)
		if(!properties.containsKey(Constants.PROP_TEXT)){
			notes.addValidationEntry(ValidationExceptionType.ConfigException, 
					                 "Required input field missing: " + Constants.PROP_TEXT);
		}
		
		if(!properties.containsKey(Constants.PROP_IMAGE)){
			notes.addValidationEntry(ValidationExceptionType.ConfigException, 
					                 "Required input field missing: " + Constants.PROP_IMAGE);
		} 
		
		if(!properties.containsKey(Constants.PROP_PDF)){
			notes.addValidationEntry(ValidationExceptionType.ConfigException, 
					                 "Required input field missing: " + Constants.PROP_PDF);
		} 
		
		if(!properties.containsKey(Constants.PROP_BINARY)){
			notes.addValidationEntry(ValidationExceptionType.ConfigException, 
					                 "Required input field missing: " + Constants.PROP_BINARY);
		} 
				
		return notes;
	}

	@Override
	public boolean hasAvailableOptions() {
		return false;
	}

	@Override
	public List<String> getAvailableOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValidationNotes validateOptions(List<String> options) {
		// TODO Auto-generated method stub
		return null;
	}

}
