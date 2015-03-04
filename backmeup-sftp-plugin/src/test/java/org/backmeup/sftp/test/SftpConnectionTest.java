package org.backmeup.sftp.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

public class SftpConnectionTest {

    @Rule
    public SftpServerSetup sftpServer = new SftpServerSetup();

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void testReadFromSftpAndWriteLocally() {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession("mihai", "localhost", SftpServerSetup.PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("Test123.");
            session.connect(60000);

            Channel channel = session.openChannel("sftp");
            channel.connect(60000);
            sftpChannel = (ChannelSftp) channel;

            assertTrue("sftp connected", channel.isConnected());

            sftpChannel.cd("src/test/resources/sftp/mihai");

            //browse root ftp directory
            Vector<LsEntry> vEntries = sftpChannel.ls(".");
            for (LsEntry entry : vEntries) {
                System.out.println("FileName " + entry.getFilename());
                System.out.println("LongName " + entry.getLongname());
                SftpATTRS attrs = entry.getAttrs();
                System.out.println("permission " + attrs.getPermissionsString());
                System.out.println("uid " + attrs.getUId());
                System.out.println("gid " + attrs.getGId());
                System.out.println("size " + attrs.getSize());
                System.out.println("mod.time " + attrs.getMtimeString());
                System.out.println("------------------");
            }

            //reads TestSrc from sftpServer and writes it to localfile.txt
            sftpChannel.get("TestSrc.txt", "src/test/resources/sftp/localfile.txt");

            File fTest = new File("src/test/resources/sftp/localfile.txt");
            assertTrue(fTest.exists());

            sftpChannel.exit();
            session.disconnect();

        } catch (JSchException e) {
            e.printStackTrace();
            assertTrue(e.toString(), false);
        } catch (SftpException e) {
            e.printStackTrace();
            assertTrue(e.toString(), false);
        } finally {
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
