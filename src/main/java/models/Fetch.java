package models;

import com.sun.istack.internal.Nullable;

import java.net.URL;

/**
 * Created by brianzhao on 8/9/16.
 */
public class Fetch implements Fetchable {
    private URL targetURL;
    private URL sourceURL;
    private String anchorText;

    public Fetch(URL targetURL, @Nullable URL sourceURL, @Nullable String anchorText) {
        this.targetURL = targetURL;
        this.sourceURL = sourceURL;
        this.anchorText = anchorText;
    }

    public URL getTargetURL() {
        return targetURL;
    }

    public URL getSourceURL() {
        return sourceURL;
    }

    public String getAnchorText() {
        return anchorText;
    }
}
