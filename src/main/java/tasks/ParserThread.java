package tasks;

import models.DocumentAndURL;
import models.Fetch;
import models.Indexable;
import models.WikiDoc;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import utils.AllStringData;
import utils.Stemmer;
import utils.Stopwords;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/9/16.
 */

// TODO: 8/10/16 get anchortext eventually, now its always just null
public class ParserThread extends Thread{
    private AllStringData allStringData;
    private int totalDocs = 0;
    private Stopwords stopwords = new Stopwords();
    private String urlBase = "https://en.wikipedia.org/";

    private BlockingQueue<Fetch> fetchQueue;
    private BlockingQueue<DocumentAndURL> parseQueue;
    private BlockingQueue<Indexable> indexingQueue;
    private Connection connection;

    public ParserThread(BlockingQueue<Fetch> fetchQueue,
                        BlockingQueue<DocumentAndURL> parseQueue,
                        BlockingQueue<Indexable> indexingQueue,
                        Connection connection) {
        this.fetchQueue = fetchQueue;
        this.parseQueue = parseQueue;
        this.indexingQueue = indexingQueue;
        this.connection = connection;
    }


    private String getContent(Document doc) {
        Element content = doc.getElementById("mw-content-text");
        Elements paras = content.select("p");
        StringBuilder toReturn = new StringBuilder();
        for (Element element : paras) {
            List<TextNode> textNodes = element.textNodes();
            for (TextNode textNode : textNodes) {
                toReturn.append(textNode.text()).append('\n');
            }
        }
        return toReturn.toString();
    }

    private List<URL> getLinks(Document doc) {
        Elements links = doc.select("a[href]");
        List<URL> results = new ArrayList<>();
        for (Element link : links) {
            if (link.hasAttr("href")) {
                String linkString = link.attr("href");
                if (shouldAddToQueue(linkString)) {
                    try {
                        URL url = new URL(urlBase + linkString);
                        results.add(url);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.err.println("WTF! HTML Link had no href attribute.");
            }
        }
        return results;
    }

    private boolean shouldAddToQueue(String linkString) {
        linkString = linkString.toLowerCase();
        if (linkString.endsWith(".jpg") || linkString.endsWith(".jpeg")
                || linkString.endsWith(".png") || linkString.endsWith(".svg")
                || linkString.endsWith(".css") || linkString.endsWith(".js")
                || linkString.endsWith(".mp4") || linkString.endsWith(".gif")
                || linkString.endsWith(".mp3")) {
            return false;
        }
        return linkString.startsWith("/wiki");
    }

    private String stemWord(String inputString) {
        Stemmer s = new Stemmer();
        char[] word = inputString.toCharArray();
        s.add(word, word.length);
        s.stem();
        return s.toString();
    }

    @Override
    public void run() {
        while (true) {
            try {
                DocumentAndURL documentAndURL = parseQueue.take();
                URL sourceURL = documentAndURL.getSourceURL();
                Document toParse = documentAndURL.getDocument();

                String title = toParse.title();
                String content = getContent(toParse);
                String firstParagraph = getFirstParagraph(toParse);

                Map<String, Integer> currentDocTF = parseContent(content);
                WikiDoc wikiDoc = new WikiDoc(currentDocTF,
                        sourceURL, firstParagraph, title);

                List<URL> links = getLinks(toParse);
                for (URL url : links) {
                    Fetch toFetch = new Fetch(url, sourceURL, null);
                    fetchQueue.put(toFetch);
                }
                totalDocs++;

                // TODO: 8/10/16 store state of hashmap + total docs parsed in database

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
//    https://stackoverflow.com/questions/16713206/how-to-extract-texts-between-p-tags-using-jsoup
    private String getFirstParagraph(Document toParse) {
        Element element = toParse.select("p").first();
        if (element != null) {
            return element.text();
        } else {
            return null;
        }

    }

    private Map<String,Integer> parseContent(String content) {
        String[] words = content.toLowerCase().replaceAll("[^\\w ]", "").split("\\s+");
        Map<String, Integer> localTf = new HashMap<>();
        for (String currentWord : words) {
            //skip the word if it is a stopword
            if (stopwords.isStopword(currentWord)) {
                continue;
            }
            //stem the current word
            String currentWordStemmed = stemWord(currentWord);

            //if the stemmed word isn't inside the hashmap, add it w/a frequency of 1
            if (!localTf.containsKey(currentWordStemmed)) {
                localTf.put(currentWordStemmed, 1);

                //increment the word's associated document frequency as well, since this is the first time
                //the word has occurred in this timeregion
                if (!allStringData.containsString(currentWordStemmed)) {
                    int indexNumber = allStringData.size();
                    allStringData.addString(currentWordStemmed, indexNumber, currentWord);
                } else {
                    allStringData.updateDF(currentWordStemmed, allStringData.getDF(currentWordStemmed) + 1);
                }
            }
            //otherwise , increment the value in the holder hashmap
            else {
                localTf.put(currentWordStemmed, localTf.get(currentWordStemmed) + 1);
            }
        }
        localTf.remove("");
        allStringData.removeEmptyString();
        return localTf;
    }
}
