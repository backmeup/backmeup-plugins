package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.backmeup.index.api.IndexFields;
import org.backmeup.plugin.api.storage.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TikaAnalyzer {

    private TikaServerStub tika = new TikaServerStub();
    private final Logger log = LoggerFactory.getLogger(getClass());

    public TikaAnalyzer() {
        //check if we've got a Tika Server running - if not start one
        if (!TikaServerStub.isTikaAlive()) {
            this.log.debug("Tika server not running, starting up Tika Server instance");
            new TikaServerStartupHandler().startTikaServer();
            //give tika server a chance to startup
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            this.log.debug("Tika server is alive? " + TikaServerStub.isTikaAlive());
        } else {
            this.log.debug("Tika server up and running");
        }
    }

    /**
     * Tries to extract mime-type and metadata from DataObject via Tika
     * 
     * @param dob
     * @return
     */
    public Map<String, String> analyze(DataObject dob) {
        Map<String, String> meta = new HashMap<>();
        String mimeType = null;
        //extract contentType from object
        try {
            mimeType = this.tika.detectContentType(dob);
            if (mimeType != null) {
                meta.put(IndexFields.FIELD_CONTENT_TYPE, mimeType);

                //extract metadata from object
                Map<String, String> metadata = this.tika.extractMetaData(dob, mimeType);
                if (metadata.isEmpty()) {
                    return meta;
                } else {
                    metadata.put(IndexFields.FIELD_CONTENT_TYPE, mimeType);
                    return metadata;
                }
            }
        } catch (IOException e) {
            this.log.debug("Error calling tika server on metadata for " + dob.getPath() + " :" + e.toString());
        }
        return meta;
    }

    /**
     * Tries to extract fulltext description of this object via Tika
     * 
     * @param dob
     * @param contentType
     * @return
     */
    public String extractFullText(DataObject dob, String contentType) {
        String fulltext = null;
        try {
            fulltext = this.tika.extractFullText(dob, contentType);
        } catch (IOException e) {
            this.log.debug("Error calling tika server on fulltext for " + dob.getPath() + " :" + e.toString());
        }
        return fulltext;
    }

}
