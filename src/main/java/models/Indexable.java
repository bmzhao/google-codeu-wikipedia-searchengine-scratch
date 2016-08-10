package models;

import java.net.URL;
import java.util.List;

/**
 * Created by brianzhao on 8/9/16.
 */
public interface Indexable {
    //ideally implemented as json of hashmap where keys are numbers corresponding to words
    public String getVector();

    public URL getURL();

    public List<String> getTopWords();

    public String getSummary();

    public String getTitle();

    public int getNumIncomingLinks();
}
