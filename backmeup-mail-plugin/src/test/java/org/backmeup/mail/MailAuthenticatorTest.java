package org.backmeup.mail;

import java.io.IOException;

import org.backmeup.mail.test.TestDataManager;
import org.backmeup.model.ValidationNotes;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.exceptions.ValidationException;
import org.backmeup.plugin.api.Authorizable.AuthorizationType;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.ServerSetupTest;

public class MailAuthenticatorTest {
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);
    
    private GreenMailUser user;

    @Before
    public void setup() throws StorageException, IOException {
        AuthDataDTO mailAuthData = TestDataManager.getAuthDataEmail();
        String username = mailAuthData.getProperties().get("Username");
        String password = mailAuthData.getProperties().get("Password");
        user = greenMail.setUser("test@localhost.com", username, password);
    }

    @Test
    public void testAuthType() throws StorageException {
        MailAuthenticator authenticator = new MailAuthenticator();
        Assert.assertEquals(AuthorizationType.INPUTBASED, authenticator.getAuthType());
    }

    @Test
    public void testAuthorize() throws StorageException {
        Assert.assertNotNull(user);
        AuthDataDTO mailAuthData = TestDataManager.getAuthDataEmail();
        MailAuthenticator authenticator = new MailAuthenticator();
        Assert.assertTrue(authenticator.isValid(mailAuthData.getProperties()));
        String username = authenticator.authorize(mailAuthData.getProperties());
        Assert.assertNotNull(username);
    }
    
    @Test(expected = ValidationException.class)
    public void testAuthorizeFails() throws StorageException {
        Assert.assertNotNull(user);
        AuthDataDTO mailAuthData = TestDataManager.getAuthDataEmail();
        mailAuthData.getProperties().put("Username", "wrongUsername");
        MailAuthenticator authenticator = new MailAuthenticator();
        Assert.assertTrue(authenticator.isValid(mailAuthData.getProperties()));
        authenticator.authorize(mailAuthData.getProperties());
    }
    
    @Test
    public void testMissingInputField() throws StorageException {
        AuthDataDTO mailAuthData = TestDataManager.getAuthDataEmail();
        mailAuthData.getProperties().remove("Username");
        MailAuthenticator authenticator = new MailAuthenticator();
        Assert.assertFalse(authenticator.isValid(mailAuthData.getProperties()));
        ValidationNotes notes = authenticator.validateInputFields(mailAuthData.getProperties());
        Assert.assertEquals(1, notes.getValidationEntries().size());
    }
}
