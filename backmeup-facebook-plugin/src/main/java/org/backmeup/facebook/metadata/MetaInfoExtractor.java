package org.backmeup.facebook.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.backmeup.plugin.api.Metainfo;

/**
 * Takes the html and xml output files of the plugin and extracts structured
 * metadata which is passed along through the Themis MetaInfo data descriptor
 *
 */
public class MetaInfoExtractor {

    public Metainfo extract(File f) {
        Metainfo meta = new Metainfo();
        try {
            LocationInfos locInfos = extractLocationInformationFromHTML(f);
            if (locInfos.getLocationName() != null) {
                meta.setLocationName(locInfos.getLocationName());
            }
            if (locInfos.getCity() != null) {
                meta.setLocationCity(locInfos.getCity());
            }
            if (locInfos.getCountry() != null) {
                meta.setLocationCountry(locInfos.getCountry());
            }
            if (locInfos.getLat() != null) {
                meta.setLocationLatitude(locInfos.getLat());
            }
            if (locInfos.getLong() != null) {
                meta.setLocationLongitude(locInfos.getLong());
            }
        } catch (StackOverflowError | IOException e) {
            // ignore file
        }

        try {
            String author = extractAuthorInformationFromHTML(f);
            if (author != null) {
                meta.setAuthorName(author);
            }
        } catch (StackOverflowError | IOException e) {
            // ignore file
        }

        try {
            String creationDate = extractCreationDateInformationFromHTML(f);
            if (creationDate != null && creationDate.length() == 29) {
                SimpleDateFormat sdfmtFB = new SimpleDateFormat("'am' dd.MM.yyyy 'um' hh:mm:ss 'Uhr'");
                java.util.Date cDate = sdfmtFB.parse(creationDate);
                meta.setCreated(cDate);
            }
        } catch (StackOverflowError | IOException | ParseException e) {
            // ignore file
        }

        return meta;
    }

    private String extractAuthorInformationFromHTML(File f) throws IOException {
        return extractFieldByPatternFromHTML(f, "(?i)(<td>gepostet von</td><td>)(.+?)(</td>)");
    }

    private String extractCreationDateInformationFromHTML(File f) throws IOException {
        return extractFieldByPatternFromHTML(f, "(?i)(Veröffentlicht</td><td>)(.+?)(</td>)");
    }

    private String extractFieldByPatternFromHTML(File f, String patternString) throws IOException {
        String string = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), "UTF-8");
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(string);

        List<String> listMatches = new ArrayList<String>();

        while (matcher.find()) {
            listMatches.add(matcher.group(2));
        }
        if (listMatches.size() == 1) {
            return listMatches.get(0);
        }
        return null;
    }

    private LocationInfos extractLocationInformationFromHTML(File f) throws IOException {
        LocationInfos locInfos = new LocationInfos();
        String string = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), "UTF-8");
        Pattern pattern = Pattern.compile("(?i)(<tr><td>Ort</td><td>)(.+?)(</td>)");
        Matcher matcher = pattern.matcher(string);

        List<String> listMatches = new ArrayList<String>();

        while (matcher.find()) {
            listMatches.add(matcher.group(2));
        }

        for (String s : listMatches) {
            // distinguish Location Name and Location Geo coordinates
            if (s.startsWith("<a href=\"https://www.google.at/maps/@")) {
                // input is a string like <a
                // href="https://www.google.at/maps/@48.163243875326,16.511982714185,17z">null,
                // Vienna, null, Austria</a>
                String[] elements = s.split(",");
                if (elements.length == 6) {
                    locInfos.setLat(elements[0].substring(elements[0].indexOf('@') + 1, elements[0].length()));
                    locInfos.setLong(elements[1]);
                    locInfos.setCity(elements[3].substring(1, elements[3].length()));
                    locInfos.setCountry(elements[5].substring(1, elements[5].indexOf("</a>")));
                }
            } else {
                locInfos.setLocationName(s);
            }
        }
        return locInfos;
    }

    public static class LocationInfos {
        private String latitute;
        private String longitude;
        private String city;
        private String country;
        private String locationName;

        public String getLat() {
            return this.latitute;
        }

        public void setLat(String lat) {
            this.latitute = lat;
        }

        public String getLong() {
            return this.longitude;
        }

        public void setLong(String l) {
            this.longitude = l;
        }

        public String getCity() {
            return this.city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return this.country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getLocationName() {
            return this.locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }
    }

}
