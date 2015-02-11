package org.backmeup.plugin.api.actions.indexing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.SystemUtils;

public class TikaServerStartupHandler {

    private ExecutorService exec = Executors.newFixedThreadPool(1);

    public void startTikaServer() {

        this.exec.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Calling: Start Tika Server");
                launchProcessIsolatedTika();
                System.out.println("Started Tika Server done");
            }
        });

    }

    @Deprecated
    public void stopTikaServer() {
        System.out.println("Calling: Stop Tika Server");
        //shutdownNow() will try to cancel the already submitted tasks by interrupting the relevant threads. 
        //Note that if your tasks ignore the interruption, shutdownNow will behave exactly the same way as shutdown
        this.exec.shutdownNow();
    }

    private void launchProcessIsolatedTika() {
        Process ps;
        java.io.InputStream stderr = null;
        try {
            ps = Runtime.getRuntime().exec(
                    new String[] { "java", "-jar",
                            "src/main/resources/processisolatedtika-0.0.4-SNAPSHOT-jar-with-dependencies.jar" });

            //on windows don't read result, as nothing returns (blocking process)
            if (SystemUtils.IS_OS_LINUX) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()))) {
                    String result = "";
                    String line;
                    while ((line = in.readLine()) != null) {
                        result += line;
                    }
                    System.out.println(result);
                }
            }
            int exitVal = ps.waitFor();
            System.out.println("exit value: " + exitVal);
        } catch (IOException | InterruptedException e) {
            System.out.println("issues starting up tika server" + e.toString());
        } finally {
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
