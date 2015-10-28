package org.backmeup.facebook.storage.keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.backmeup.facebook.storage.Datatype;

public enum PageInfoKey implements SerializerKey {
    
    ID("ID", Datatype.OTHER),
    ABOUT("Allgemein", Datatype.OTHER),
    AFFILIATION("Affiliation", Datatype.OTHER),
    ARTISTS_WE_LIKE("Artisten, die wir mögen", Datatype.OTHER),
    DRESSCODE("Bekleidung", Datatype.OTHER),
    AWARDS("Preise", Datatype.OTHER),
    BAND_INTERESTS("Das interessiert uns", Datatype.OTHER),
    BAND_MEMBERS("Bandmitglieder", Datatype.OTHER),
    BEST_PAGE("Beste Seite", Datatype.OTHER),
    BIO("Biographie", Datatype.OTHER),
    BIRTHDAY("Geburtstag", Datatype.OTHER),
    BOOKING_AGENT("Buchbeauftragter", Datatype.OTHER),
    BUILT("Baujahr", Datatype.OTHER),
    BUISSNESS("Kontakt", Datatype.OTHER),
    CATEGORY("Kategorie", Datatype.OTHER),
    CATEGORIES("Kategorien", Datatype.LIST),
    CHECKINS("Anzahl besucht", Datatype.NUMBER),
    COMPANY_OVERVIEW("Unternehmensübersicht", Datatype.OTHER),
    CONTACT_ADRESS("Kontaktadresse", Datatype.OTHER),
    COVER("Cover", Datatype.OTHER),
    CULINARY_TEAM("Culinary Team", Datatype.OTHER),
    CURRENT_LOCATION("Derzeitiger Standort", Datatype.OTHER),
    DESCRIPTION("Beschreibung", Datatype.OTHER),
    DESCRIPTION_HTML("Beschreibung HTML", Datatype.OTHER),
    DIRECTOR("Filmproduzent", Datatype.OTHER),
    EMAILS("EMails", Datatype.LIST),
    ENGAGEMENT("Sinn", Datatype.OTHER),
    FEATURED_VIDEO("Repräsentatives Video", Datatype.OTHER),
    FEATURES("Funktionen", Datatype.OTHER),
    FOOD_STYLES("Essensstil", Datatype.LIST),
    FOUNDED("Gegründet", Datatype.OTHER),
    GENERAINFO("Allgemeine Informationen", Datatype.OTHER),
    GENERAL_MANAGER("Manager", Datatype.OTHER),
    GENRE("Genre", Datatype.OTHER),
    GLOBAL_BRAND_PAGE_NAME("Eindeutiger Name", Datatype.OTHER),
    GLOBAL_PARENT_PAGE("Elternseite", Datatype.OTHER),
    HOMETOWN("Heimatstadt", Datatype.OTHER),
    HOURS("Öffnungszeiten", Datatype.OTHER),
    IMPRESSUM("Impressum", Datatype.OTHER),
    INFLUENCES("Beinflussungen", Datatype.OTHER),
    IS_COMMUNITY_PAGE("Gemeinschaftsseite", Datatype.OTHER),
    IS_PERMANENTLY_CLOSED("Für immer geschlossen", Datatype.OTHER),
    IS_PUBLISHED("Veröffentlicht", Datatype.OTHER),
    IS_UNCLAIMED("Ohne Beschwerde", Datatype.OTHER),
    IS_VERIFIED("Bestätigt", Datatype.OTHER),
    LIKES("Likes", Datatype.NUMBER),
    LINK("Originaler Link", Datatype.LINK),
    LOCATION("Ort", Datatype.OTHER),
    MEMBERS("Mitglieder", Datatype.OTHER),
    MISSION("Aufgabe", Datatype.OTHER),
    MPG("MPG", Datatype.OTHER),
    NAME("Name", Datatype.OTHER),
    NETWORK("Fersehnetzwerk", Datatype.OTHER),
    NEW_LIKE_COUNT("Aktualisierte Likes", Datatype.OTHER),
    PAYMENT_OPTIONS("Zahlungsmöglichkeiten", Datatype.OTHER),
    PERSONAL_INFO("Personalinformationen", Datatype.OTHER),
    PERSONAL_INTERESTS("persönliche Vorieben", Datatype.OTHER),
    PHARMA_SAFETY_INFO("Versicherung", Datatype.OTHER),
    PHONE("Telefonnummer", Datatype.OTHER),
    PICTURE("Bild", Datatype.OTHER),
    PLOT_OUTLINE("PLotoutline", Datatype.OTHER),
    PRESS_CONTACT("Pressekontakt", Datatype.OTHER),
    PRICE_RANGE("Geldspanne", Datatype.OTHER),
    PROPDUCER("Filmproduzent", Datatype.OTHER),
    PRODUCTS("Produkte", Datatype.OTHER),
    PUBLIC_TRANSIT("Öffentlicher Kontakt", Datatype.OTHER),
    RECORD_LABEL("CD Name", Datatype.OTHER),
    RELEASE_DATE("Erscheinungsdatum", Datatype.OTHER),
    RESTAURANT_SPECIALITIES("Spezialitäten", Datatype.OTHER),
    RESTAURANT_SERVICES("Service", Datatype.OTHER),
    SCHEDULE("Sendezeiten", Datatype.OTHER),
    SCREENPLAY_BY("Filmer", Datatype.OTHER),
    SEASON("Staffel", Datatype.OTHER),
    STARRING("Darsteller", Datatype.OTHER),
    START_INFO("Startinformationen", Datatype.OTHER),
    STORE_NUMBER("Speichernummer", Datatype.OTHER),
    STUDIO("Studio", Datatype.OTHER),
    TALKING_ABOUT("Anzahl Leute die die Seite besprechen", Datatype.NUMBER),
    UNREAD_MESSAGES("unglesene Nachrichten", Datatype.NUMBER),
    UNREAD_NOTIFICATIONS("unglesene Benachrichtungen", Datatype.NUMBER),
    UNSEEN_MESSAGES("noch nicht gesehene Nachrichten", Datatype.NUMBER),
    USERNAME("Benutzername", Datatype.OTHER),
    VOIP_INFO("VoIP Informationen", Datatype.OTHER),
    WEBSITE("Website", Datatype.LINK),
    WERE_HRER("Angesehen", Datatype.NUMBER),
    WRITTEN_BY("Geschrieben von", Datatype.OTHER);
    
    private String label;
    private Datatype type;

    private PageInfoKey(String label, Datatype type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public Datatype getType() {
        return type;
    }

    @Override
    public PageInfoKey[] getReduced() {
        List<PageInfoKey> ret = new ArrayList<>(Arrays.asList(values()));
        ret.remove(ID);
        return ret.toArray(new PageInfoKey[ret.size()]);
    }
}