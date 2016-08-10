package models;

import java.net.URL;

/**
 * Created by brianzhao on 8/9/16.
 */
public interface Fetchable {
    public URL getTargetURL();

    public URL getSourceURL();

    public String getAnchorText();
}
