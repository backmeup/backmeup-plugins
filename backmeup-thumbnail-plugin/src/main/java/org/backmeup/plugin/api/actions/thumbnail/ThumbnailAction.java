package org.backmeup.plugin.api.actions.thumbnail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.Action;
import org.backmeup.plugin.api.ActionException;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.PluginContext;
import org.backmeup.plugin.api.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbnailAction implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailAction.class);

    private static final String FIELD_THUMBNAIL_PATH = "thumbnail_path";
    private static final String THUMBNAIL_PATH_EXTENSION = "_thumb.jpg";
    private static final Integer THUMBNAIL_DIMENSIONS = 120;
    private static final Double THUMBNAIL_QUALITY = 80.0;

    private static final List<String> UNSUPPORTED_TYPES = Arrays.asList("css", "html", "xml");

    @Override
    public void doAction(PluginProfileDTO pluginProfile, PluginContext pluginContext, Storage storage, Progressable progressor) throws ActionException {

        progressor.progress("Starting thumbnail rendering");

        File tempDir = setPluginOutputLocation(pluginContext);
        progressor.progress("plugin output directory: " + tempDir.getAbsolutePath());

        try {
            Iterator<DataObject> dobs = storage.getDataObjects();
            while (dobs.hasNext()) {
                DataObject dataobject = dobs.next();
                progressor.progress("Processing " + dataobject.getPath());

                // Create location for output file, write to workflow temp dir
                String thumbFilename = dataobject.getPath();

                if (isSupportedFileType(thumbFilename)) {
                    if (thumbFilename.startsWith("/")) {
                        thumbFilename = thumbFilename.substring(1);
                    }

                    thumbFilename = System.currentTimeMillis() + "_"
                            + thumbFilename.replace("/", "$").replace(" ", "_").replace("#", "_");
                    File thumbOutputFile = new File(tempDir, thumbFilename);
                    generateThumbnail(dataobject, thumbOutputFile, progressor);
                }
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }

        progressor.progress("Thumbnail rendering complete");
    }

    private void generateThumbnail(DataObject dataobject, File thumbOutputFile, Progressable progressor) {
        try {
            // Generate thumbnails using GraphicsMagick
            String thumbPath = convert(dataobject, thumbOutputFile.getAbsolutePath());
            Metainfo meta = new Metainfo();
            meta.setAttribute(FIELD_THUMBNAIL_PATH, thumbPath);
            MetainfoContainer container = dataobject.getMetainfo();
            container.addMetainfo(meta);
            dataobject.setMetainfo(container);
            progressor.progress("created thumbnail for object: " + thumbPath);
        } catch (Exception ex) {
            progressor.progress("skipping " + ex.toString());
            LOGGER.debug("Failed to render thumbnail for: " + dataobject.getPath());
            LOGGER.debug(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
    
    /**
     * The GraphicsMagick command we need to emulate is this:
     * gm convert -size 120x120 original.jpg -resize 120x120 +profile "*" thumbnail.jpg
     * 
     * @return the name of the thumbnail file
     */
    private String convert(DataObject dob, String pathThumbnail) throws IOException, InterruptedException,
            IM4JavaException {

        String thumbnailPath = pathThumbnail + THUMBNAIL_PATH_EXTENSION;

        IMOperation op = new IMOperation();
        op.size(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
        op.quality(THUMBNAIL_QUALITY);
        op.resize(THUMBNAIL_DIMENSIONS, THUMBNAIL_DIMENSIONS);
        op.p_profile("*");

        ByteArrayInputStream is = new ByteArrayInputStream(dob.getBytes());
        try {
            Pipe pipeIn = new Pipe(is, null);

            op.addImage("-");
            op.addImage(thumbnailPath);

            ConvertCmd cmd = new ConvertCmd(true);
            if (SystemUtils.IS_OS_WINDOWS) {
                cmd.setSearchPath("C:/Program Files/GraphicsMagick-1.3.20-Q8");
            }
            cmd.setInputProvider(pipeIn);
            LOGGER.debug("calling ImageMagickProcessor: " + cmd.toString());
            cmd.run(op);
        } catch (Exception ex) {
            throw ex;
        } finally {
            IOUtils.closeQuietly(is);
        }

        cleanupMultipageThumbnails(thumbnailPath);
        File f = new File(thumbnailPath);
        if (f.exists()) {
            return thumbnailPath;
        } else {
            throw new IOException("Was not able to generate thumbnail");
        }
    }

    /**
     * For some file formats GraphicsMagick produces one jpg for every page (e.g. pdf input). In this case the output
     * will be located in thumbnailPath.0 for page 1, thumbnailPath.1 for page2, etc.
     */
    private void cleanupMultipageThumbnails(String thumbnailPath) {
        File f = new File(thumbnailPath);
        if (f.exists()) {
            //if only one thumbnail was generated then we're already fine
            return;
        }
        File f2 = new File(thumbnailPath + ".0");
        if (f2.exists()) {
            //rename the file and return the proper thumbnailPath
            boolean success = f2.renameTo(f);
            LOGGER.debug("rename of multi-page image thumbnail success?: " + success);
        }
        int i = 1;
        boolean loop = true;
        while (loop) {
            File f3 = new File(thumbnailPath + "." + i);
            if (f3.exists()) {
                if (f3.delete()) {
                    LOGGER.debug("deleted unused multi-page image thumbnail : " + f3.getAbsolutePath());
                }
                i++;
            } else {
                loop = false;
            }
        }
    }

    private File setPluginOutputLocation(PluginContext context) {
        String path = context.getAttribute("org.backmeup.thumbnails.tmpdir", String.class);
        if (path == null) {
            path = "/data/thumbnails";
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        File tempDir = new File(path);
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                throw new PluginException("", "Unable to create directory " + tempDir);
            }
        }
        return tempDir;
    }
    
    private boolean isSupportedFileType(String fileName) {
        boolean supported = true;
        for (String format : UNSUPPORTED_TYPES) {
            if (fileName.toLowerCase().endsWith(format))
                supported = false;
        }
        return supported;
    }
}
