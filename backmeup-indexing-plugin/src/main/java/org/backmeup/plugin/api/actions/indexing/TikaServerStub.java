package org.backmeup.plugin.api.actions.indexing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

    private HttpResponse addPayloadAndExecuteCall(CloseableHttpClient httpclient, HttpPut httpput, DataObject dob,
            boolean useInputStreamBody) throws IOException {
        ByteArrayInputStream is = null;
        try {
            //add an InputStreamBody to httpput entity
            is = new ByteArrayInputStream(dob.getBytes());
            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
            if (useInputStreamBody) {
                //add a inputstream body
                multipartEntity.addPart("data", new InputStreamBody(is, getFilename(dob.getPath())));
                httpput.setEntity(multipartEntity.build());
            } else {
                //add a file body - as some Tika JAX-RS endpoints can't handle streaming
                File fileToUse = stream2file(is);
                FileBody data = new FileBody(fileToUse);
                multipartEntity = MultipartEntityBuilder.create();
                multipartEntity.addPart("data", data);
                httpput.setEntity(multipartEntity.build());
            }

            //execute the call
            HttpResponse response = httpclient.execute(httpput);
            return response;
        } catch (IOException e) {
            this.log.debug("Error calling Tika ", e);
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

    /**
     * Calls Apache Tika server to determine the object's Content-Type It uses Tikas mime-type detector and hands over
     * the file name to achieve better results http://localhost:9998/detect/stream
     * 
     * @param dob
     *            the object to extract data from
     * @return
     * @throws IOException
     */
    public String detectContentType(DataObject dob) throws IOException {
        HttpPut httpput = new HttpPut(SERVER_AND_PORT + "detect/stream");
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        //add content disposition header to get better Tika discovery results
        //e.g. httpput.addHeader("Content-Disposition", "attachment; filename=creative-commons.pdf");
        httpput.addHeader("Content-Disposition", "attachment; filename=" + getFilename(dob.getPath()));

        HttpResponse response = addPayloadAndExecuteCall(httpclient, httpput, dob, true);
        //check on status code
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return responseBody.toString();
        } else {
            throw new IOException("Error calling Tika for content type detection - received status code "
                    + response.getStatusLine().getStatusCode());
        }
    }

    private String getFilename(String path) {
        if (path.indexOf('/') > -1) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return path;
    }

    /**
     * Takes an InputStream an flushes it into a temp file which is deleted on exit.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    private static File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile("stream2file", ".tmp");
        FileOutputStream out = new FileOutputStream(tempFile);
        tempFile.deleteOnExit();
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        return tempFile;
    }

    /**
     * Calls Apache Tika server to extract full text from its content. from a t uses Tikas mime-type detector and hands
     * over the file name to achieve better results http://localhost:9998/tika
     * 
     * @param dob
     * @param contentType
     * @return
     */
    public String extractFullText(DataObject dob, String contentType) throws IOException {

        this.log.debug("calling Tika FullText extraction on content type: " + contentType + " for " + dob.getPath());

        HttpPut httpput = new HttpPut(SERVER_AND_PORT + "tika");
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        //we accept plain text as return value
        httpput.addHeader("Accept", "text/plain");
        if (contentType != null) {
            //e.g. httpput.addHeader("Content-Type", "application/pdf");
            httpput.addHeader("Content-Type", contentType);
        }

        HttpResponse response = addPayloadAndExecuteCall(httpclient, httpput, dob, false);

        //check on status code
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return responseBody.toString();
        } else {
            throw new IOException("received status code " + response.getStatusLine().getStatusCode());
        }

    }

    /**
     * Adds a detectContentType call before calling Tika full text extraction on object
     * 
     * @param dob
     * @return
     * @throws IOException
     */
    public String extractFullText(DataObject dob) throws IOException {
        String contentType = this.detectContentType(dob);
        return this.extractFullText(dob, contentType);
    }

    /**
     * Adds a detectContentType call before calling Tika metadata extraction on object
     * 
     * @param dob
     * @return
     * @throws IOException
     */
    public Map<String, String> extractMetaData(DataObject dob) throws IOException {
        String contentType = this.detectContentType(dob);
        return this.extractMetaData(dob, contentType);
    }

    /**
     * @param dob
     * @return
     * @throws IOException
     */
    public Map<String, String> extractMetaData(DataObject dob, String contentType) throws IOException {
        this.log.debug("calling Tika Metadata extraction on content type: " + contentType + " for " + dob.getPath());
        HttpPut httpput = new HttpPut(SERVER_AND_PORT + "meta");
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        //we accept plain text as return value (other possibilities: application/rdf+xml or application/json)
        httpput.addHeader("Accept", "text/csv");
        if (contentType != null) {
            //e.g. httpput.addHeader("Content-Type", "application/pdf");
            httpput.addHeader("Content-Type", contentType);
        }

        HttpResponse response = addPayloadAndExecuteCall(httpclient, httpput, dob, true);

        //check on status code
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            System.out.println(responseBody);
            return convertCSV2Map(responseBody);
        } else {
            throw new IOException("received status code " + response.getStatusLine().getStatusCode());
        }
    }

    private Map<String, String> convertCSV2Map(String responsebody) {
        Map<String, String> maps = new HashMap<String, String>();

        String[] lines = responsebody.split("\r\n|\r|\n");
        for (String line : lines) {
            //split only at commas which are followed by an even (or zero) number of quotes (and thus not inside quotes)
            String[] res = line.split(",(?=([^\"]|\"[^\"]*\")*$)");
            boolean keyFound = false;
            String key = "";
            String value = "";
            for (String s : res) {
                s = s.replace("\"", "");
                if (!keyFound) {
                    //first element in csv file is the key
                    key = s;
                    keyFound = true;
                } else {
                    if (value.equals("")) {
                        value += s;
                    } else {
                        value += ", " + s;
                    }

                }
            }
            maps.put(key, value);
        }
        return maps;
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
        HttpPut httpput = new HttpPut("http://localhost:9998/meta");
        //HttpPut httpput = new HttpPut("http://localhost:9998/detect/stream");
        //HttpPut httpput = new HttpPut("http://localhost:9998/rmeta");

        //httpput.addHeader("Accept", "application/rdf+xml");
        httpput.addHeader("Accept", "text/csv");
        httpput.addHeader("Content-Type", "application/pdf");
        //httpput.addHeader("Content-Type", "text/csv");

        //httpput.addHeader("Content-Type", "application/pdf");
        //httpput.addHeader("Content-Disposition", "attachment; filename=creative-commons.pdf");

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