package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.index.api.IndexClient;
import org.backmeup.index.api.IndexFields;
import org.backmeup.index.model.IndexDocument;
import org.backmeup.index.serializer.Json;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The indexer must be handed an ElasticSearch client in order to work. (This must be done by the class orchestrating
 * the backup workflow!) Cf. IndexActionTest for an example on how to create a client talking to an ad-hoc embedded
 * ElasticSearch node.
 * 
 * To talk to an existing ElasticSearch cluster, I recommend using a TransportClient, like so:
 * 
 * Client client = new TransportClient() .addTransportAddress(new InetSocketTransportAddress("host1", 9300))
 * .addTransportAddress(new InetSocketTransportAddress("host2", 9300));
 * 
 * It is possible to add arbitrary numbers of transport addresses - the client will communicate with them in round-robin
 * fashion.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class ElasticSearchIndexer {

    private final IndexClient client;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ElasticSearchIndexer(IndexClient client) {
        this.client = client;
    }

    public void doIndexing(Properties externalProps, BackupJobDTO job, DataObject dataObject, Map<String, String> meta,
            Date timestamp) throws IOException {
        // Build the index object
        IndexDocument document = new IndexDocument();

        for (String metaKey : meta.keySet()) {
            document.field(metaKey, meta.get(metaKey));
        }

        String relObjectPathOnStorage = getBMULocation(externalProps) + dataObject.getPath();

        document.field(IndexFields.FIELD_OWNER_ID, job.getUser().getUserId());
        document.field(IndexFields.FIELD_OWNER_NAME, job.getUser().getUsername());
        document.field(IndexFields.FIELD_FILENAME, getFilename(dataObject.getPath()));
        document.field(IndexFields.FIELD_PATH, relObjectPathOnStorage);
        document.field(IndexFields.FIELD_FILE_HASH, dataObject.getMD5Hash());
        document.field(IndexFields.FIELD_BACKUP_SINK, job.getSink().getAuthData().getName());
        document.field(IndexFields.FIELD_BACKUP_AT, timestamp.getTime());
        document.field(IndexFields.FIELD_JOB_ID, job.getJobId());
        document.field(IndexFields.FIELD_JOB_NAME, job.getJobTitle());

        // There is currently only one source per job!
        PluginProfileDTO sourceProfile = job.getSource();

        if (sourceProfile != null) {
            document.field(IndexFields.FIELD_BACKUP_SOURCE_ID, sourceProfile.getPluginId());
            document.field(IndexFields.FIELD_BACKUP_SOURCE_PLUGIN_NAME, sourceProfile.getAuthData().getName());
            document.field(IndexFields.FIELD_BACKUP_SOURCE_IDENTIFICATION, sourceProfile.getProfileId());
        }

        if (externalProps != null) {
            //check if download access is supported by the sink plugin
            if (externalProps.containsKey(Metadata.STORAGE_ALWAYS_ACCESSIBLE)
                    && externalProps.containsKey(Metadata.DOWNLOAD_BASE)) {
                boolean alwaysAccess = Boolean.parseBoolean((String) externalProps
                        .get(Metadata.STORAGE_ALWAYS_ACCESSIBLE));
                String downloadBase = (String) externalProps.get(Metadata.DOWNLOAD_BASE);
                //we're having a file sink like the themis central storage with permanent access
                if (alwaysAccess) {
                    document.field(IndexFields.FIELD_SINK_DOWNLOAD_BASE, downloadBase);
                }
            }

        }

        MetainfoContainer metainfoContainer = dataObject.getMetainfo();
        if (metainfoContainer != null) {
            Iterator<Metainfo> it = metainfoContainer.iterator();
            while (it.hasNext()) {
                Properties metainfo = it.next().getAttributes();
                for (Object key : metainfo.keySet()) {
                    document.largeField(key.toString(), metainfo.get(key).toString());
                }
            }
        }

        if (document.getLargeFields().containsKey("thumbnail_path")) {
            String tempThumbFileLocation = document.getLargeFields().get("thumbnail_path");
            String relThumbPathOnStorage = buildThumnailPath(relObjectPathOnStorage, tempThumbFileLocation);
            document.largeField("thumbnail_path", relThumbPathOnStorage);
        }

        this.log.debug("Started pushing IndexDocument to ES from Indexing Plugin " + Json.serialize(document));
        // Push to ES index
        this.client.index(document);
        this.log.debug("Completed pushing IndexDocument to ES from Indexing Plugin ");

        //TODO AL Persist document for later ingestion instead pushing to ES.
    }

    private String getFilename(String path) {
        if (path.indexOf('/') > -1) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

    /**
     * returns e.g. BMU_dummy_494_22_01_2015_22_14
     * 
     * @param accessData
     * @return
     */
    private String getBMULocation(Properties p) {
        if (p.containsKey("org.backmeup.bmuprefix")) {
            return p.getProperty("org.backmeup.bmuprefix");
        } else {
            return "";
        }
    }

    /**
     * @param parentObjectPath
     *            /BMU_filegenerator_553_28_01_2015_00_30/file10.jpg
     * @param thumbnailLocalLocation
     *            is a file path on the local disk e.g.
     *            C:\\data\\thumbnails\\BMU_filegenerator_553_28_01_2015_00_30\\1422401430361_file10.jpg_thumb.jpg
     * @return relative path of thumbnail on storage to ingest into elasticsearch
     */
    private String buildThumnailPath(String parentOjbectPath, String thumbnailLocalLocation) {

        if (thumbnailLocalLocation.indexOf('\\') > -1) {
            thumbnailLocalLocation = thumbnailLocalLocation.replace('\\', '/');
        }
        String fileName = getFilename(thumbnailLocalLocation);
        String pathPrefix = "";
        if (thumbnailLocalLocation.indexOf('/') > -1) {
            pathPrefix = parentOjbectPath.substring(0, parentOjbectPath.lastIndexOf('/'));
        }
        return pathPrefix + "/thumbs/" + fileName;
    }
}
