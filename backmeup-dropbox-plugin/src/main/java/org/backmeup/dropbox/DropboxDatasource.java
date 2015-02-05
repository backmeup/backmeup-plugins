package org.backmeup.dropbox;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.WebAuthSession;

/**
 * The DropboxDatasource is capable of listing all directories and files of a
 * certain directory and of downloading certain files from Dropbox.
 * 
 * @author fschoeppl
 */
public class DropboxDatasource extends FilesystemLikeDatasource implements Validationable {
    private static final String DROPBOX = "dropbox";
    private static final SimpleDateFormat formatter = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);

    private boolean beginsWith(List<String> collection, String beginner) {
        for (String s : collection) {
            if (beginner.startsWith(s))
                return true;
        }
        return false;
    }

    @Override
    public List<FilesystemURI> list(Map<String, String> accessData, List<String> options, FilesystemURI uri) {
        String path = uri == null ? "/" : uri.toString();
        DropboxAPI<WebAuthSession> api = DropboxHelper.getInstance().getApi(accessData);
        List<FilesystemURI> uris = new ArrayList<>();

        try {
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            path = path.replace("%20", " "); // Dropbox cannot handle %20
                                             // encoded spaces, but URI needs it
            // adjusted to maximum file_limit of 25k
            // (https://www.dropbox.com/developers/reference/api#metadata)
            Entry entry = api.metadata(path, 25000, null, true, null);
            for (Entry e : entry.contents) {
                // String encodedURI = e.path.replace(" ", "%20");
                String encodedURI = e.path;
                try {
                    encodedURI = URLEncoder.encode(e.path, "UTF-8").replace("%2F", "/");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                FilesystemURI furi = new FilesystemURI(new URI(encodedURI), e.isDir);
                Metainfo meta = new Metainfo();
                meta.setId(encodedURI);
                if (uri != null)
                    meta.setParent(uri.getMetainfoContainer().get(0).getId());
                meta.setModified(formatter.parse(e.modified));
                meta.setBackupDate(new Date());
                meta.setDestination(e.path);
                meta.setSource(DROPBOX);
                meta.setType(e.isDir ? "directory" : new MimetypesFileTypeMap().getContentType(e.path));
                furi.addMetainfo(meta);
                if (options == null || options.isEmpty() || beginsWith(options, e.path.replace(" ", "%20"))) {
                    uris.add(furi);
                }
            }
        } catch (DropboxException e) {
            throw new PluginException(
                    DropboxDescriptor.DROPBOX_ID,
                    String.format(
                            "Exception while metadata call with folder parameter %s, limit 100",
                            path), e);
        } catch (URISyntaxException e) {
            throw new PluginException(
                    DropboxDescriptor.DROPBOX_ID,
                    String.format(
                            "URISyntaxException while creating FilesystemURIs with name %s",
                            e.getInput()), e);
        } catch (ParseException e) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID,
                    String.format(
                            "ParseException during date parse process \"%s\"",
                            e.getMessage()), e);
        }
        return uris;
    }

    @Override
    public InputStream getFile(Map<String, String> accessData, List<String> options,
            FilesystemURI uri) {
        String path = "";
        try {
            try {
                path = URLDecoder.decode(uri.toString(), "UTF-8");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return DropboxHelper.getInstance().getApi(accessData)
                    .getFileStream(path, null);
        } catch (DropboxServerException e) {
            // Handle undocumented error 460 (Restricted).
            // https://forums.dropbox.com/topic.php?id=97208
            if (e.error == 460) {
                // Ignore the file if 460 comes up.
                return null;
            } else {
                throw new PluginException(DropboxDescriptor.DROPBOX_ID,
                        String.format("Error downloading file \" %s\"", path),
                        e);
            }
        } catch (DropboxException e) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID,
                    String.format("Error downloading file \" %s\"", path), e);
        }
    }
    
    @Override
    public boolean hasRequiredProperties() {
        return false;
    }

    @Override
    public List<RequiredInputField> getRequiredProperties() {
        return null;
    }

    @Override
    public ValidationNotes validateProperties(Map<String, String> properties) {
        return null;
    }

    @Override
    public boolean hasAvailableOptions() {
        return true;
    }

    @Override
    public ValidationNotes validateOptions(List<String> options) {
        return null;
    }
    
    @Override
    public List<String> getAvailableOptions(Map<String, String> accessData) {
        List<String> options = new ArrayList<>();
        if (accessData == null || accessData.isEmpty()) {
            return options;
        }
        
        DropboxAPI<WebAuthSession> api = DropboxHelper.getInstance().getApi(accessData);
        try {
            Entry entry = api.metadata("/", 25000, null, true, null);
            for (Entry e : entry.contents) {
                String encodedURI = e.path.replace(" ", "%20");
                options.add(encodedURI);
            }
        } catch (DropboxException e) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID, "Failed to determine root folders", e);
        }
        return options;
    }
}
