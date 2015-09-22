package org.backmeup.sftp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.activation.MimetypesFileTypeMap;

import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.Datasource;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * The DropboxDatasource is capable of listing all directories and files of a certain directory and of downloading
 * certain files from Dropbox.
 * 
 * @author fschoeppl
 */
public class SftpDatasource implements Datasource {
	
	private static final String SFTP = "sftp";

    private final ResourceBundle textBundle = ResourceBundle.getBundle(SftpDatasource.class.getSimpleName());
    private static final String INDEX_HTML_WRAP = "org.backmeup.sftp.SftpDatasource.INDEX_HTML_WRAP";
    private static final String INDEX_HTML_ENTRY = "org.backmeup.sftp.SftpDatasource.INDEX_HTML_ENTRY";

    public SftpDatasource() {
    }

    private Metainfo create(String id, String type, String destination) {
        Metainfo info = new Metainfo();
        info.setBackupDate(new Date());
        info.setDestination(destination);
        info.setId(id);
        info.setSource(SFTP);
        info.setType(type);
        return info;
    }
    
    @Override
    public void downloadAll(PluginProfileDTO pluginProfile, PluginContext pluginContext,
    						Storage storage, Progressable progressor) throws StorageException {
    	Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
        	JSch jsch = new JSch();
        	
        	Map<String, String> accessData = pluginProfile.getAuthData().getProperties();
            progressor.progress("Connecting to sftp server " + accessData.get("Host"));
            
            session = jsch.getSession(accessData.get("Username"), accessData.get("Host"), Integer.parseInt(accessData.get("Port")));
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(accessData.get("Password"));
            session.connect(60000);
           
            Channel channel = session.openChannel("sftp");
            channel.connect(60000);
            sftpChannel = (ChannelSftp) channel;

            if(channel.isConnected()) {
            	progressor.progress("Connection successfull");
            } else {
                throw new PluginException(SftpDescriptor.SFTP_ID, "Could not connect to sftp server " + accessData.get("Host"));
            }

            progressor.progress("Change to remote folder");
            sftpChannel.cd(accessData.get("Folder"));
            
            StringBuilder sb = new StringBuilder(); 
            handleDownloadAll(".", sftpChannel, storage, progressor, sb);

            String indexHtml = MessageFormat.format(this.textBundle.getString(INDEX_HTML_WRAP), sb.toString());
        	storage.addFile(new ByteArrayInputStream(indexHtml.getBytes("UTF-8")), "index.html", new MetainfoContainer());
        	
            sftpChannel.exit();
            session.disconnect();
            
            progressor.progress("Download completed");

        } catch (JSchException | SftpException | UnsupportedEncodingException e) {
        	progressor.progress(e.toString());
            progressor.progress(e.getMessage());
            throw new PluginException(SftpDescriptor.SFTP_ID, "An error occured during the backup", e);
        } finally {
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
    
    private void handleDownloadAll(String folder, ChannelSftp sftpChannel, Storage storage, Progressable progressor, StringBuilder sb) throws SftpException, StorageException {
    	List<String> toVisit = new ArrayList<>();
    	
        progressor.progress("Get list of folders");
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> vEntries = sftpChannel.ls(folder);
        for (LsEntry entry : vEntries) {
        	String fname = entry.getFilename();
        	if (".".equals(fname) || "..".equals(fname)) {
        		continue;
        	}
	        MetainfoContainer cont = new MetainfoContainer();
	        SftpATTRS attrs = entry.getAttrs();
	        if(attrs.isDir()) {
	        	cont.addMetainfo(create("1", "directory", fname));
	        	toVisit.add(entry.getFilename());
	        } else {
	        	String xName = folder+"/"+fname;
	        	xName = xName.substring(2);
	        	String type = new MimetypesFileTypeMap().getContentType(fname);
		        cont.addMetainfo(create("1", type, fname));
	        	InputStream is = sftpChannel.get(xName);
	        	
		        storage.addFile(is, xName, cont);
	        	sb.append( MessageFormat.format(this.textBundle.getString(INDEX_HTML_ENTRY), xName, attrs.getPermissionsString(),
	    				attrs.getSize(), attrs.getMtimeString(), xName ) );
	        }
	    }
        
        for (String f : toVisit) {
        	handleDownloadAll(folder+"/"+f, sftpChannel, storage, progressor, sb);
        }
    } 
}
