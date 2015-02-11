package org.backmeup.plugin.api.actions.indexing;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TikaServerStartupHandlerTest {

    private static TikaServerStartupHandler h = new TikaServerStartupHandler();

    @BeforeClass
    public static void before() throws InterruptedException {
        h.startTikaServer();
        Thread.sleep(5000);
    }

    @AfterClass
    public static void after() {
        h.stopTikaServer();
    }

    @Test
    public void testCheckTikaServerIsRunning() {
        boolean b = TikaServerStub.isTikaAlive();
        assertTrue("Tika Server is not responding", b);
    }

    @Test
    @Ignore("We can't bring down tika as it spins of its own process once started")
    public void testTikaShutdown() throws InterruptedException {
        boolean b = TikaServerStub.isTikaAlive();
        assertTrue("Tika Server is not responding", b);

        //now issue shutdown
        h.stopTikaServer();

        //Give Tika Server Thread time to shutdown
        int iMax = 5;
        boolean bShutdown = false;
        for (int i = 0; i < iMax; i++) {
            b = TikaServerStub.isTikaAlive();
            if (!b) {
                bShutdown = true;
                i = iMax;
            } else {
                Thread.sleep(5000);
            }
        }
        assertTrue("Tika Server did not shutdown properly", bShutdown);

    }

}
