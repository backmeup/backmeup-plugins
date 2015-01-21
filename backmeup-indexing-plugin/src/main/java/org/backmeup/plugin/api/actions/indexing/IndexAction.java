package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.backmeup.index.api.IndexClient;
import org.backmeup.index.api.IndexFields;
import org.backmeup.index.client.IndexClientFactory;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class IndexAction implements Action {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexClient client;

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
    public IndexAction(IndexClient client) {
        this.client = client;
    }

    private static final String START_INDEX_PROCESS = ">>>>>Starting index plugin ";
    private static final String ANALYZING = "Analyzing data object ";
    private static final String SKIPPING_TIKA_ANALYSIS = "This filetype is not analysed (tika blacklist) ";
    private static final String INDEXING_OBJECT_STARTED = "Elastic Search Indexing data object started ";
    private static final String INDEXING_OBJECT_COMPLETED = "Elastic Search Indexing data object completed ";
    private static final String INDEX_PROCESS_COMPLETE = ">>>>>Indexing plugin completed ";
    private static final String ERROR_SKIPPING_ITEM = "Error indexing data object, skipping object ";

    @Override
    public void doAction(Properties accessData, Properties parameters, List<String> options, Storage storage,
            BackupJobDTO job, Progressable progressor) throws ActionException {

        int indexedItems_OK = 0;
        int indexedItems_SKIPPED_TIKA_ANALYSIS = 0;
        int indexedItems_SKIPPED_ERROR = 0;

        this.logger.debug("Starting file analysis...");
        progressor.progress(START_INDEX_PROCESS);

        TikaAnalyzer analyzer = new TikaAnalyzer();

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

                    if (needsIndexing(dob)) {
                        //call Apache Tika to analyze the object
                        meta = analyzer.analyze(dob);
                        mime = meta.get("Content-Type");
                        if (mime != null) {
                            fulltext = extractFullText(dob, meta.get("Content-Type"));
                        }
                    } else {
                        progressor.progress(SKIPPING_TIKA_ANALYSIS + dob.getPath());
                        indexedItems_SKIPPED_TIKA_ANALYSIS++;
                    }

                    progressor.progress(INDEXING_OBJECT_STARTED + dob.getPath());
                    initIndexClient(job.getUser().getUserId());
                    ElasticSearchIndexer indexer = new ElasticSearchIndexer(this.client);

                    meta = new HashMap<>();
                    meta.put(IndexFields.FIELD_CONTENT_TYPE, mime);
                    if (fulltext != null) {
                        meta.put(IndexFields.FIELD_FULLTEXT, fulltext);
                    }

                    this.logger.debug("Indexing " + dob.getPath());
                    //push information to ElasticSearch
                    indexer.doIndexing(job, dob, meta, indexingTimestamp);
                    indexedItems_OK++;
                    progressor.progress(INDEXING_OBJECT_COMPLETED + dob.getPath());

                } catch (Exception e) {
                    progressor.progress(ERROR_SKIPPING_ITEM + " " + e.toString());
                    indexedItems_SKIPPED_ERROR++;
                }
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }

        progressor.progress(INDEX_PROCESS_COMPLETE + " # of items indexed OK: " + indexedItems_OK
                + " SKIPPED (blacklist): " + indexedItems_SKIPPED_TIKA_ANALYSIS + " FAILED: "
                + indexedItems_SKIPPED_ERROR);
    }

    private boolean needsIndexing(DataObject dob) {
        // switching into whitelist approach for now
        if (dob.getPath().endsWith(".css"))
            return false;

        if (dob.getPath().endsWith(".xsd"))
            return false;

        //TODO need to fix osgi classloading issues for org.apache.tika.parser.image.MetadataExtractor -> ClassNotFoundException com.drew.metadata.MetadataException
        if (dob.getPath().endsWith(".jpg"))
            return false;

        if (dob.getPath().endsWith(".txt"))
            return true;

        if (dob.getPath().endsWith(".pdf"))
            return true;

        if (dob.getPath().endsWith(".html"))
            return true;

        return false;
    }

    private String extractFullText(DataObject dob, String contentType) throws IOException, SAXException, TikaException {
        ContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
        Metadata metadata = new Metadata();

        AutoDetectParser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);

        parser.parse(new ByteArrayInputStream(dob.getBytes()), handler, metadata, context);

        return handler.toString();
    }

    /*
    private String extractFullText_HTML(DataObject dob) throws IOException, SAXException, TikaException {		
        ContentHandler handler = new BodyContentHandler(10*1024*1024);
        Metadata metadata = new Metadata();
        new HtmlParser().parse(new ByteArrayInputStream(dob.getBytes()), handler, metadata, new ParseContext());
        return handler.toString();
    }
    */

    private void initIndexClient(Long userId) {
        this.client = new IndexClientFactory().getIndexClient(userId);
    }

}
