package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.backmeup.plugin.api.storage.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction for the underlying Apache Tika operations for Content-Type, Metadata and Fulltext extraction Apache Tika
 * is started as ProcessIsolatedTika in server mode. This class expects Tika to be available in server mode via JAX-RS
 * web service endpoints at localhost:9998 If a call to tika-server takes longer than 10 seconds (default) the
 * tika-server is restarted. A crash of the tika-server should not take out the controlling JVM.
 * 
 * @see https://github.com/willp-bl/ProcessIsolatedTika
 *
 */
public class TikaServerStub {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String SERVER_AND_PORT = "http://localhost:9998/";

    public TikaServerStub() {
        if (!isTikaAlive()) {
            //startupServer();
        }
    }

    private void startupServer() {
        Process ps;
        try {
            //TODO add only for testing? Start processIsolatedTika in new Thread
            ps = Runtime.getRuntime().exec(
                    new String[] { "java", "-jar",
                            "src/main/resources/processisolatedtika-0.0.4-SNAPSHOT-jar-with-dependencies.jar" });
            ps.waitFor();
            java.io.InputStream is = ps.getInputStream();
            byte b[] = new byte[is.available()];
            is.read(b, 0, b.length);
            System.out.println(new String(b));
        } catch (IOException | InterruptedException e) {
            this.log.debug("issues starting up tika server" + e.toString());
        }
    }

    public boolean isTikaAlive() {

        HttpGet httpget = new HttpGet(SERVER_AND_PORT + "tika");

        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            String responseBody = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            this.log.debug(e.toString());
            return false;
        }

    }

    /**
     * Calls Apache Tika to determine the object's Content-Type It uses Tikas mime-type detector and hands over the file
     * name to achieve better results http://localhost:9998/detect/stream
     * 
     * @return null if problems detecting
     */
    public String detectContentType(DataObject dob) throws IOException {
        HttpPut httpput = new HttpPut(SERVER_AND_PORT + "detect/stream");
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        //add content disposition header to get better Tika discovery results
        //e.g. httpput.addHeader("Content-Disposition", "attachment; filename=creative-commons.pdf");
        httpput.addHeader("Content-Disposition", "attachment; filename=" + getFilename(dob.getPath()));

        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(dob.getBytes());
            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
            multipartEntity.addPart("data", new InputStreamBody(is, getFilename(dob.getPath())));
            httpput.setEntity(multipartEntity.build());

            HttpResponse response = httpclient.execute(httpput);
            //check on status code
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                return responseBody.toString();
            } else {
                throw new IOException("received status code " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            this.log.debug("Error calling Tika for content type detection", e);
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private String getFilename(String path) {
        if (path.indexOf('/') > -1) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

    /**
     * * @param args
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    public void extractMeta() throws ClientProtocolException, IOException {

        //HttpClient httpclient = HttpClientBuilder.create().build();
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        //HttpPut httpput = new HttpPut("http://localhost:9998/tika");
        //HttpPut httpput = new HttpPut("http://localhost:9998/meta");
        HttpPut httpput = new HttpPut("http://localhost:9998/detect/stream");
        //HttpPut httpput = new HttpPut("http://localhost:9998/rmeta");

        //httpput.addHeader("Accept", "application/rdf+xml");
        //httpput.addHeader("Accept", "text/csv");
        //httpput.addHeader("Content-Type", "application/pdf");
        //httpput.addHeader("Content-Type", "text/csv");

        //httpput.addHeader("Content-Type", "application/pdf");
        httpput.addHeader("Content-Disposition", "attachment; filename=creative-commons.pdf");

        //for docx application/vnd.openxmlformats-officedocument.wordprocessingml.document

        //File fileToUse = new File("src/test/resources/tika_analyser.pdf");
        //File fileToUse = new File("src/test/resources/creative-commons.jpg");
        File fileToUse = new File("src/test/resources/creative-commons.pdf");
        //File fileToUse = new File("src/test/resources/creative-commons.png");
        FileBody data = new FileBody(fileToUse);

        MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
        //multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        multipartEntity.addPart("data", data);
        httpput.setEntity(multipartEntity.build());

        HttpResponse response = httpclient.execute(httpput);
        System.out.println(response.getStatusLine());
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody.toString());
        System.out.println("is 200 ok?: " + response.getStatusLine().getStatusCode());
    }
}