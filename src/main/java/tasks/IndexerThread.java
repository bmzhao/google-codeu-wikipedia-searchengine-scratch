package tasks;

import models.Constants;
import models.Indexable;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/10/16.
 */
public class IndexerThread extends Thread {
    private BlockingQueue<Indexable> indexQueue;
    private Connection connection;

    // TODO: 8/10/16 refactor out hardcoded column names
    private String insertIndexQuery =
            "INSERT INTO " + Constants.DATABASE_NAME + "." + Constants.INDEX_TABLE_NAME +
                    " ( Url, Title, TopWords, NumInlinks, Vector)\n" +
                    "VALUES\n" +
                    "( ?, ?, ?, ?, ? );";

    private PreparedStatement insertPrep;

    public IndexerThread(BlockingQueue<Indexable> indexQueue, Connection connection) {
        this.indexQueue = indexQueue;
        this.connection = connection;
        try {
            this.insertPrep = connection.prepareStatement(insertIndexQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void addToCrawlIndex(Indexable indexable) throws SQLException {
        insertPrep.setString(1, indexable.getURL().toString());
        if (indexable.getTitle() != null) {
            insertPrep.setString(2, indexable.getTitle());
        } else {
            insertPrep.setNull(2, Types.VARCHAR);
        }

        if (indexable.getTopWords() != null) {
            insertPrep.setString(3, StringUtils.join(indexable.getTopWords(), ", "));
        } else {
//            https://stackoverflow.com/questions/6772594/what-is-the-java-sql-types-equivalent-for-the-mysql-text
            insertPrep.setNull(3, Types.VARCHAR);
        }

        if (indexable.getNumIncomingLinks() != 0) {
            throw new RuntimeException(String.format("Expected indexable object to have 0 incoming links, " +
                    "actually had %d", indexable.getNumIncomingLinks()));
        }
        insertPrep.setInt(4, 0);

        insertPrep.setString(5, indexable.getVector());
        int rowsAffected = insertPrep.executeUpdate();
        if (rowsAffected != 1) {
            throw new RuntimeException(String.format("Expected to update 1 row in crawl index table," +
                    " actual num of rows affected was %d", rowsAffected));
        }

    }

    // TODO: 8/10/16 should have indexer bulk insert 100 at a time instead of on each item in queue
    @Override
    public void run() {
        while (true) {
            try {
                Indexable indexable = indexQueue.take();
                addToCrawlIndex(indexable);

            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
