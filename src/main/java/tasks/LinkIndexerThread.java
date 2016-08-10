package tasks;

import models.Constants;
import models.Fetch;

import java.net.URL;
import java.sql.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by brianzhao on 8/9/16.
 */
public class LinkIndexerThread extends Thread {
    private BlockingQueue<Fetch> linkIndexQueue;
    private BlockingQueue<URL> linkIncrementerQueue;
    private Connection connection;

    //TODO refactor to select exists for performance
//    private String existsInLinkDBQuery =
//            "SELECT EXISTS(SELECT * FROM Crawl.Links WHERE 'SourceURL' = ? AND 'TargetURL' = ?)";

    //TODO refactor hardcoded column names
    private String existsInLinkDBQuery =
            "SELECT COUNT(*) FROM " + Constants.DATABASE_NAME + "." + Constants.LINK_TABLE_NAME +
            " WHERE 'SourceURL' = ? AND 'TargetURL' = ?";

    // TODO: 8/9/16 refactor hardcoded column names
    private String insertQuery =
            "INSERT INTO " + Constants.DATABASE_NAME + "." + Constants.LINK_TABLE_NAME +
                    " ( SourceURL, TargetURL, AnchorText)\n" +
                    "VALUES\n" +
                    "( ?, ?, ? );";

    private PreparedStatement existsPrep;
    private PreparedStatement insertPrep;


    public LinkIndexerThread(BlockingQueue<Fetch> linkIndexQueue, BlockingQueue<URL> linkIncrementerQueue, Connection connection) {
        this.linkIndexQueue = linkIndexQueue;
        this.linkIncrementerQueue = linkIncrementerQueue;
        this.connection = connection;
        try {
            this.existsPrep = connection.prepareStatement(existsInLinkDBQuery);
            this.insertPrep = connection.prepareStatement(insertQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private boolean existsInLinkDB(Fetch links) throws SQLException{
        this.existsPrep.setString(1, links.getSourceURL().toString());
        this.existsPrep.setString(2, links.getTargetURL().toString());
        ResultSet resultSet = this.existsPrep.executeQuery();
        if (resultSet.next()) {
            int count = resultSet.getInt(1);
            return count > 0;
        }
        return false;
    }

    private void insertIntoLinkDB(Fetch links) throws SQLException {
        this.insertPrep.setString(1, links.getSourceURL().toString());
        this.insertPrep.setString(2, links.getTargetURL().toString());
        if (links.getAnchorText() != null) {
            this.insertPrep.setString(3, links.getAnchorText());
        } else {
            this.insertPrep.setNull(3, Types.VARCHAR);
        }
        int rowsAffected = insertPrep.executeUpdate();
        if (rowsAffected != 1) {
            throw new RuntimeException(String.format("Expected to insert 1 row into linkdb," +
                    " actual num of rows affected was %d", rowsAffected));
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                /**
                 * all fetch objects are guaranteed to have nonNull sourceURL and targetURL
                 */
                Fetch toLinkIndex = linkIndexQueue.take();
                System.out.println("LinkIndexer received an object");
                if (!existsInLinkDB(toLinkIndex)) {
                    insertIntoLinkDB(toLinkIndex);
                    System.out.println("LinkIndexer wrote object to linkdb");
                    linkIncrementerQueue.put(toLinkIndex.getTargetURL());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
