package org.backmeup.plugin.api.actions.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.backmeup.index.utils.file.FileUtils;
import org.backmeup.model.BackupJob;
import org.backmeup.model.serializer.JsonSerializer;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DummyStorage;
import org.backmeup.plugin.api.storage.Storage;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

public class IndexActionTest {

	private static Node node;

	private static final String ELASTICSEARCH_CLUSTERNAME = "testcluster";

	private static final String BACKUP_JOB_old = "{\"id\":1, \"user\":{\"userId\":1,\"username\":\"TestUser\",\"password\":\"pw\","
			+ "\"keyRing\":\"k3yr1nG\",\"email\":\"e@ma.il\",\"isActivated\":false,\"properties\":[]},"
			+ "\"sourceProfiles\":"
			+ "[{\"profile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"TestUser\","
			+ "\"password\":\"pw\",\"keyRing\":\"k3yr1nG\",\"email\":\"e@ma.il\",\"isActivated\":"
			+ "false,\"properties\":[]},\"profileName\":\"TestProfile\",\"description\":"
			+ "\"org.backmeup.dummy\",\"sourceAndOrSink\":\"Source\"},\"options\":"
			+ "[\"folder1\",\"folder2\"]}],"
			+ "\"sinkProfile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"TestUser\""
			+ ",\"password\":\"pw\",\"keyRing\":\"pw\",\"email\":\"e@ma.il\",\"isActivated\":"
			+ "false,\"properties\":[]},\"profileName\":\"TestProfile2\",\"description\":"
			+ "\"org.backmeup.dummy\",\"sourceAndOrSink\":\"Sink\"},\"requiredActions\":[],"
			+ "\"start\":\"1345203377704\",\"delay\":1345203258212}";

	private static final String BACKUP_JOB = "{\"user\":{\"userId\":1,\"username\":\"john.doe\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"Sepp@Mail.at\",\"password\":\"John123!#\",\"activated\":false,\"protocols\":[],\"properties\":[]},\"sourceProfiles\":{\"profile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"john.doe\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"Sepp@Mail.at\",\"password\":\"John123!#\",\"activated\":false,\"protocols\":[],\"properties\":[]},\"profileName\":\"TestProfile\",\"description\":\"org.backmeup.source\",\"created\":1413382070009,\"modified\":1413382070009,\"sourceAndOrSink\":\"Source\"},\"options\":[\"folder1\",\"folder2\"]},\"jobProtocols\":[],\"sinkProfile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"john.doe\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"Sepp@Mail.at\",\"password\":\"John123!#\",\"activated\":false,\"protocols\":[],\"properties\":[]},\"profileName\":\"TestProfile2\",\"description\":\"org.backmeup.sink\",\"created\":1413382070009,\"modified\":1413382070009,\"sourceAndOrSink\":\"Sink\"},\"requiredActions\":[],\"start\":1413382070010,\"delay\":1413383070010,\"created\":1413382070010,\"modified\":1413382070010,\"jobTitle\":\"TestJob1\",\"reschedule\":false,\"onHold\":false}";
	private static final String BACKUP_JOB_FAIL = "{\"userId\":1,\"username\":\"john.doe\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"Sepp@Mail.at\",\"password\":\"John123!#\",\"activated\":false,\"protocols\":[],\"properties\":[]},\"sourceProfiles\":{\"profile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"john.doe\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"Sepp@Mail.at\",\"password\":\"John123!#\",\"activated\":false,\"protocols\":[],\"properties\":[]},\"profileName\":\"TestProfile\",\"description\":\"org.backmeup.source\",\"created\":1413382070009,\"modified\":1413382070009,\"sourceAndOrSink\":\"Source\"},\"options\":[\"folder1\",\"folder2\"]},\"jobProtocols\":[],\"sinkProfile\":{\"profileId\":2,\"user\":{\"userId\":1,\"username\":\"john.doe\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"email\":\"Sepp@Mail.at\",\"password\":\"John123!#\",\"activated\":false,\"protocols\":[],\"properties\":[]},\"profileName\":\"TestProfile2\",\"description\":\"org.backmeup.sink\",\"created\":1413382070009,\"modified\":1413382070009,\"sourceAndOrSink\":\"Sink\"},\"requiredActions\":[],\"start\":1413382070010,\"delay\":1413383070010,\"created\":1413382070010,\"modified\":1413382070010,\"jobTitle\":\"TestJob1\",\"reschedule\":false,\"onHold\":false}";

	private final Progressable logProgressable = new Progressable() {
		@Override
		public void progress(String message) {
			System.out.println("PROGRESS: " + message);
		}
	};

	@Before
	public void setup() throws ActionException {
		node = NodeBuilder.nodeBuilder().local(true)
				.clusterName(ELASTICSEARCH_CLUSTERNAME).node();

		System.out.println("Setting up test index...");
		// Dummy storage reader on the src/test/resources directory
		Storage storage = new DummyStorage();

		// Local ElasticSearch node
		Client client = node.client();

		// Index test files on the local ES index
		IndexAction action = new IndexAction(client);

		BackupJob job = JsonSerializer.deserialize(BACKUP_JOB, BackupJob.class);

		action.doAction(null, storage, job, logProgressable);
		System.out.println("Done.");
	}

	@Test
	public void deserializeBackupJob() {

		BackupJob job = JsonSerializer.deserialize(BACKUP_JOB, BackupJob.class);
		Assert.assertNotNull(
				"Object deserialization failed, probably outdated BACKUP_JOB",
				job);

		try {
			JsonSerializer.deserialize(BACKUP_JOB_old, BackupJob.class);
			Assert.fail("Object deserialization of outdated object should fail");
		} catch (JsonSyntaxException e) {
			Assert.assertTrue(true);
		}
	}

	@After
	public void tearDown() throws IOException {
		node.close();
		// the directory is backmeup-plugins/backmeup-indexing-plugin/data
		FileUtils.deleteDirectory(new File("data"));
	}

	@Ignore
	@Test
	public void verifyIndex() {
		System.out.println("Verifying indexing content");
		Client client = node.client();
		SearchResponse response = client.prepareSearch("backmeup")
				.setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
		SearchHits hits = response.getHits();
		Assert.assertEquals(3, hits.getHits().length);

		for (SearchHit hit : response.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			for (String key : source.keySet()) {
				System.out.println(key + ": " + source.get(key));

				if (key.equals("owner_name"))
					Assert.assertEquals("TestUser", source.get(key));

				if (key.equals("owner_id"))
					Assert.assertEquals(1, source.get(key));

				if (key.equals("backup_sources"))
					Assert.assertEquals("org.backmeup.dummy", source.get(key));

				if (key.equals("backup_sink"))
					Assert.assertEquals("org.backmeup.dummy", source.get(key));

				// if (key.equals("path"))
				// Assert.assertTrue(source.get(key).toString().startsWith("src"));
			}
		}

		System.out.println("Done.");
	}

	/*
	 * @Test public void verifyQuery() {
	 * System.out.println("Verifying keyword search"); Client client =
	 * node.client();
	 * 
	 * ElasticSearchIndexClient idx = new ElasticSearchIndexClient(client);
	 * SearchResponse response = idx.queryBackup(Long.valueOf(1),
	 * "creat commons");
	 * 
	 * for (SearchHit hit : response.getHits()) {
	 * System.out.println(hit.getSourceAsString()); }
	 * 
	 * List<SearchEntry> bmuSearchEntries =
	 * IndexUtils.convertSearchEntries(response); for (SearchEntry entry :
	 * bmuSearchEntries) { System.out.println("Result: " + entry.getTitle()); }
	 * Assert.assertEquals(3, bmuSearchEntries.size());
	 * 
	 * List<CountedEntry> bmuBySource = IndexUtils.getBySource(response); for
	 * (CountedEntry entry : bmuBySource) { System.out.println("From source: " +
	 * entry.getTitle() + ":  " + entry.getCount() + " results"); }
	 * Assert.assertEquals(1, bmuBySource.size());
	 * 
	 * List<CountedEntry> bmuByType = IndexUtils.getByType(response); for
	 * (CountedEntry entry : bmuByType) { System.out.println("For type: " +
	 * entry.getTitle() + ":  " + entry.getCount() + " results"); }
	 * 
	 * Assert.assertEquals(3, response.getHits().totalHits());
	 * 
	 * System.out.println("Getting results for Job 1:"); SearchResponse
	 * resultsForJob = idx.searchByJobId(1); for (SearchHit hit :
	 * resultsForJob.getHits()) { System.out.println(hit.getSourceAsString()); }
	 * 
	 * Set<FileItem> fileItems = IndexUtils.convertToFileItems(resultsForJob);
	 * for (FileItem f: fileItems) { System.out.println("item " + f.getFileId()
	 * + " - " + f.getTitle()); } }
	 */

}
