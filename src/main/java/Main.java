import models.Constants;
import models.DocumentAndURL;
import models.Fetch;
import models.Indexable;
import tasks.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/8/16.
 */

// TODO: 8/9/16 Refactor JDBC Connection object use to: https://stackoverflow.com/questions/1531073/is-java-sql-connection-thread-safe
// TODO: 8/10/16 Probably update many usages of integer to long to scale to high crawl sizes
// TODO: 8/10/16 Would like to persist queue state in more db tables in case of crash/interrupt ?
// should yield greater performance

public class Main {


    public static void main(String[] args) {
        BlockingQueue<Fetch> fetchQueue = new ArrayBlockingQueue<>(models.Constants.FETCH_QUEUE_LIMIT);
        BlockingQueue<DocumentAndURL> parseQueue = new ArrayBlockingQueue<>(models.Constants.PARSE_QUEUE_LIMIT);
        BlockingQueue<Fetch> linkIndexQueue = new ArrayBlockingQueue<>(models.Constants.LINK_INDEX_QUEUE_LIMIT);
        BlockingQueue<URL> linkIncrementQueue = new ArrayBlockingQueue<>(models.Constants.LINK_INCREMENT_QUEUE_LIMIT);
        BlockingQueue<Indexable> indexQueue = new ArrayBlockingQueue<>(models.Constants.INDEX_QUEUE_LIMIT);

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // TODO: 8/9/16 refactor connection url to use constants
            String url = "jdbc:mysql://192.168.99.100:3306/Crawl";
            conn = DriverManager.getConnection(url, "root", "root");

            for (String createStatement : Constants.createTableStatements) {
                Statement st = conn.createStatement();
                boolean result = st.execute(createStatement);
                if (result) {
                    throw new RuntimeException("Create statement should have no return value");
                }
            }

            Fetch toFetch = new Fetch(new URL("https://en.wikipedia.org/wiki/Cosine_similarity"),
                    null, null);
            fetchQueue.put(toFetch);

            for (int i = 0; i < Constants.NUM_FETCH_WORKERS; i++) {
                FetcherThread fetcherThread = new FetcherThread(fetchQueue,
                        parseQueue, linkIndexQueue, conn);
                fetcherThread.start();
            }

            for (int i = 0; i < Constants.NUM_PARSE_WORKERS; i++) {
                ParserThread parserThread = new ParserThread(fetchQueue, parseQueue,
                        indexQueue, conn);
                parserThread.start();
            }

            for (int i = 0; i < Constants.LINK_INDEX_WORKERS; i++) {
                LinkIndexerThread linkIndexerThread = new LinkIndexerThread(
                        linkIndexQueue, linkIncrementQueue, conn
                );
                linkIndexerThread.start();
            }

            for (int i = 0; i < Constants.LINK_INCREMENT_QUEUE_WORKERS; i++) {
                LinkIncrementerThread linkIndexerThread = new LinkIncrementerThread(
                        linkIncrementQueue, conn
                );
                linkIndexerThread.start();
            }


            for (int i = 0; i < Constants.INDEX_QUEUE_WORKERS; i++) {
                IndexerThread indexerThread = new IndexerThread(indexQueue,conn);
                indexerThread.start();
            }

            // TODO: 8/10/16 make this less hacky
            while (true) {

            }
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
