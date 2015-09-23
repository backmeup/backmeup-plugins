package org.backmeup.plugin.api.actions.indexing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.backmeup.index.api.IndexFields;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.util.GeoMetadataConverter;

public class StandardizedMetadataExtractor {

    private String locationName = null;
    private String city = null;
    private String country = null;
    private String author = null;
    private Double latitude = null;
    private Double longitude = null;
    private Date creationDate = null;

    /**
     * checks what metadata we've received from the plugins, tries to extract and normalize it and enriches it with the
     * information coming from apache tika
     * 
     * @param tikaMetadata
     * @param pluginMetadataContainer
     * @param document
     */
    public StandardizedMetadataExtractor(Map<String, String> tikaMetadata, MetainfoContainer pluginMetadataContainer) {
        extractLocationName(pluginMetadataContainer, tikaMetadata);
        extractCountry(pluginMetadataContainer, tikaMetadata);
        extractLocationCity(pluginMetadataContainer, tikaMetadata);
        extractLocationLatitude(pluginMetadataContainer, tikaMetadata);
        extractLocationLongitude(pluginMetadataContainer, tikaMetadata);
        extractAuthorName(pluginMetadataContainer, tikaMetadata);
        extractDocumentCreationDate(pluginMetadataContainer, tikaMetadata);
    }

    private void extractLocationName(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            //only if it has not yet been set
            if ((this.locationName == null) && (metainfo.getLocationName() != null)) {
                this.locationName = metainfo.getLocationName();
            }
        }
    }

    private void extractCountry(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            //only if it has not yet been set
            if ((this.country == null) && (metainfo.getLocationCountry() != null)) {
                this.country = metainfo.getLocationCountry();
            }
        }
    }

    private void extractLocationCity(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            //only if it has not yet been set
            if ((this.city == null) && (metainfo.getLocationCity() != null)) {
                this.city = metainfo.getLocationCity();
            }
        }
    }

    private void extractLocationLatitude(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        boolean bMetaInfoContainsLat = false;
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        //1. iterate over all metainfo records an check if a latitude was provided
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            if ((metainfo.getLocationLatitude() != null) && (metainfo.getLocationLatitude() != -1D)) {
                bMetaInfoContainsLat = true;
                this.latitude = metainfo.getLocationLatitude();
            }
        }

        //2. if not try to extract it from tika metadata
        if (!bMetaInfoContainsLat) {
            if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "geo:lat")) {
                //in this case try to get the value from tika
                this.latitude = Double.valueOf(tikaMetadata.get(IndexFields.TIKA_FIELDS_PREFIX + "geo:lat"));
            } else if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "GPS Latitude")) {
                //in this case try to get the value from tika and parse it ourselves 
                try {
                    this.latitude = GeoMetadataConverter.extractAndConvertGeoCoordinates(tikaMetadata
                            .get(IndexFields.TIKA_FIELDS_PREFIX + "GPS Latitude"));
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    private void extractLocationLongitude(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        boolean bMetaInfoContainsLon = false;
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        //1. iterate over all metainfo records an check if a latitude was provided
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            if ((metainfo.getLocationLongitude() != null) && (metainfo.getLocationLongitude() != -1D)) {
                bMetaInfoContainsLon = true;
                this.longitude = metainfo.getLocationLongitude();
            }
        }

        //2. if not try to extract it from tika metadata
        if (!bMetaInfoContainsLon) {
            if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "geo:long")) {
                //in this case try to get the value from tika
                this.longitude = Double.valueOf(tikaMetadata.get(IndexFields.TIKA_FIELDS_PREFIX + "geo:long"));
            } else if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "GPS Longitude")) {
                //in this case try to get the value from tika and parse it ourselves 
                try {
                    this.longitude = GeoMetadataConverter.extractAndConvertGeoCoordinates(tikaMetadata
                            .get(IndexFields.TIKA_FIELDS_PREFIX + "GPS Longitude"));
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }

    private void extractAuthorName(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        boolean bMetaInfoContainsAuthor = false;
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        //1. check the metainfo records for author information
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            //only if it has not yet been set
            if ((this.author == null) && (metainfo.getAuthorName() != null)) {
                bMetaInfoContainsAuthor = true;
                this.author = metainfo.getAuthorName();
            }
        }

        //2. try to get the author information from tika
        if (!bMetaInfoContainsAuthor) {
            if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "dc:creator")) {
                this.author = tikaMetadata.get(IndexFields.TIKA_FIELDS_PREFIX + "dc:creator");
            }
        }
    }

    private void extractDocumentCreationDate(MetainfoContainer pluginMetadataContainer, Map<String, String> tikaMetadata) {
        boolean bMetaInfoContainsCreationDate = false;
        Iterator<Metainfo> it = pluginMetadataContainer.iterator();
        //1. check the metainfo records for document creation information
        while (it.hasNext()) {
            Metainfo metainfo = it.next();
            //only if it has not yet been set
            if ((this.creationDate == null) && (metainfo.getCreated() != null)) {
                bMetaInfoContainsCreationDate = true;
                this.creationDate = metainfo.getCreated();
            }
        }

        //2. try to get the creation information from tika
        if (!bMetaInfoContainsCreationDate) {
            if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "meta:creation-date")) {
                //format is something like "2015-08-23T13:15:50"
                try {
                    String dateFormat = "YYYY-MM-DD'T'HH:mm:ss";
                    final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                    this.creationDate = formatter.parse(tikaMetadata.get(IndexFields.TIKA_FIELDS_PREFIX
                            + "meta:creation-date"));
                } catch (ParseException e) {
                }
            } else if (tikaMetadata.containsKey(IndexFields.TIKA_FIELDS_PREFIX + "Creation-Date")) {
                //format is something like "2015-08-23T13:15:50"
                try {
                    String dateFormat = "YYYY-MM-DD'T'HH:mm:ss";
                    final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                    this.creationDate = formatter.parse(tikaMetadata.get(IndexFields.TIKA_FIELDS_PREFIX
                            + "Creation-Date"));
                } catch (ParseException e) {
                }
            }
        }
    }

    public String getCountry() {
        return this.country;
    }

    public String getAuthor() {
        return this.author;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public String getCity() {
        return this.locationName;
    }

}
