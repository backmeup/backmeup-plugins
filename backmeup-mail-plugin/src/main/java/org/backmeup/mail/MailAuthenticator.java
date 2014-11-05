package org.backmeup.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.plugin.spi.InputBasedAuthorizable;

public class MailAuthenticator implements InputBasedAuthorizable {
	private static final String PROP_SSL = "SSL";
	private static final String PROP_SSL_DESC = "Use SSL (encrypted connection)";
	private static final String PROP_PORT = "Port";
	private static final String PROP_PORT_DESC = "The port on which the plugin should connect to the E-Mail server";
	private static final String PROP_HOST = "Host";
	private static final String PROP_HOST_DESC = "The hostname of your E-Mail server";
	private static final String PROP_USERNAME = "Username";
	private static final String PROP_USERNAME_DESC = "The username of your E-Mail account";
	private static final String PROP_PASSWORD = "Password";
	private static final String PROP_PASSWORD_DESC = "The password of your E-Mail account";
	private static final String PROP_TYPE = "Type";
	private static final String PROP_TYPE_DESC = "IMAP or POP3";
 
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.InputBased;
	}

	@Override
	public String authorize(Properties inputProperties) {
		Properties newProps = convertInputPropertiesToAuthProperties(inputProperties);
		inputProperties.clear();
		inputProperties.putAll(newProps);
		return inputProperties.getProperty("mail.user");
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> inputs = new ArrayList<>();

		inputs.add(new RequiredInputField(PROP_USERNAME, PROP_USERNAME, PROP_USERNAME_DESC, true, 0, Type.String));
		inputs.add(new RequiredInputField(PROP_PASSWORD, PROP_PASSWORD, PROP_PASSWORD_DESC, true, 1, Type.Password));
		// TODO: Add choices imap/pop3... also provide default values
		inputs.add(new RequiredInputField(PROP_TYPE, PROP_TYPE, PROP_TYPE_DESC, true, 2, Type.String));
		inputs.add(new RequiredInputField(PROP_HOST, PROP_HOST, PROP_HOST_DESC, true, 3, Type.String));
		inputs.add(new RequiredInputField(PROP_PORT, PROP_PORT, PROP_PORT_DESC, true, 4, Type.Number));
		inputs.add(new RequiredInputField(PROP_SSL, PROP_SSL, PROP_SSL_DESC, true, 5, Type.Bool));

		return inputs;
	}

	private Properties convertInputPropertiesToAuthProperties(Properties inputs) {
		Properties authProperties = new Properties();
		String storeType = inputs.getProperty(PROP_TYPE);
		String prefix = "mail." + storeType + ".";
		if (inputs.get(PROP_SSL) != null && inputs.get(PROP_SSL).toString().equalsIgnoreCase("true")) {
			authProperties.put(prefix + "socketFactory.class","javax.net.ssl.SSLSocketFactory");
			authProperties.put(prefix + "socketFactory.fallback", "false");
		}
		authProperties.put(prefix + "port", inputs.getProperty(PROP_PORT));
		authProperties.put("mail.user", inputs.getProperty(PROP_USERNAME));
		authProperties.put("mail.password", inputs.getProperty(PROP_PASSWORD));
		authProperties.put("mail.host", inputs.getProperty(PROP_HOST));
		authProperties.put(prefix + "connectiontimeout", "5000");
		authProperties.put(prefix + "timeout", "5000");
		authProperties.put("mail.store.protocol", storeType);

		return authProperties;
	}

	@Override
	public boolean isValid(Properties inputs) {
		return validateInputFields(inputs).getValidationEntries().isEmpty();
	}
  
	@Override
	public ValidationNotes validateInputFields(Properties properties) {
		ValidationNotes notes = new ValidationNotes();

		addEntryIfKeyMissing(properties, PROP_USERNAME, notes);
		addEntryIfKeyMissing(properties, PROP_PASSWORD, notes);
		addEntryIfKeyMissing(properties, PROP_TYPE, notes);
		addEntryIfKeyMissing(properties, PROP_HOST, notes);
		addEntryIfKeyMissing(properties, PROP_PORT, notes);
		addEntryIfKeyMissing(properties, PROP_SSL, notes);
		
		if (notes.getValidationEntries().isEmpty()) {
			try {
				Properties authProperties = convertInputPropertiesToAuthProperties(properties);
				Session session = Session.getInstance(authProperties);
				Store store = session.getStore();
				store.connect(authProperties.getProperty("mail.host"),
						authProperties.getProperty("mail.user"),
						authProperties.getProperty("mail.password"));
				store.close();
				return notes;
			} catch (NoSuchProviderException e) {
				notes.addValidationEntry(ValidationExceptionType.AuthException, e);
			} catch (MessagingException e) {
				notes.addValidationEntry(ValidationExceptionType.AuthException, e);
			}
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
