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
import org.apache.http.client.config.RequestConfig;
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
    private final String SERVER_AND_PORT = "http://localhost:9998/";
    // set the connection timeout value to 10 seconds (10000 milliseconds)
    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).setSocketTimeout(10 * 1000)
            .build();

    public boolean isTikaAlive() {

        HttpGet httpget = new HttpGet(this.SERVER_AND_PORT + "tika");

        CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);
            String responseBody = EntityUtils.toString(response.getEntity());
            httpclient.close();
            if (response.getStatusLine().getStatusCode() == 200) {
                this.log.debug("is Tika Server alive? true");
                return true;
            } else {
                this.log.debug("is Tika Server alive? false");
                return false;
            }

        } catch (IOException e) {
            this.log.debug("is Tika alive? false ", e);
            return false;
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
            }
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
            System.out.println(response.toString());
            this.log.debug(response.toString());
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
        CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
        try {
            HttpPut httpput = new HttpPut(this.SERVER_AND_PORT + "detect/stream");
            //add content disposition header to get better Tika discovery results
            //e.g. httpput.addHeader("Content-Disposition", "attachment; filename=creative-commons.pdf");
            httpput.addHeader("Content-Disposition", "attachment; filename=" + getFilename(dob.getPath()));

            HttpResponse response = addPayloadAndExecuteCall(httpclient, httpput, dob, true);
            //check on status code
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                httpclient.close();
                return responseBody.toString();
            } else {
                throw new IOException("Error calling Tika for content type detection - received status code "
                        + response.getStatusLine().getStatusCode());
            }
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
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
     * Takes an InputStream an flushes it into a temp file which is deleted on exit.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    private File stream2file(InputStream in) throws IOException {
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
        CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
        try {
            HttpPut httpput = new HttpPut(this.SERVER_AND_PORT + "tika");

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
                httpclient.close();
                return responseBody.toString();
            } else {
                throw new IOException("received status code " + response.getStatusLine().getStatusCode());
            }
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
            }
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
        String contentType = detectContentType(dob);
        return extractMetaData(dob, contentType);
    }

    /**
     * @param dob
     * @return
     * @throws IOException
     */
    public Map<String, String> extractMetaData(DataObject dob, String contentType) throws IOException {
        this.log.debug("calling Tika Metadata extraction on content type: " + contentType + " for " + dob.getPath());
        CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
        try {
            HttpPut httpput = new HttpPut(this.SERVER_AND_PORT + "meta");

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
                this.log.debug("Extracted Metadata for " + dob.getPath() + " and content-type: " + contentType
                        + " was: " + responseBody);
                httpclient.close();
                return convertCSV2Map(responseBody);
            } else {
                throw new IOException("received status code " + response.getStatusLine().getStatusCode());
            }
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
            }
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
}