package tasks;

import models.CustomDoc;
import models.Fetch;
import models.Indexable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import utils.AllStringData;
import utils.Stopwords;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
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
    private BlockingQueue<CustomDoc> parseQueue;
    private BlockingQueue<Indexable> indexingQueue;

    public ParserThread(BlockingQueue<Fetch> fetchQueue,
                        BlockingQueue<CustomDoc> parseQueue,
                        BlockingQueue<Indexable> indexingQueue,
                        Connection connection) {
        this.fetchQueue = fetchQueue;
        this.parseQueue = parseQueue;
        this.indexingQueue = indexingQueue;
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


    @Override
    public void run() {
        while (true) {
            try {
                CustomDoc customDoc= parseQueue.take();
                URL sourceURL = customDoc.getSourceURL();
                Document toParse = customDoc.getDocument();

                String title = toParse.title();
                String content = getContent(toParse);
                //todo parse

                List<URL> links = getLinks(toParse);
                for (URL url : links) {
                    Fetch toFetch = new Fetch(url, sourceURL, null);
                    fetchQueue.put(toFetch);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
