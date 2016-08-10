package tasks;

import models.Constants;
import models.CustomDoc;
import models.Fetch;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private java.sql.Connection connection;

    // TODO: 8/9/16 refactor hardcoded column name
    private String existsInCrawlIndexQuery =
            "SELECT COUNT(*) FROM " + Constants.DATABASE_NAME + "." + Constants.INDEX_TABLE_NAME +
                    " WHERE 'Url' = ?";

    private PreparedStatement existsPrep;

    public FetcherThread(BlockingQueue<Fetch> fetchQueue,
                         BlockingQueue<CustomDoc> parseQueue,
                         BlockingQueue<Fetch> linkIndexingQueue,
                         java.sql.Connection connection) {
        this.fetchQueue = fetchQueue;
        this.parseQueue = parseQueue;
        this.linkIndexingQueue = linkIndexingQueue;
        this.connection = connection;
        try {
            this.existsPrep = connection.prepareStatement(existsInCrawlIndexQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private boolean alreadyIndexed(String URL) throws SQLException {
        this.existsPrep.setString(1, URL.toString());
        ResultSet resultSet = this.existsPrep.executeQuery();
        if (resultSet.next()) {
            int count = resultSet.getInt(1);
            return count > 0;
        }
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
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
