package tasks;

import models.CustomDoc;
import models.Fetch;
import models.Indexable;
import utils.AllStringData;

import java.sql.Connection;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/9/16.
 */
public class ParserThread extends Thread{
    private AllStringData allStringData;
    private int totalDocs = 0;

    private BlockingQueue<Fetch> fetchQueue;
    private BlockingQueue<CustomDoc> parseQueue;
    private BlockingQueue<Indexable> indexingQueue;
    private Connection connection;

    public ParserThread(BlockingQueue<Fetch> fetchQueue,
                        BlockingQueue<CustomDoc> parseQueue,
                        BlockingQueue<Indexable> indexingQueue,
                        Connection connection) {
        this.fetchQueue = fetchQueue;
        this.parseQueue = parseQueue;
        this.indexingQueue = indexingQueue;
        this.connection = connection;
    }

    @Override
    public void run() {
        while (true) {
            //// TODO: 8/10/16 implement run method
        }
    }
}
