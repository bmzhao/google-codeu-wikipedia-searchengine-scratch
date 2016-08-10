package tasks;

import models.CustomDoc;
import models.Fetch;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/8/16.
 */

/**
 * Thread that will fetch links and index links into Link Table
 */
public class FetcherThread extends Thread {
    private BlockingQueue<Fetch> fetchQueue;
    private BlockingQueue<Fetch> linkIndexingQueue;
    private BlockingQueue<CustomDoc> parseQueue;

    public FetcherThread(BlockingQueue<Fetch> fetchQueue, BlockingQueue<CustomDoc> parseQueue, BlockingQueue<Fetch> linkIndexingQueue) {
        this.fetchQueue = fetchQueue;
        this.parseQueue = parseQueue;
        this.linkIndexingQueue = linkIndexingQueue;
    }

    private boolean alreadyIndexed(String URL) {
        //TODO query index table for target url, if count(rows) > 1 return true
        return false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                /**
                 * take an object to fetch, and get its components
                 */
                Fetch toFetch = fetchQueue.take();
                URL sourceURL = toFetch.getSourceURL();
                String anchorText = toFetch.getAnchorText();
                URL targetURL = toFetch.getTargetURL();

                /**
                 * if already indexed, send the link on to link indexer
                 */
                if (alreadyIndexed(targetURL.toString())) {
                    linkIndexingQueue.put(toFetch);
                    continue;
                } else {
                    Connection conn = Jsoup.connect(targetURL.toString());
                    Document document = conn.get();
                    parseQueue.put(new CustomDoc(toFetch.getTargetURL(),document));
                    /**
                     * if sourceURL exists, then index the link
                     */
                    if (sourceURL != null) {
                        linkIndexingQueue.put(toFetch);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
