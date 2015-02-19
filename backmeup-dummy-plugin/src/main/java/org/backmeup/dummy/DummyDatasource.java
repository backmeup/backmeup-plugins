package org.backmeup.dummy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class DummyDatasource implements Datasource, Validationable {
    
    private static final List<String> BACKUP_OPTIONS = new ArrayList<>();
    
    static {
        BACKUP_OPTIONS.add("option1");
        BACKUP_OPTIONS.add("option2");
        BACKUP_OPTIONS.add("fail validation");
        BACKUP_OPTIONS.add("fail hard");
    }

    private InputStream stringToStream(String input) {
        try {
            InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
            return is;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF8 is not available?");
        }
    }

    private Metainfo create(String id, String type, String destination) {
        Metainfo info = new Metainfo();
        info.setBackupDate(new Date());
        info.setDestination(destination);
        info.setId(id);
        info.setSource("dummy");
        info.setType(type);
        return info;
    }

    @Override
    public void downloadAll(Map<String, String> accessData, Map<String, String> properties, List<String> options, Storage storage, Progressable progressor) throws StorageException {
        MetainfoContainer cont = new MetainfoContainer();
        cont.addMetainfo(create("1", "text/plain", "/plain.txt"));
        InputStream is = stringToStream("This is an important text file.\nPlease create a backup with this file");
        storage.addFile(is, "/plain.txt", cont);

        cont = new MetainfoContainer();
        cont.addMetainfo(create("2", "text/html", "/html.txt"));
        is = stringToStream("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
            + "http://www.w3.org/TR/html4/strict.dtd\">"
            + "<html>"
            + "<head>"
            + "    <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">"
            + "    <title>title</title>"
            + "</head>"
            + "<body><p>This is one important text file.\nPlease create a backup with this file</p></body></html>");
        storage.addFile(is, "/html.txt", cont);
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
    public List<String> getAvailableOptions(Map<String, String> accessData) {
        return Collections.unmodifiableList(BACKUP_OPTIONS);
    }

    @Override
    public ValidationNotes validateOptions(List<String> options) {
        ValidationNotes notes = new ValidationNotes();
        for (String option : options) {
            if (!BACKUP_OPTIONS.contains(option)) {
                notes.addValidationEntry(ValidationExceptionType.ConfigException, DummyDescriptor.DUMMY_ID, "Option \""+option+"\" not available");
            }
        }
        if (options.contains("fail validation")) {
            notes.addValidationEntry(ValidationExceptionType.ConfigException, DummyDescriptor.DUMMY_ID, "Option fail selected -> failing");
        }
        if (options.contains("fail hard")) {
            throw new PluginException(DummyDescriptor.DUMMY_ID, "forced to fail hard!");
        }
        return notes;
    }

}
