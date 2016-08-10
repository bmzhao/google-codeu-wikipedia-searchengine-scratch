package tasks;

import models.Constants;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/9/16.
 */
public class LinkIncrementerThread extends Thread{
    private BlockingQueue<URL> linkIncrementerQueue;
    private Connection connection;

    // TODO: 8/9/16 refactor hardcoded column name, also duplicated in fetcherthread
    private String existsInCrawlIndexQuery =
            "SELECT COUNT(*) FROM " + Constants.DATABASE_NAME + "." + Constants.INDEX_TABLE_NAME +
                    " WHERE Url = ?";
    private PreparedStatement existsPrep;


    private String incrementLinkQuery =
            "UPDATE " + Constants.DATABASE_NAME + "." + Constants.INDEX_TABLE_NAME +
                    " SET NumInlinks = NumInlinks + 1\n" +
                    "WHERE Url = ?";
    private PreparedStatement incrementPrep;


    public LinkIncrementerThread(BlockingQueue<URL> linkIncrementerQueue, Connection connection) {
        this.linkIncrementerQueue = linkIncrementerQueue;
        this.connection = connection;
        try {
            this.existsPrep = connection.prepareStatement(existsInCrawlIndexQuery);
            this.incrementPrep = connection.prepareStatement(incrementLinkQuery);
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

    private void incrementIncomingLinks(URL targetURL) throws SQLException{
        while (!alreadyIndexed(targetURL.toString())) {
            //if the targetURL hasn't already been indexed yet, then spinwait
            // because it will eventually be by the indexer soon
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        incrementPrep.setString(1, targetURL.toString());
        int rowsAffected = incrementPrep.executeUpdate();
        if (rowsAffected != 1) {
            throw new RuntimeException(String.format("Expected to update 1 row in crawl index table," +
                    " actual num of rows affected was %d", rowsAffected));
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                URL toLinkIncrement = linkIncrementerQueue.take();
                System.out.println("LinkIncrementer got an object");
                incrementIncomingLinks(toLinkIncrement);
                System.out.println("LinkIncrementer incremented a field");
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
