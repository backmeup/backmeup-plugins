package org.backmeup.plugin.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.plugin.spi.InputBasedAuthorizable;
import org.backmeup.plugin.storage.Constants.Constants;

public class BackmeupStorageAuthenticator implements InputBasedAuthorizable {
	
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.InputBased;
	}

	@Override
	public String authorize(Properties inputProperties) {
		return "User1";
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> inputs = new ArrayList<>();

		inputs.add(
				new RequiredInputField(
						Constants.PROP_CONNECTION_STRING,
						Constants.PROP_CONNECTION_STRING_LABEL,
						Constants.PROP_CONNECTION_STRING_DESC, true, 0, Type.String,
						Constants.PROP_CONNECTION_STRING_DEFAULT)
				);
		
		return inputs;
	}

	@Override
	public boolean isValid(Properties inputs) {
		return validateInputFields(inputs).getValidationEntries().isEmpty();
	}
  
	@Override
	public ValidationNotes validateInputFields(Properties properties) {
		ValidationNotes notes = new ValidationNotes();

		addEntryIfKeyMissing(properties, Constants.PROP_CONNECTION_STRING, notes);
		
		if (notes.getValidationEntries().isEmpty()) {
			// TODO: validate connection string
			// e.g. connect to backmeup-storage-service
		}
		return notes;
	}
	
	private void addEntryIfKeyMissing(Properties properties, String key, ValidationNotes notes) {
		if (!properties.containsKey(key)) {
			notes.addValidationEntry(ValidationExceptionType.ConfigException,
					"Required input field missing: " + key);
		}
	}
}
