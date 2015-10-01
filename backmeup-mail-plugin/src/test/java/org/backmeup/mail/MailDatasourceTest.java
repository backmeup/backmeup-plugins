package org.backmeup.mail;

import java.io.File;
import java.io.IOException;

import javax.mail.internet.MimeMessage;

import org.backmeup.mail.test.TestDataManager;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;

public class MailDatasourceTest {
    private Storage storage;
    private PluginContext pluginContext;
    private File tempStorage;
    private GreenMailUser user;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @Before
    public void setup() throws StorageException, IOException {
        tempStorage = temp.newFolder();

        storage = new LocalFilesystemStorage();
        storage.open(tempStorage.getPath());

        pluginContext = new PluginContext();
        pluginContext.setAttribute("org.backmeup.tmpdir", "test");

        PluginProfileDTO mailProfile = TestDataManager.getProfileEmail();
        AuthDataDTO mailAuthData = mailProfile.getAuthData();
        String username = mailAuthData.getProperties().get("Username");
        String password = mailAuthData.getProperties().get("Password");
        user = greenMail.setUser("test@localhost.com", username, password);
    }

    @After
    public void tearDown() throws StorageException {
        // Junit automatically cleans up temporary storage directories
    }

    @Test
    public void testDownloadSimpleEmail() throws StorageException {
        user.deliver(getSimpleMessage());
        user.deliver(getSimpleMessage());
        
        PluginProfileDTO mailProfile = TestDataManager.getProfileEmail();
        MailAuthenticator authenticator = new MailAuthenticator();
        Assert.assertTrue(authenticator.isValid(mailProfile.getAuthData().getProperties()));
        authenticator.authorize(mailProfile.getAuthData().getProperties());
        
        MailDatasource mailDatasource = new MailDatasource();
        mailDatasource.downloadAll(mailProfile, pluginContext, storage, logProgressable);
        
    }

    
    public MimeMessage getSimpleMessage() {
        final String subject = GreenMailUtil.random();
        final String body = GreenMailUtil.random();
        return GreenMailUtil.createTextEmail(user.getEmail(), "from@localhost.com", subject, body,
                greenMail.getImap().getServerSetup());
    }

    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            System.out.println("PROGRESS: " + message);
        }
    };

}
