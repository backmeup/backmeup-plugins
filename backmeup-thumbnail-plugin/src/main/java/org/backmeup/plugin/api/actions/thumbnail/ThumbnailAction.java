package org.backmeup.plugin.api.actions.thumbnail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.backmeup.model.dto.BackupJobDTO;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailAction implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailAction.class);

    private static final String FIELD_THUMBNAIL_PATH = "thumbnail_path";

    private static final String THUMBNAIL_PATH_EXTENSION = "_thumb.jpg";
    private static final String THUMBNAIL_TEMP_DIR = "/data/thumbnails";

    private static final Integer THUMBNAIL_DIMENSIONS = 120;
    private static final Double THUMBNAIL_QUALITY = 80.0;

    private static final List<String> UNSUPPORTED_TYPES = Arrays.asList("css", "html", "xml");

    private static File TEMP_DIR;

    static {
        String path = THUMBNAIL_TEMP_DIR;
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        TEMP_DIR = new File(path);
        if (!TEMP_DIR.exists()) {
            TEMP_DIR.mkdirs();
        }
    }

    /**
     * The GraphicsMagick command we need to emulate is this:
     * 
     * gm convert -size 120x120 original.jpg -resize 120x120 +profile "*" thumbnail.jpg
     * 
     * @return the name of the thumbnail file
     */
    private String convert(File original) throws IOException, InterruptedException, IM4JavaException {

        String thumbnailPath = original.getAbsolutePath() + THUMBNAIL_PATH_EXTENSION;

        IMOperation op = new IMOperation();
        op.size(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
        op.quality(THUMBNAIL_QUALITY);
        op.resize(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
        op.p_profile("*");

        op.addImage(original.getAbsolutePath() + "[0]");
        op.addImage(thumbnailPath);

        ConvertCmd cmd = new ConvertCmd(true);
        if (SystemUtils.IS_OS_WINDOWS) {
            //TODO move the configuration into a property file
            cmd.setSearchPath("C:/Program Files/GraphicsMagick-1.3.20-Q8");
        }
        cmd.run(op);

        return thumbnailPath;
    }

    @Override
    public void doAction(Properties accessData, Properties properties, List<String> options, Storage storage,
            BackupJobDTO job, Progressable progressor) throws ActionException {

        progressor.progress("Starting thumbnail rendering");

        try {
            Iterator<DataObject> dobs = storage.getDataObjects();
            while (dobs.hasNext()) {
                DataObject dataobject = dobs.next();
                progressor.progress("Processing " + dataobject.getPath());

                // Write file to temp dir
                String tempFilename = dataobject.getPath();

                boolean supported = true;
                for (String format : UNSUPPORTED_TYPES) {
                    if (tempFilename.toLowerCase().endsWith(format))
                        supported = false;
                }

                if (supported) {
                    if (tempFilename.startsWith("/"))
                        tempFilename = tempFilename.substring(1);

                    tempFilename = System.currentTimeMillis() + "_"
                            + tempFilename.replace("/", "$").replace(" ", "_").replace("#", "_");

                    File folder = new File(TEMP_DIR, job.getJobId().toString());
                    if (!folder.exists())
                        folder.mkdirs();

                    File tempFile = new File(folder, tempFilename);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(dataobject.getBytes());
                    }

                    try {
                        // Generate thumbnails using GraphicsMagick
                        String thumbPath = convert(tempFile);
                        Metainfo meta = new Metainfo();
                        meta.setAttribute(FIELD_THUMBNAIL_PATH, thumbPath);
                        MetainfoContainer container = dataobject.getMetainfo();
                        container.addMetainfo(meta);
                        dataobject.setMetainfo(container);
                    } catch (Throwable t) {
                        LOGGER.debug("Failed to render thumbnail for: " + dataobject.getPath());
                        LOGGER.debug(t.getClass().getName() + ": " + t.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }

        progressor.progress("Thumbnail rendering complete");
    }
}
