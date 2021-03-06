package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.backmeup.index.api.IndexDocumentUploadClient;
import org.backmeup.index.api.IndexFields;
import org.backmeup.index.model.IndexDocument;
import org.backmeup.index.serializer.Json;
import org.backmeup.model.dto.BackupJobExecutionDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.Metadata;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.storage.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The ElasticSearchIndexer takes data created by other plugins and transforms it into a ElasticSearch specific data
 * representation Interaction with ElasticSearch: The indexer must be handed an ElasticSearch Client in order to
 * communicate. In Themis every user is provided with a custom ElasticSearch instance for security reasons. This is
 * assigned by the framework and mounted within Truecrypt.
 * 
 * For manually talking to an existing ElasticSearch cluster, I recommend using a TransportClient, like so: Client
 * client = new TransportClient() .addTransportAddress(new InetSocketTransportAddress("host1", 9300))
 * .addTransportAddress(new InetSocketTransportAddress("host2", 9300)); It is possible to add arbitrary numbers of
 * transport addresses - the client will communicate with them in round-robin fashion.
 */
public class ElasticSearchIndexer {

    private final IndexDocumentUploadClient client;
    private final Logger log = LoggerFactory.getLogger(getClass());

    //used to be pushing elements directly to ES via the IndexClient
    //now dropp-off in queue for sharing distribution
    public ElasticSearchIndexer(IndexDocumentUploadClient client) {
        this.client = client;
    }

    public void doIndexing(PluginContext pluginContext, DataObject dataObject, Map<String, String> meta, Date timestamp) throws IOException {
        // Build the index object
        IndexDocument document = new IndexDocument();

        //get the standardized geo + temporal metadata from the plugins or tika and add it to the index
        MetainfoContainer metaInfoContainer = dataObject.getMetainfo();
        setStandardizedGeoAndTemporalMetadata(meta, metaInfoContainer, document);

        //add all metadata provided by Tika 
        for (String metaKey : meta.keySet()) {
            document.field(metaKey, meta.get(metaKey));
        }

        String relObjectPathOnStorage = getBMULocation(pluginContext) + dataObject.getPath();

        BackupJobExecutionDTO job = pluginContext.getAttribute("org.backmeup.job", BackupJobExecutionDTO.class);
        document.field(IndexFields.FIELD_OWNER_ID, job.getUser().getUserId());
        document.field(IndexFields.FIELD_OWNER_NAME, job.getUser().getUsername());
        document.field(IndexFields.FIELD_FILENAME, getFilename(dataObject.getPath()));
        document.field(IndexFields.FIELD_PATH, relObjectPathOnStorage);
        document.field(IndexFields.FIELD_FILE_HASH, dataObject.getMD5Hash());
        document.field(IndexFields.FIELD_BACKUP_AT, timestamp.getTime());
        document.field(IndexFields.FIELD_JOB_ID, job.getJobId());
        document.field(IndexFields.FIELD_JOB_NAME, job.getName());
        document.field(IndexFields.FIELD_INDEX_DOCUMENT_UUID, UUID.randomUUID().toString());

        // There is currently only one source per job!
        PluginProfileDTO sourceProfile = job.getSource();

        if (sourceProfile != null) {
            document.field(IndexFields.FIELD_BACKUP_SOURCE_PLUGIN_ID, sourceProfile.getPluginId());
            document.field(IndexFields.FIELD_BACKUP_SOURCE_PROFILE_ID, sourceProfile.getProfileId());
            if ((sourceProfile.getAuthData() != null) && (sourceProfile.getAuthData().getName() != null)) {
                document.field(IndexFields.FIELD_BACKUP_SOURCE_AUTH_TITLE, sourceProfile.getAuthData().getName());
            }
        }

        PluginProfileDTO sinkProfile = job.getSink();

        if (sinkProfile != null) {
            document.field(IndexFields.FIELD_BACKUP_SINK_PLUGIN_ID, sinkProfile.getPluginId());
            document.field(IndexFields.FIELD_BACKUP_SINK_PROFILE_ID, sinkProfile.getProfileId());
            if ((sinkProfile.getAuthData() != null) && (sinkProfile.getAuthData().getName() != null)) {
                document.field(IndexFields.FIELD_BACKUP_SINK_AUTH_TITLE, sinkProfile.getAuthData().getName());
            }
        }

        if (pluginContext != null) {
            //check if download access is supported by the sink plugin
            if (pluginContext.hasAttribute(Metadata.STORAGE_ALWAYS_ACCESSIBLE) && pluginContext.hasAttribute(Metadata.DOWNLOAD_BASE)) {
                boolean alwaysAccess = Boolean.parseBoolean(pluginContext.getAttribute(Metadata.STORAGE_ALWAYS_ACCESSIBLE, String.class));
                String downloadBase = pluginContext.getAttribute(Metadata.DOWNLOAD_BASE, String.class);
                //we're having a file sink like the themis central storage with permanent access
                if (alwaysAccess) {
                    document.field(IndexFields.FIELD_SINK_DOWNLOAD_BASE, downloadBase);
                }
            }

        }

        //add the metadata fields added in the Metainfo Container File
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

        this.log.debug("Started uploading IndexDocument to queue from Indexing Plugin " + Json.serialize(document));
        // no direct indexing within the plugin anymore -> upload to drop-of queue for further distribution
        String message = this.client.uploadForSharing(document);
        this.log.debug("Completed uploading IndexDocument to queue from Indexing Plugin: " + message);
    }

    /**
     * Takes specific geo + temporal elements coming from either the Metainfo container by the plugins or Tika (if not
     * all values are set by the plugins. e.g. document analysis vs. facebook plugin) and adds them to the Index
     */
    private void setStandardizedGeoAndTemporalMetadata(Map<String, String> tikaMetadata, MetainfoContainer pluginMetadataContainer,
            IndexDocument document) {

        StandardizedMetadataExtractor stMeta = new StandardizedMetadataExtractor(tikaMetadata, pluginMetadataContainer);

        //populate location name
        if (stMeta.getLocationName() != null) {
            document.field(IndexFields.FIELD_LOC_NAME, stMeta.getLocationName());
        }
        //populate location country
        if (stMeta.getCountry() != null) {
            document.field(IndexFields.FIELD_LOC_COUNTRY, stMeta.getCountry());
        }
        //populate location city
        if (stMeta.getCity() != null) {
            document.field(IndexFields.FIELD_LOC_CITY, stMeta.getCity());
        }
        //populate latitude geo coordinate
        if (stMeta.getLatitude() != null) {
            document.field(IndexFields.FIELD_LOC_LATITUDE, stMeta.getLatitude() + "");
        }
        //populate longitude geo coordinate
        if (stMeta.getLongitude() != null) {
            document.field(IndexFields.FIELD_LOC_LONGITUDE, stMeta.getLongitude() + "");
        }
        //populate author name
        if (stMeta.getAuthor() != null) {
            document.field(IndexFields.FIELD_DOC_AUTHOR, stMeta.getAuthor());
        }
        //populate document creation date
        if (stMeta.getCreationDate() != null) {
            document.field(IndexFields.FIELD_DOC_CREATION_DATE, stMeta.getCreationDate().getTime());
        }
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
    private String getBMULocation(PluginContext context) {
        if (context.hasAttribute("org.backmeup.bmuprefix")) {
            return context.getAttribute("org.backmeup.bmuprefix", String.class);
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
