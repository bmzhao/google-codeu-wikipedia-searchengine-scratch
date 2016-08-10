package models;

import com.google.gson.Gson;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brianzhao on 8/10/16.
 */
public class WikiDoc implements Indexable {
    private Map<String, Integer> termFreq;
    private URL url;
    private List<String> allWords;
    private String firstPara;
    private String title;
    private int incomingLinks = 0;
    private static Gson gson = new Gson();


    public WikiDoc(Map<String, Integer> termFreq, URL url, String firstPara, String title) {
        this.termFreq = termFreq;
        this.url = url;
        this.firstPara = firstPara;
        this.title = title;
        this.allWords = new ArrayList<>(termFreq.keySet());
    }

    //todo serialize the hashmap using gson
    @Override
    public String getVector() {
        return gson.toJson(termFreq);
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public List<String> getTopWords() {
        return allWords;
    }

    @Override
    public String getSummary() {
        return firstPara;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getNumIncomingLinks() {
        return incomingLinks;
    }
}
