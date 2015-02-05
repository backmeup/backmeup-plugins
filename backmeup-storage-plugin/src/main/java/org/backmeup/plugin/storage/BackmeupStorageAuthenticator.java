package org.backmeup.plugin.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.plugin.spi.InputBasedAuthorizable;
import org.backmeup.plugin.storage.constants.Constants;
import org.backmeup.plugin.storage.utils.PropertiesUtil;
import org.backmeup.storage.api.StorageClient;
import org.backmeup.storage.client.BackmeupStorageClient;

public class BackmeupStorageAuthenticator implements InputBasedAuthorizable {

    @Override
    public AuthorizationType getAuthType() {
        return AuthorizationType.InputBased;
    }

    @Override
    public String authorize(Properties inputProperties) {
        try {
            String username = inputProperties.getProperty(Constants.PROP_USERNAME);
            String password = inputProperties.getProperty(Constants.PROP_PASSWORD);
            
            String storageUrl = PropertiesUtil.getInstance().getProperty(Constants.PROP_STORAGE_URL);
            StorageClient storageClient = new BackmeupStorageClient(storageUrl);
            String accessToken = storageClient.authenticate(username, password);
            
            inputProperties.put(Constants.ACCESS_TOKEN, accessToken);
            inputProperties.put(Constants.PROP_STORAGE_URL, storageUrl);
            
            return "User";
        } catch (Exception e) {
            throw new PluginException(BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID, 
                    "An error occurred during authorization: " + e.getMessage());
        }
    }

    @Override
    public List<RequiredInputField> getRequiredInputFields() {
        List<RequiredInputField> inputs = new ArrayList<>();

        inputs.add(
            new RequiredInputField(
                Constants.PROP_USERNAME, 
                Constants.PROP_USERNAME_LABEL, 
                Constants.PROP_USERNAME_DESC, true, 1, Type.String, "")
        );

        inputs.add(
            new RequiredInputField(
                Constants.PROP_PASSWORD, 
                Constants.PROP_PASSWORD_LABEL, 
                Constants.PROP_PASSWROD_DESC, true, 1, Type.Password, "")
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

        addEntryIfKeyMissing(properties, Constants.PROP_USERNAME, notes);
        addEntryIfKeyMissing(properties, Constants.PROP_PASSWORD, notes);
        
        addEntryIfValueEmpty(properties, Constants.PROP_USERNAME, notes);
        addEntryIfValueEmpty(properties, Constants.PROP_PASSWORD, notes);

        return notes;
    }

    private void addEntryIfKeyMissing(Properties properties, String key, ValidationNotes notes) {
        if (!properties.containsKey(key)) {
            notes.addValidationEntry(ValidationExceptionType.ConfigException, BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID,
                    "Required input field missing: " + key);
        }
    }
    
    private void addEntryIfValueEmpty(Properties properties, String key, ValidationNotes notes) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            notes.addValidationEntry(ValidationExceptionType.ConfigException, BackmeupStorageDescriptor.BACKMEUP_STORAGE_ID,
                    "Value must not be null or empty: " + key);
        }
    }
}
