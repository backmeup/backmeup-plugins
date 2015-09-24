package org.backmeup.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.plugin.api.InputBasedAuthorizable;

public class MailAuthenticator implements InputBasedAuthorizable {
    private static final String PROP_SSL = "SSL";
	private static final String PROP_SSL_DEFAULT = "true";
	private static final String PROP_SSL_DESC = "Use SSL (encrypted connection)";
	private static final String PROP_PORT = "Port";
	private static final String PROP_PORT_DEFAULT = "993";
	private static final String PROP_PORT_DESC = "The port on which the plugin should connect to the E-Mail server";
	private static final String PROP_HOST = "Host";
	private static final String PROP_HOST_DEFAULT = "imap.gmail.com";
	private static final String PROP_HOST_DESC = "The hostname of your E-Mail server";
	private static final String PROP_USERNAME = "Username";
	private static final String PROP_USERNAME_DEFAULT = "me@gmail.com";
	private static final String PROP_USERNAME_DESC = "The username of your E-Mail account";
	private static final String PROP_PASSWORD = "Password";
	private static final String PROP_PASSWORD_DEFAULT = "";
	private static final String PROP_PASSWORD_DESC = "The password of your E-Mail account";
	private static final String PROP_TYPE = "Type";
	private static final String PROP_TYPE_DEFAULT = "imap,pop3";
	private static final String PROP_TYPE_DESC = "IMAP or POP3";
	
	private static final String AUTHPROP_MAIL_HOST = "mail.host";
    private static final String AUTHPROP_MAIL_PASSWORD = "mail.password";
    private static final String AUTHPROP_MAIL_USER = "mail.user";
	
	
 
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.INPUTBASED;
	}

	@Override
	public String authorize(Map<String, String> authData) {
		Properties authProps = convertInputPropertiesToAuthProperties(authData);
		
		try {
			Session session = Session.getInstance(authProps);
			Store store = session.getStore();
			store.connect(authProps.getProperty(AUTHPROP_MAIL_HOST),
					authProps.getProperty(AUTHPROP_MAIL_USER),
					authProps.getProperty(AUTHPROP_MAIL_PASSWORD));
			store.close();
		} catch (MessagingException e) {
			throw new ValidationException(ValidationExceptionType.AuthException, "Cannot authorize mail provider", e);
		}

        for (final String name : authProps.stringPropertyNames()) {
            authData.put(name, authProps.getProperty(name));
        }
		
		return authData.get(AUTHPROP_MAIL_USER);
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> inputs = new ArrayList<>();

		inputs.add(new RequiredInputField(PROP_USERNAME, PROP_USERNAME, PROP_USERNAME_DESC, true, 0, Type.String, PROP_USERNAME_DEFAULT));
		inputs.add(new RequiredInputField(PROP_PASSWORD, PROP_PASSWORD, PROP_PASSWORD_DESC, true, 1, Type.Password, PROP_PASSWORD_DEFAULT));
		inputs.add(new RequiredInputField(PROP_TYPE, PROP_TYPE, PROP_TYPE_DESC, true, 2, Type.Enum, PROP_TYPE_DEFAULT));
		inputs.add(new RequiredInputField(PROP_HOST, PROP_HOST, PROP_HOST_DESC, true, 3, Type.String, PROP_HOST_DEFAULT));
		inputs.add(new RequiredInputField(PROP_PORT, PROP_PORT, PROP_PORT_DESC, true, 4, Type.Number, PROP_PORT_DEFAULT));
		inputs.add(new RequiredInputField(PROP_SSL, PROP_SSL, PROP_SSL_DESC, true, 5, Type.Bool, PROP_SSL_DEFAULT));

		return inputs;
	}

	@Override
	public boolean isValid(Map<String, String> inputs) {
		return validateInputFields(inputs).getValidationEntries().isEmpty();
	}
  
	@Override
	public ValidationNotes validateInputFields(Map<String, String> properties) {
		ValidationNotes notes = new ValidationNotes();

		addEntryIfKeyMissing(properties, PROP_USERNAME, notes);
		addEntryIfKeyMissing(properties, PROP_PASSWORD, notes);
		addEntryIfKeyMissing(properties, PROP_TYPE, notes);
		addEntryIfKeyMissing(properties, PROP_HOST, notes);
		addEntryIfKeyMissing(properties, PROP_PORT, notes);
		addEntryIfKeyMissing(properties, PROP_SSL, notes);
		
		return notes;
	}
	
	private void addEntryIfKeyMissing(Map<String, String> properties, String key, ValidationNotes notes) {
		if (!properties.containsKey(key)) {
			notes.addValidationEntry(ValidationExceptionType.ConfigException, MailDescriptor.MAIL_ID,
					"Required input field missing: " + key);
		}
	}
	
	private Properties convertInputPropertiesToAuthProperties(Map<String, String> inputs) {
		Properties authProperties = new Properties();
		String storeType = inputs.get(PROP_TYPE);
		String prefix = "mail." + storeType + ".";
		if (inputs.get(PROP_SSL) != null && "true".equalsIgnoreCase(inputs.get(PROP_SSL).toString())) {
			authProperties.put(prefix + "socketFactory.class","javax.net.ssl.SSLSocketFactory");
			authProperties.put(prefix + "socketFactory.fallback", "false");
		}
		authProperties.put(prefix + "port", inputs.get(PROP_PORT));
		authProperties.put(AUTHPROP_MAIL_USER, inputs.get(PROP_USERNAME));
		authProperties.put(AUTHPROP_MAIL_PASSWORD, inputs.get(PROP_PASSWORD));
		authProperties.put(AUTHPROP_MAIL_HOST, inputs.get(PROP_HOST));
		authProperties.put(prefix + "connectiontimeout", "5000");
		authProperties.put(prefix + "timeout", "5000");
		authProperties.put("mail.store.protocol", storeType);

		return authProperties;
	}
}
