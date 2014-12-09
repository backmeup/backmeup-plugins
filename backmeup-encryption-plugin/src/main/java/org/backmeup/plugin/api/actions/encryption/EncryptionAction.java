package org.backmeup.plugin.api.actions.encryption;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptionAction implements Action {
    private final Logger logger = LoggerFactory.getLogger(EncryptionAction.class);

    private final String PROP_CONT_COUNT = "org.backmeup.filesplitting.containercount";
    private final String PROP_PASSWORD = "org.backmeup.encryption.password";

    @Override
    public void doAction(Properties accessData, Properties parameters, List<String> options, Storage storage,
            BackupJobDTO job, Progressable progressor) throws ActionException {

        this.logger.debug("###############################################################");
        this.logger.debug("Start encryption Plugin");

        String password;
        int containers;
        long[] containersize;
        String[] containername;

        if (parameters.containsKey(this.PROP_CONT_COUNT) == true) {
            containers = new Integer(parameters.getProperty(this.PROP_CONT_COUNT));
        } else {
            throw new ActionException("Property \"" + this.PROP_CONT_COUNT + "\" is not set");
        }

        containersize = new long[containers];
        containername = new String[containers];
        for (int i = 0; i < containers; i++) {
            if (parameters.containsKey("org.backmeup.filesplitting.container." + i + ".size") == true) {
                containersize[i] = new Long(parameters.getProperty("org.backmeup.filesplitting.container." + i
                        + ".size"));
                // add 30% to size for filesystem
                containersize[i] += containersize[i] / 100 * 30;

                if (containersize[i] < 10485760) {
                    containersize[i] = 10485760;
                }
            } else {
                throw new ActionException("Property \"org.backmeup.filesplitting.container." + i + ".size\" is not set");
            }

            if (parameters.containsKey("org.backmeup.filesplitting.container." + i + ".name") == true) {
                containername[i] = parameters.getProperty("org.backmeup.filesplitting.container." + i + ".name");
            } else {
                throw new ActionException("Property \"org.backmeup.filesplitting.container." + i + ".name\" is not set");
            }
        }

        if (parameters.containsKey(this.PROP_PASSWORD) == true) {
            password = parameters.getProperty(this.PROP_PASSWORD);
        } else {
            throw new ActionException("Property \"" + this.PROP_PASSWORD + "\" is not set");
        }

        EncryptionContainers enccontainers = new EncryptionContainers(containers, containersize, containername,
                password);
        try {
            Iterator<DataObject> dataObjects = storage.getDataObjects();
            while (dataObjects.hasNext() == true) {
                DataObject daob = dataObjects.next();
                enccontainers.addFile(daob);
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }

        for (EncryptionContainer container : enccontainers.getContainers()) {
            try {
                this.logger.debug("Write container start");
                container.writeContainer();
                this.logger.debug("Write container finished");

                this.logger.debug("Delete files start");
                // remove the partxxx folder in the storage
                storage.removeDir(container.getContainername());
                this.logger.debug("Delete files finished");

                this.logger.debug("Move containers to FS start");
                storage.addFile(container.getContainer(), container.getContainername(), new MetainfoContainer());
                this.logger.debug("Move containers to FS finished");
                container.deleteContainer();
            } catch (Exception e) {
                throw new ActionException(e);
            }
        }

        enccontainers.cleanupFolders();
    }
}
