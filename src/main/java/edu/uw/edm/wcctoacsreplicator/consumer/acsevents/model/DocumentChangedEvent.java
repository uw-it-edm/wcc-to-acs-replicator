package edu.uw.edm.wcctoacsreplicator.consumer.acsevents.model;

/**
 * @author Maxime Deravet Date: 8/23/18
 */
public class DocumentChangedEvent {
    private DocumentChangedType type;
    private String documentId;
    private String wccId;
    private String profile;
    private long lastModifiedDate;
    public DocumentChangedEvent(){

    }
    public DocumentChangedEvent(DocumentChangedType type, String documentId, String wccId, String profile, long lastModifiedDate) {
        this.type = type;
        this.documentId = documentId;
        this.wccId = wccId;
        this.profile = profile;
        this.lastModifiedDate = lastModifiedDate;
    }


    public DocumentChangedType getType() {
        return type;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getProfile() {
        return profile;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getWccId() {
        return wccId;
    }
}
