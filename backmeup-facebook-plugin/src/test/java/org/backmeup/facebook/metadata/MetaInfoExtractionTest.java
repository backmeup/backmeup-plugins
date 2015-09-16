package org.backmeup.facebook.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.backmeup.facebook.utils.FileWalker;
import org.backmeup.plugin.api.Metainfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MetaInfoExtractionTest {

    private File imageFBHtml;

    @Before
    public void before() throws URISyntaxException {
        this.imageFBHtml = new File(getClass().getClassLoader().getResource("1098891313473063.html").toURI());
    }

    @Test
    public void extractLocationData() {
        MetaInfoExtractor extractor = new MetaInfoExtractor();
        Metainfo metaInfo = extractor.extract(this.imageFBHtml);
        assertEquals("Unteres Donauinsel Wehr", metaInfo.getLocationName());
        assertEquals("48.163243875326", metaInfo.getLocationLatitude());
        assertEquals("16.511982714185", metaInfo.getLocationLongitude());
        assertEquals("Vienna", metaInfo.getLocationCity());
        assertEquals("Austria", metaInfo.getLocationCountry());
    }

    @Test
    public void extractAuthorData() {
        MetaInfoExtractor extractor = new MetaInfoExtractor();
        Metainfo metaInfo = extractor.extract(this.imageFBHtml);
        assertEquals("Andrew Lindley", metaInfo.getAuthorName());
    }

    @Test
    public void extractCreationDate() {
        MetaInfoExtractor extractor = new MetaInfoExtractor();
        Metainfo metaInfo = extractor.extract(this.imageFBHtml);
        assertNotNull(metaInfo.getCreated());
        assertEquals("Sun Jul 26 13:11:27 CEST 2015", metaInfo.getCreated().toString());
    }

    @Test
    @Ignore
    public void doNotCrashOnArbitraryFiles() {
        FileWalker fw = new FileWalker();
        List<File> files = fw.walk("C:/data/backmeup-storage/3/BMU_facebook_43839_29_07_2015_13_51");
        for (File f : files) {
            MetaInfoExtractor extractor = new MetaInfoExtractor();
            Metainfo metaInfo = extractor.extract(f);
        }
    }
}
