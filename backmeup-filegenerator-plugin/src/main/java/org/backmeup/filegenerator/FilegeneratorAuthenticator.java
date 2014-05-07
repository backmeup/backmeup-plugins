package org.backmeup.filegenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.filegenerator.constants.Constants;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.plugin.spi.InputBased;

public class FilegeneratorAuthenticator implements InputBased {



	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.InputBased;
	}

	@Override
	public String postAuthorize(Properties inputProperties) {
		String retValue = "";
		
		if(inputProperties.getProperty(Constants.PROP_TEXT).equals("true")) {
			retValue += Constants.PROP_TEXT_PARAGRAPHS + " = " + inputProperties.getProperty(Constants.PROP_TEXT_PARAGRAPHS);
		}
		
		if(inputProperties.getProperty(Constants.PROP_IMAGE).equals("true")) {
			retValue += ", " + Constants.PROP_IMAGE_SIZE + " = " + inputProperties.getProperty(Constants.PROP_IMAGE_SIZE);
		}
		
		if(inputProperties.getProperty(Constants.PROP_PDF).equals("true")) {
			retValue += ", " + Constants.PROP_PDF_PARAGRAPHS + " = " + inputProperties.getProperty(Constants.PROP_PDF_PARAGRAPHS);
		}
		
		if(inputProperties.getProperty(Constants.PROP_BINARY).equals("true")) {
			retValue += ", " + Constants.PROP_BINARY_SIZE + " = " + inputProperties.getProperty(Constants.PROP_BINARY_SIZE);
		}
		
		retValue += ", " + Constants.PROP_GENERATOR_FILES + " = " + inputProperties.getProperty(Constants.PROP_GENERATOR_FILES);
		
		return retValue;
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> inputs = new ArrayList<RequiredInputField>();
		
		inputs.add(new RequiredInputField (Constants.PROP_TEXT, Constants.PROP_TEXT, Constants.PROP_TEXT_DESC, true, 0, Type.Bool));
		inputs.add(new RequiredInputField (Constants.PROP_TEXT_PARAGRAPHS, Constants.PROP_TEXT_PARAGRAPHS, Constants.PROP_TEXT_PARAGRAPHS_DESC, false, 1, Type.Number));
		
		inputs.add(new RequiredInputField (Constants.PROP_IMAGE, Constants.PROP_IMAGE, Constants.PROP_IMAGE_DESC, true, 2, Type.Bool));
		inputs.add(new RequiredInputField (Constants.PROP_IMAGE_SIZE, Constants.PROP_IMAGE_SIZE, Constants.PROP_IMAGE_SIZE_DESC, false, 3, Type.Number));
		
		inputs.add(new RequiredInputField (Constants.PROP_PDF, Constants.PROP_PDF, Constants.PROP_PDF_DESC, true, 4, Type.Bool));
		inputs.add(new RequiredInputField (Constants.PROP_PDF_PARAGRAPHS, Constants.PROP_PDF_PARAGRAPHS, Constants.PROP_PDF_PARAGRAPHS_DESC, false, 5, Type.Number));
		
		inputs.add(new RequiredInputField (Constants.PROP_BINARY, Constants.PROP_BINARY, Constants.PROP_BINARY_DESC, true, 6, Type.Bool));
		inputs.add(new RequiredInputField (Constants.PROP_BINARY_SIZE, Constants.PROP_BINARY_SIZE, Constants.PROP_BINARY_SIZE_DESC, false, 7, Type.Number));
		
		inputs.add(new RequiredInputField(Constants.PROP_GENERATOR_FILES, Constants.PROP_GENERATOR_FILES, Constants.PROP_GENERATOR_FILES_DESC, true, 8, Type.Number));
		
	    return inputs;
	}

	@Override
	public Map<String, Type> getTypeMapping() {
		Map<String, Type> typeMapping = new HashMap<String, Type>();
		typeMapping.put(Constants.PROP_TEXT, Type.Bool);
		typeMapping.put(Constants.PROP_TEXT_PARAGRAPHS, Type.Number);
		typeMapping.put(Constants.PROP_IMAGE, Type.Bool);
	    typeMapping.put(Constants.PROP_IMAGE_SIZE, Type.Number);
	    typeMapping.put(Constants.PROP_PDF, Type.Bool);
	    typeMapping.put(Constants.PROP_PDF_PARAGRAPHS, Type.Number);
	    typeMapping.put(Constants.PROP_BINARY, Type.Bool);
	    typeMapping.put(Constants.PROP_BINARY_SIZE, Type.Number);
	    typeMapping.put(Constants.PROP_GENERATOR_FILES, Type.Number); 
	    return typeMapping;
	}

	@Override
	public boolean isValid(Properties inputs) {
		return true;
	}



}
