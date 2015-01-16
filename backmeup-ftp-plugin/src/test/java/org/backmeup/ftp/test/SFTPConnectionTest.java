package org.backmeup.ftp.test;

import static org.junit.Assert.assertTrue;

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

public class SFTPConnectionTest {

    @Rule
    public SFTPServerSetup sftpServer = new SFTPServerSetup();

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void testReadFromFtpAndWriteLocally() {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession("testUser1", "127.0.0.1", SFTPServerSetup.PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("testUser1");
            session.connect(60000);

            Channel channel = session.openChannel("sftp");
            channel.connect(60000);
            sftpChannel = (ChannelSftp) channel;

            assertTrue("sftp connected", channel.isConnected());

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
            sftpChannel.get("src/test/resources/TestSrc.txt", "src/test/resources/ftp/testUser1/localfile.txt");

            sftpChannel.cd("src/test/resources/ftp/testUser1");
            vEntries = sftpChannel.ls(".");
            boolean bFound = false;
            for (LsEntry entry : vEntries) {
                if (entry.getFilename().equals("localfile.txt")) {
                    bFound = true;
                }
            }
            assertTrue("found file", bFound);

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
