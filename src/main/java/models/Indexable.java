package models;

import java.util.List;

/**
 * Created by brianzhao on 8/9/16.
 */
public interface Indexable {
    public String getSerializedSparseMap();

    public String getURL();

    public List<String> getMostUsedTerms();

    public String getTitle();

    public int getNumIncomingLinks();
}
