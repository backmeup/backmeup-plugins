package org.backmeup.facebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookGraphException;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;

/**
 * The FacebookValidator makes sure that authorization was successfully
 * and checks if API-calls are working fine.
 * 
 * @author mmurauer
 * 
 */

public class FacebookValidator implements Validationable {
	@Override
	public boolean hasRequiredProperties() {
		return true;
	}

	@Override
	public List<RequiredInputField> getRequiredProperties() {
		List<RequiredInputField> inputs = new ArrayList<>();

		inputs.add(new RequiredInputField(FacebookHelper.PROPERTY_TOKEN, FacebookHelper.PROPERTY_TOKEN, FacebookHelper.PROPERTY_TOKEN_DESC, true, 1, Type.String, ""));

		return inputs;
	}

	@Override
	public ValidationNotes validateProperties(Map<String, String> properties) {
		ValidationNotes notes = new ValidationNotes();
		try {
			// Make sure authentication / authorization and API is working well
			String accessToken = properties.get(FacebookHelper.PROPERTY_TOKEN);
			if (accessToken == null)
				notes.addValidationEntry(ValidationExceptionType.AuthException, FacebookDescriptor.FACEBOOK_ID);

			FacebookClient client = new DefaultFacebookClient(accessToken);
			
			// Just to be sure about API is working well, catch user information
			User user = client.fetchObject("me", User.class);
			user.getId();

		} catch (FacebookNetworkException e) {
			notes.addValidationEntry(ValidationExceptionType.APIException, FacebookDescriptor.FACEBOOK_ID, e);
		} catch (FacebookOAuthException e) {
			notes.addValidationEntry(ValidationExceptionType.AuthException, FacebookDescriptor.FACEBOOK_ID, e);
		} catch (FacebookGraphException e) {
			notes.addValidationEntry(ValidationExceptionType.APIException, FacebookDescriptor.FACEBOOK_ID, e);
		}
		
		return notes;
	}

	@Override
	public boolean hasAvailableOptions() {
		return false;
	}

	@Override
	public List<String> getAvailableOptions() {
		return null;
	}

	@Override
	public ValidationNotes validateOptions(List<String> options) {
		return null;
	}

}
