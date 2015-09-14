package org.backmeup.sftp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.plugin.api.InputBasedAuthorizable;

public class SftpAuthenticator implements InputBasedAuthorizable {
	
	private static final String PROP_HOST = "Host";
	private static final String PROP_HOST_DEFAULT = "localhost";
	private static final String PROP_HOST_DESC = "The hostname of your ftp server";
	
	private static final String PROP_PORT = "Port";
	private static final String PROP_PORT_DEFAULT = "22";
	private static final String PROP_PORT_DESC = "The port on which the plugin should connect to the FTP server";
	
	private static final String PROP_FOLDER = "Folder";
	private static final String PROP_FOLDER_DEFAULT = "/";
	private static final String PROP_FOLDER_DESC = "The remote folder where mobile backup data resides";
	
	private static final String PROP_USERNAME = "Username";
	private static final String PROP_USERNAME_DEFAULT = "me@localhost";
	private static final String PROP_USERNAME_DESC = "The username of your FTP account";
	
	private static final String PROP_PASSWORD = "Password";
	private static final String PROP_PASSWORD_DEFAULT = "";
	private static final String PROP_PASSWORD_DESC = "The password of your FTP account";
	
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.INPUTBASED;
	}

	@Override
	public String authorize(Map<String, String> inputProperties) {
		return inputProperties.get(PROP_USERNAME);
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> inputs = new ArrayList<>();
		inputs.add(new RequiredInputField(PROP_HOST, PROP_HOST, PROP_HOST_DESC, true, 0, Type.String, PROP_HOST_DEFAULT));
		inputs.add(new RequiredInputField(PROP_PORT, PROP_PORT, PROP_PORT_DESC, true, 1, Type.Number, PROP_PORT_DEFAULT));
		inputs.add(new RequiredInputField(PROP_FOLDER, PROP_FOLDER, PROP_FOLDER_DESC, true, 2, Type.String, PROP_FOLDER_DEFAULT));
		inputs.add(new RequiredInputField(PROP_USERNAME, PROP_USERNAME, PROP_USERNAME_DESC, true, 3, Type.String, PROP_USERNAME_DEFAULT));
		inputs.add(new RequiredInputField(PROP_PASSWORD, PROP_PASSWORD, PROP_PASSWORD_DESC, true, 4, Type.Password, PROP_PASSWORD_DEFAULT));
		return inputs;
	}

	@Override
	public boolean isValid(Map<String, String> inputs) {
		return validateInputFields(inputs).getValidationEntries().isEmpty();
	}
  
	@Override
	public ValidationNotes validateInputFields(Map<String, String> properties) {
		ValidationNotes notes = new ValidationNotes();
		addEntryIfKeyMissing(properties, PROP_HOST, notes);
		addEntryIfKeyMissing(properties, PROP_PORT, notes);
		addEntryIfKeyMissing(properties, PROP_FOLDER, notes);
		addEntryIfKeyMissing(properties, PROP_USERNAME, notes);
		addEntryIfKeyMissing(properties, PROP_PASSWORD, notes);
		return notes;
	}
	
	private void addEntryIfKeyMissing(Map<String, String> properties, String key, ValidationNotes notes) {
		if (!properties.containsKey(key)) {
			notes.addValidationEntry(ValidationExceptionType.ConfigException, SftpDescriptor.SFTP_ID, "Required input field missing: " + key);
		}
	}
}
