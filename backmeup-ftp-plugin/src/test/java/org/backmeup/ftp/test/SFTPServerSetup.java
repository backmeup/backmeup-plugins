package org.backmeup.ftp.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.rules.ExternalResource;

public class SFTPServerSetup extends ExternalResource {

    private SshServer sshd;
    public static final int PORT = 22;
    File sftpWorkingDir = new File("src/test/resources/ftp");

    public SFTPServerSetup() {
        createFtpWorkingDir();
        initSFTPServer(PORT);
    }

    @Override
    public void before() {
        try {
            //create working dir
            createFtpWorkingDir();
            //start the sftp server
            this.sshd.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void after() {
        try {
            //stop the sftp server
            this.sshd.stop();
            //cleanup working dir
            deleteFtpWorkingDir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSFTPServer(int port) {
        this.sshd = SshServer.setUpDefaultServer();
        this.sshd.setPort(port);
        setFileSystemFactory(this.sshd);
        this.sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        this.sshd.setCommandFactory(new ScpCommandFactory());
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        this.sshd.setSubsystemFactories(namedFactoryList);

        this.sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return username != null && username.equals(password);
            }
        });
    }

    private void setFileSystemFactory(SshServer sshd) {
        sshd.setFileSystemFactory(new NativeFileSystemFactory() {
            @Override
            public void setCreateHome(boolean createHome) {
                super.setCreateHome(true);
            }

            @Override
            public FileSystemView createFileSystemView(final Session session) {

                String userName = session.getUsername();
                // create home if does not exist
                String homeDirStr = "src/test/resources/ftp/" + userName;
                System.out.println("creating: " + homeDirStr);
                File homeDir = new File(homeDirStr);

                if ((!homeDir.exists()) && (!homeDir.mkdirs())) {
                    System.out.println("Cannot create user home :: " + homeDirStr);
                }

                return new NativeFileSystemView(session.getUsername(), false);
            };
        });
    }

    private void createFtpWorkingDir() {
        if (!this.sftpWorkingDir.exists()) {
            this.sftpWorkingDir.mkdirs();
        }
    }

    private void deleteFtpWorkingDir() {
        try {
            delete(this.sftpWorkingDir);
            delete(new File("hostkey.ser"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

}
