package tasks;

import java.net.URL;
import java.sql.Connection;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/9/16.
 */
public class LinkIncrementerThread extends Thread{
    private BlockingQueue<URL> linkIncrementerQueue;
    private Connection connection;

    public LinkIncrementerThread(BlockingQueue<URL> linkIncrementerQueue, Connection connection) {
        this.linkIncrementerQueue = linkIncrementerQueue;
    }

    private void incrementIncomingLinks(URL targetURL){
        //TODO increment link count in indexing db
//        lookup associated targetURL in Indexing Table
//        if doesn't exist,
//          reinsert at end of queue
//        else
//          increment incoming link count
    }


    @Override
    public void run() {
        while (true) {
            try {
                URL toLinkIncrement = linkIncrementerQueue.take();
                incrementIncomingLinks(toLinkIncrement);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
