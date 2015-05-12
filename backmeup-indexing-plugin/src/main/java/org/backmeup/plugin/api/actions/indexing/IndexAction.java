package org.backmeup.plugin.api.actions.indexing;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.backmeup.index.api.IndexDocumentUploadClient;
import org.backmeup.index.api.IndexFields;
import org.backmeup.index.client.IndexClientFactory;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexAction implements Action {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexDocumentUploadClient client;

    /**
     * This class must be public and have a public default constructor for it to be usable by the osgi Service Component
     */
    public IndexAction() {
        this.logger.debug("initialized IndexAction default constructor");
    }

    /**
     * This constructor is used for junit testing to inject a IndexClient to overwrite the client obtained within
     * initIndexClient(long userId);
     * 
     * @param client
     */
    public IndexAction(IndexDocumentUploadClient client) {
        this.client = client;
    }

    private static final String START_INDEX_PROCESS = ">>>>>Starting index plugin ";
    private static final String ANALYZING = "Analyzing data object ";
    private static final String SKIPPING_TIKA_ANALYSIS = "This filetype is not analysed (tika blacklist) ";
    private static final String SKIPPING_PUSHING_TO_ES = "This file is not sent to ElasticSearch (ES blacklist) ";
    private static final String INDEXING_OBJECT_STARTED = "Elastic Search Indexing data object started ";
    private static final String INDEXING_OBJECT_COMPLETED = "Elastic Search Indexing data object completed ";
    private static final String INDEX_PROCESS_COMPLETE = ">>>>>Indexing plugin completed ";
    private static final String ERROR_SKIPPING_ITEM = "Error indexing data object, skipping object ";

    @Override
    public void doAction(Map<String, String> authData, Map<String, String> properties, List<String> options,
            Storage storage, BackupJobExecutionDTO job, Progressable progressor) throws ActionException {

        int indexedItems_OK = 0;
        int indexedItems_SKIPPED_TIKA_ANALYSIS = 0;
        int indexedItems_SKIPPED_ERROR = 0;
        int indexedItems_SKIPPED_BLACKLSIT = 0;

        this.logger.debug("Starting file analysis...");
        progressor.progress(START_INDEX_PROCESS);

        TikaAnalyzer analyzer = new TikaAnalyzer(progressor);

        Date indexingTimestamp = new Date();

        try {
            Iterator<DataObject> dataObjects = storage.getDataObjects();
            while (dataObjects.hasNext()) {
                try {
                    DataObject dob = dataObjects.next();
                    progressor.progress(ANALYZING + dob.getPath());

                    String mime = null;
                    Map<String, String> meta = new HashMap<>();
                    String fulltext = null;

                    if (needsTikaAnalysis(dob)) {
                        //call Apache Tika to analyze the object
                        meta = analyzer.analyze(dob);
                        mime = meta.get(IndexFields.FIELD_CONTENT_TYPE);
                        if (mime != null) {
                            fulltext = analyzer.extractFullText(dob);
                            if (fulltext != null) {
                                meta.put(IndexFields.FIELD_FULLTEXT, fulltext);
                            }
                        }
                    } else {
                        progressor.progress(SKIPPING_TIKA_ANALYSIS + dob.getPath());
                        indexedItems_SKIPPED_TIKA_ANALYSIS++;
                    }

                    progressor.progress(INDEXING_OBJECT_STARTED + dob.getPath());
                    //init index client for user
                    initIndexClient(job.getUser().getUserId());
                    ElasticSearchIndexer indexer = new ElasticSearchIndexer(this.client);

                    if (needsESIndexing(dob)) {
                        this.logger.debug("Indexing " + dob.getPath());
                        //extract information to ElasticSearch compatible format and upload to queue
                        indexer.doIndexing(properties, job, dob, meta, indexingTimestamp);
                        indexedItems_OK++;
                        progressor.progress(INDEXING_OBJECT_COMPLETED + dob.getPath());
                    } else {
                        indexedItems_SKIPPED_BLACKLSIT++;
                        progressor.progress(SKIPPING_PUSHING_TO_ES + dob.getPath());
                    }

                } catch (Exception e) {
                    indexedItems_SKIPPED_ERROR++;
                    progressor.progress(ERROR_SKIPPING_ITEM + " " + e.toString());
                }
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }

        progressor.progress(INDEX_PROCESS_COMPLETE + " # of items indexed OK: " + indexedItems_OK + " , SKIPPED: "
                + indexedItems_SKIPPED_BLACKLSIT + ", ERROR: " + indexedItems_SKIPPED_ERROR
                + "# of items tika analysis SKIPPED: " + indexedItems_SKIPPED_TIKA_ANALYSIS);
    }

    private boolean needsESIndexing(DataObject dob) {

        if (dob.getPath().endsWith(".css"))
            return false;

        if (dob.getPath().endsWith(".xsd"))
            return false;

        return true;

    }

    private boolean needsTikaAnalysis(DataObject dob) {
        // blacklist approach, try to extract tika metadata for as many objects as possible
        if (dob.getPath().endsWith(".css"))
            return false;

        if (dob.getPath().endsWith(".xsd"))
            return false;

        /*if (dob.getPath().endsWith(".jpg"))
            return false;

        if (dob.getPath().endsWith(".txt"))
            return true;

        if (dob.getPath().endsWith(".pdf"))
            return true;

        if (dob.getPath().endsWith(".html"))
            return true;*/

        return true;
    }

    private void initIndexClient(Long userId) {
        if (userId != null) {
            this.client = new IndexClientFactory().getIndexDocumentUploadClient(userId);
        }
    }

}
