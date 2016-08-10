package tasks;

import models.CustomDoc;
import models.Fetch;
import models.Indexable;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/9/16.
 */
public class ParserThread {
    private HashMap<String, Integer> uniqueWordsToPosition;

    private BlockingQueue<Fetch> fetchQueue;
    private BlockingQueue<CustomDoc> parseQueue;
    private BlockingQueue<Indexable> IndexingQueue;



}
