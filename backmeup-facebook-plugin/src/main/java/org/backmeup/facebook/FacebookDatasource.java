package org.backmeup.facebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.backmeup.facebook.files.ConfLoader;
import org.backmeup.facebook.files.PropertyOption;
import org.backmeup.facebook.htmlgenerator.HTMLGenerator;
import org.backmeup.facebook.storage.Serializer;
import org.backmeup.facebook.utils.FileUtils;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.plugin.api.Datasource;
import org.backmeup.plugin.api.DatasourceException;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import com.restfb.experimental.api.Facebook;

public class FacebookDatasource implements Datasource {
    /**
     * 
     * @deprecated use standalone version instead
     * @param args
     *            arguments
     */
    @Deprecated
    public static void main(String[] args) {
        FacebookClient fbc;
        Facebook facebook;
        Long maxPics;
        ArrayList<String> skipAlbums;
        File dir;
        HTMLGenerator mainGen;
        String path;
        File target;
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (!ConfLoader.confExists())
            ConfLoader.genProperties();
        Properties props = ConfLoader.getProperties();
        path = props.getProperty(PropertyOption.DIRECTORY.toString());
        if (!ConfLoader.confExists())
            ConfLoader.genProperties();
        if (arguments.contains("--download")) {
            String CURRENT_ACCESSTOKEN = ConfLoader.getProperties().getProperty(PropertyOption.ACCESS_TOKEN.toString());
            maxPics = (long) -1;
            try {
                maxPics = Long.parseLong(ConfLoader.getProperties().getProperty(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString()));
            } catch (NumberFormatException e) {

            }
            skipAlbums = new ArrayList<>();
            skipAlbums.addAll(Arrays.asList(ConfLoader.getProperties().getProperty(PropertyOption.SKIP_ALBUMS.toString()).split(";")));
            fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
            facebook = new Facebook(fbc);
            dir = new File(ConfLoader.getProperties().getProperty(PropertyOption.DIRECTORY.toString()));
            Serializer.generateAll(fbc, facebook, dir, skipAlbums, maxPics, null);
        }
        if (arguments.contains("--generate-html")) {
            mainGen = new HTMLGenerator();
            target = new File(props.getProperty(PropertyOption.HTML_DIR.toString()));
            if (!target.exists())
                target.mkdirs();
            try {
                FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/main.css", new File("" + target + "/main.css"), HTMLGenerator.class);
                FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/menu.css", new File("" + target + "/menu.css"), HTMLGenerator.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mainGen.genOverview(target, new File(path));
        }
    }

    @Override
    public void downloadAll(PluginProfileDTO pluginProfile, PluginContext pluginContext, Storage storage, Progressable progressor)
            throws DatasourceException, StorageException {
        FacebookClient fbc;
        Facebook facebook;
        Long maxPics;
        ArrayList<String> skipAlbums;
        File dir;
        HTMLGenerator mainGen;
        File target;
        
        // TODO: check if we use the right properties
        Map<String, String> properties = pluginProfile.getProperties();
        if (!ConfLoader.confExists() && properties != null)
            ConfLoader.genProperties();
        Properties props;
        if (!ConfLoader.confExists())
            ConfLoader.genProperties();
        if (properties == null)
            props = ConfLoader.getProperties();
        else {
            props = new Properties();
            props.putAll(properties);
        }

        /*
         * if (options.contains("--download")) {
         */
        String CURRENT_ACCESSTOKEN = props.getProperty(PropertyOption.ACCESS_TOKEN.toString());
        maxPics = (long) -1;
        try {
            maxPics = Long.parseLong(props.getProperty(PropertyOption.MAX_PHOTOS_PER_ALBUM.toString()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        skipAlbums = new ArrayList<>();
        skipAlbums.addAll(Arrays.asList(props.getProperty(PropertyOption.SKIP_ALBUMS.toString()).split(";")));
        fbc = new DefaultFacebookClient(CURRENT_ACCESSTOKEN, Version.VERSION_2_3);
        facebook = new Facebook(fbc);
        String tDir = System.getProperty("java.io.tmpdir");
        dir = new File(tDir + "/facebook_" + System.currentTimeMillis() + "/xmldata/.core.xml");
        Serializer.generateAll(fbc, facebook, dir, skipAlbums, maxPics, progressor);
        /*
         * } if (options.contains("--generate-html")) {
         */
        mainGen = new HTMLGenerator();
        target = new File(tDir + "/html");
        if (!target.exists())
            target.mkdirs();

        try {
            FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/main.css", new File("" + target + "/main.css"), HTMLGenerator.class);
            FileUtils.exctractFromJar("/org/backmeup/facebook/htmlgenerator/css/menu.css", new File("" + target + "/menu.css"), HTMLGenerator.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mainGen.genOverview(target, dir);
        // }
        ArrayList<File> allXmlFiles = new ArrayList<>();
        files(dir.getParentFile(), allXmlFiles);
        ArrayList<File> allHtmlFiles = new ArrayList<>();
        files(target, allHtmlFiles);
        for (File file : allXmlFiles)
            registerFile(dir.getParentFile(), file, storage);
        for (File file : allHtmlFiles)
            registerFile(target, file, storage);
        try (FileInputStream fishtml = new FileInputStream(target); FileInputStream fisxml = new FileInputStream(dir.getParentFile())) {
            storage.addFile(fisxml, "xmldata", new MetainfoContainer());
            storage.addFile(fishtml, "html", new MetainfoContainer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void files(File root, ArrayList<File> list) {
        if (root == null || list == null)
            return;
        if (root.isDirectory())
            for (File file : root.listFiles())
                if (file.isDirectory())
                    files(file, list);
                else
                    list.add(file);
    }

    public static void registerFile(File root, File file, Storage storage) {
        try (FileInputStream fis = new FileInputStream(file)) {
            String path = FileUtils.getWayTo(root.getParentFile(), file);
            storage.addFile(fis, path.substring(2, path.length() - 1), new MetainfoContainer());
        } catch (IOException | StorageException e) {
            e.printStackTrace();
        }
    }
}
