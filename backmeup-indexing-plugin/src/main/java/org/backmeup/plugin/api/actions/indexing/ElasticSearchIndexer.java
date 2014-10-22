package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.backmeup.index.client.IndexClient;
import org.backmeup.index.client.IndexDocument;
import org.backmeup.index.client.IndexFields;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;

/**
 * The indexer must be handed an ElasticSearch client in order to work. (This must be done
 * by the class orchestrating the backup workflow!) Cf. IndexActionTest for an example on
 * how to create a client talking to an ad-hoc embedded ElasticSearch node.
 * 
 * To talk to an existing ElasticSearch cluster, I recommend using a TransportClient, like so:
 * 
 * Client client = new TransportClient()
 *      .addTransportAddress(new InetSocketTransportAddress("host1", 9300))
 *      .addTransportAddress(new InetSocketTransportAddress("host2", 9300));
 *       
 * It is possible to add arbitrary numbers of transport addresses - the client will
 * communicate with them in round-robin fashion.   
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class ElasticSearchIndexer {
	
	private final IndexClient client;
	
	public ElasticSearchIndexer(IndexClient client) {
		this.client = client;
	}
	
	public void doIndexing(BackupJob job, DataObject dataObject, Map<String, String> meta, Date timestamp) throws IOException {
		// Build the index object
	    IndexDocument document = new IndexDocument();
		
		for (String metaKey : meta.keySet()) {
			document.field(metaKey, meta.get(metaKey));
		}
		
		document.field(IndexFields.FIELD_OWNER_ID, job.getUser().getUserId());
		document.field(IndexFields.FIELD_OWNER_NAME, job.getUser().getUsername());
		document.field(IndexFields.FIELD_FILENAME, getFilename(dataObject.getPath()));
		document.field(IndexFields.FIELD_PATH, dataObject.getPath());
		document.field(IndexFields.FIELD_FILE_HASH, dataObject.getMD5Hash());
		document.field(IndexFields.FIELD_BACKUP_SINK, job.getSinkProfile().getName());
		document.field(IndexFields.FIELD_BACKUP_AT, timestamp.getTime());
		document.field(IndexFields.FIELD_JOB_ID, job.getId());
		document.field(IndexFields.FIELD_JOB_NAME, job.getJobTitle());
				
		// There is currently only one source per job!
		Profile sourceProfile = job.getSourceProfile();
		
		if (sourceProfile != null) {
			document.field(IndexFields.FIELD_BACKUP_SOURCE_ID, sourceProfile.getId());
			document.field(IndexFields.FIELD_BACKUP_SOURCE_PLUGIN_NAME, sourceProfile.getPluginId());
			document.field(IndexFields.FIELD_BACKUP_SOURCE_IDENTIFICATION, sourceProfile.getIdentification());
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
		
		// Push to ES index
		client.index(document);
	}
	
	private String getFilename(String path) {
		if (path.indexOf('/') > -1) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
		return path;
	}

}
