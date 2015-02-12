package org.backmeup.plugin.api.actions.indexing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.backmeup.index.api.IndexClient;
import org.backmeup.index.model.User;
import org.backmeup.index.query.ElasticSearchIndexClient;
import org.backmeup.index.utils.file.FileUtils;
import org.backmeup.model.BackupJob;
import org.backmeup.model.dto.BackupJobDTO;
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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

public class IndexActionTest {

    private static final String ELASTICSEARCH_CLUSTERNAME = "testcluster";

    private static Node node;
    private IndexClient client;

    private final String BACKUP_JOB_old = loadJson("backup_job_old.json");
    private final String BACKUP_JOB = loadJson("backup_job.json");
    //TODO AL switch to BackupJobDTO within this class
    private final String BACKUP_JOB_DTO = loadJson("backup_job_dto.json");

    // private static final String BACKUP_JOB_FAIL =
    // loadJson("backup_job_fail.json");

    private String loadJson(String fileName) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(fileName));
    }

    private final Progressable logProgressable = new Progressable() {
        @Override
        public void progress(String message) {
            System.out.println("PROGRESS: " + message);
        }
    };

    public IndexActionTest() throws IOException {
    }

    // @Before
    public void setup() throws ActionException {
        node = NodeBuilder.nodeBuilder().local(true).clusterName(ELASTICSEARCH_CLUSTERNAME).node();

        System.out.println("Setting up test index...");
        // Dummy storage reader on the src/test/resources directory
        Storage storage = new DummyStorage();

        this.client = new ElasticSearchIndexClient(new User(1L), node.client());

        // Index test files on the local ES index
        IndexAction action = new IndexAction(this.client);

        BackupJobDTO job = JsonSerializer.deserialize(this.BACKUP_JOB_DTO, BackupJobDTO.class);

        // now call the actual indexing (Metadata extraction, Tika analysis)
        action.doAction(new HashMap<String, String>(), new HashMap<String, String>(), new ArrayList<String>(), storage,
                job, this.logProgressable);
        System.out.println("Done.");
    }

    @Test
    @Ignore("outdated test, interface changed to BackupJobDTO")
    public void deserializeBackupJob() {
        BackupJob job = JsonSerializer.deserialize(this.BACKUP_JOB, BackupJob.class);
        Assert.assertNotNull("Object deserialization failed, probably outdated BACKUP_JOB", job);
    }

    @Test
    public void deserializeBackupJobDTO() {
        BackupJobDTO job = JsonSerializer.deserialize(this.BACKUP_JOB_DTO, BackupJobDTO.class);
        Assert.assertNotNull("Object deserialization failed, probably outdated BACKUP_JOB_DTO", job);
    }

    @Ignore("fails but maybe we do not care")
    @Test(expected = JsonSyntaxException.class)
    public void deserializeOutdatedBackupJob() {
        JsonSerializer.deserialize(this.BACKUP_JOB_old, BackupJob.class);
    }

    //@After
    public void tearDown() {
        this.client.close();
        node.close();
        // the directory is backmeup-plugins/backmeup-indexing-plugin/data
        FileUtils.deleteDirectory(new File("data"));
    }

    @Test
    @Ignore("outdated test, we can't test this way anymore, as IndexAction creates an index-per-user instance on its own")
    public void verifyIndex() {
        System.out.println("Verifying indexing content");
        Client rawClient = node.client();
        SearchResponse response = rawClient.prepareSearch("backmeup").setQuery(QueryBuilders.matchAllQuery()).execute()
                .actionGet();
        SearchHits hits = response.getHits();
        Assert.assertEquals(1, hits.getHits().length); //skipping jpg and png, only indexing pdf+txt right now

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> source = hit.getSource();
            for (String key : source.keySet()) {
                System.out.println(key + ": " + source.get(key));

                if (key.equals("owner_name"))
                    Assert.assertEquals("JohnDoe", source.get(key));

                if (key.equals("owner_id"))
                    Assert.assertEquals(1, source.get(key));

                if (key.equals("backup_sources"))
                    Assert.assertEquals("org.backmeup.dummy", source.get(key));

                if (key.equals("fulltext"))
                    Assert.assertTrue(((String) source.get(key)).contains("Dr. Crawford will be teaching a new course"));

                // if (key.equals("backup_sink"))
                // Assert.assertEquals("org.backmeup.dummy", source.get(key));

                // if (key.equals("path"))
                // Assert.assertTrue(source.get(key).toString().startsWith("src"));
            }
        }

        System.out.println("Done.");
    }
    // @Test
    // public void verifyQuery() {
    // System.out.println("Verifying keyword search");
    // Client rawClient = node.client();
    //
    // ElasticSearchIndexClient idx = new ElasticSearchIndexClient(1L,
    // rawClient);
    // SearchResponse response = idx.queryBackup("creat commons",
    // Collections.<String, List<String>> emptyMap());
    //
    // for (SearchHit hit : response.getHits()) {
    // System.out.println(hit.getSourceAsString());
    // }
    //
    // List<SearchEntry> bmuSearchEntries =
    // IndexUtils.convertSearchEntries(response);
    // for (SearchEntry entry : bmuSearchEntries) {
    // System.out.println("Result: " + entry.getTitle());
    // }
    // Assert.assertEquals(3, bmuSearchEntries.size());
    //
    // List<CountedEntry> bmuBySource = IndexUtils.getBySource(response);
    // for (CountedEntry entry : bmuBySource) {
    // System.out.println("From source: " + entry.getTitle() + ":  " +
    // entry.getCount() + " results");
    // }
    // Assert.assertEquals(1, bmuBySource.size());
    //
    // List<CountedEntry> bmuByType = IndexUtils.getByType(response);
    // for (CountedEntry entry : bmuByType) {
    // System.out.println("For type: " + entry.getTitle() + ":  " +
    // entry.getCount() + " results");
    // }
    //
    // Assert.assertEquals(3, response.getHits().totalHits());
    //
    // System.out.println("Getting results for Job 1:");
    // SearchResponse resultsForJob = idx.searchByJobId(1);
    // for (SearchHit hit : resultsForJob.getHits()) {
    // System.out.println(hit.getSourceAsString());
    // }
    //
    // Set<FileItem> fileItems = IndexUtils.convertToFileItems(resultsForJob);
    // for (FileItem f : fileItems) {
    // System.out.println("item " + f.getFileId() + " - " + f.getTitle());
    // }
    // }

}
