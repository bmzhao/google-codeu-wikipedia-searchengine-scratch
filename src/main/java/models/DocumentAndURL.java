package models;

import org.jsoup.nodes.Document;

import java.net.URL;

/**
 * Created by brianzhao on 8/8/16.
 */
public class DocumentAndURL {
    private final URL sourceURL;
    private final Document document;

    public DocumentAndURL(URL sourceURL, Document document) {
        this.sourceURL = sourceURL;
        this.document = document;
    }

    public URL getSourceURL() {
        return sourceURL;
    }

    public Document getDocument() {
        return document;
    }

}
