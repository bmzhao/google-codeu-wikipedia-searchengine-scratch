package tasks;

import models.Constants;
import models.Fetch;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private String existsInLinkDBQuery =
            "SELECT COUNT(*) FROM " + Constants.DATABASE_NAME + "." + Constants.LINK_TABLE_NAME +
            " WHERE 'SourceURL' = ? AND 'TargetURL' = ?";

    private String insertQuery =
            "INSERT INTO ";

    private PreparedStatement preparedStatement;


    public LinkIndexerThread(BlockingQueue<Fetch> linkIndexQueue, BlockingQueue<URL> linkIncrementerQueue, Connection connection) {
        this.linkIndexQueue = linkIndexQueue;
        this.linkIncrementerQueue = linkIncrementerQueue;
        this.connection = connection;
        try {
            this.preparedStatement = connection.prepareStatement(existsInLinkDBQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private boolean existsInLinkDB(Fetch links) throws SQLException{
        this.preparedStatement.setString(1, links.getSourceURL().toString());
        this.preparedStatement.setString(2, links.getTargetURL().toString());
        ResultSet resultSet = this.preparedStatement.executeQuery();
        if (resultSet.next()) {
            int count = resultSet.getInt(1);
            return count > 0;
        }
        return false;
    }

    private void insertIntoLinkDB(Fetch links) {
        //TODO add fetch object to link db
    }



    @Override
    public void run() {
        while (true) {
            try {
                /**
                 * all fetch objects are guaranteed to have nonNull sourceURL and targetURL
                 */
                Fetch toLinkIndex = linkIndexQueue.take();

                if (!existsInLinkDB(toLinkIndex)) {
                    insertIntoLinkDB(toLinkIndex);
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
