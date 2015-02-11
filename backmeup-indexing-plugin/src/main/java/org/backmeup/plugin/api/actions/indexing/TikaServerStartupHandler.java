package org.backmeup.plugin.api.actions.indexing;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TikaServerStartupHandler {

    private ExecutorService exec = Executors.newFixedThreadPool(1);

    public void startTikaServer() {

        this.exec.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Calling: Start Tika Server");
                launchProcessIsolatedTika();
            }
        });

    }

    public void stopTikaServer() {
        this.exec.shutdown();
        System.out.println("Calling: Stop Tika Server");
    }

    private void launchProcessIsolatedTika() {
        Process ps;
        try {
            ps = Runtime.getRuntime().exec(
                    new String[] { "java", "-jar",
                            "src/main/resources/processisolatedtika-0.0.4-SNAPSHOT-jar-with-dependencies.jar" });
            //ps.waitFor();
            java.io.InputStream is = ps.getInputStream();
            byte b[] = new byte[is.available()];
            is.read(b, 0, b.length);
            System.out.println(new String(b));
        } catch (IOException e) {
            System.out.println("issues starting up tika server" + e.toString());
        }
    }
}
