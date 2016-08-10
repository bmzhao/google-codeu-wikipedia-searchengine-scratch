import models.Constants;

import java.sql.*;

/**
 * Created by brianzhao on 8/8/16.
 */

// TODO: 8/9/16 Refactor JDBC Connection object use to: https://stackoverflow.com/questions/1531073/is-java-sql-connection-thread-safe
// TODO: 8/10/16 Probably update many usages of integer to long to scale to high crawl sizes
// TODO: 8/10/16 Would like to persist queue state in more db tables in case of crash/interrupt ?
// should yield greater performance

public class Main {


    public static void main(String[] args) {
//        BlockingQueue<Fetch> fetchQueue = new ArrayBlockingQueue<>(models.Constants.FETCH_QUEUE_LIMIT);
//        BlockingQueue<CustomDoc> parseQueue = new ArrayBlockingQueue<>(models.Constants.PARSE_QUEUE_LIMIT);
//        BlockingQueue<Fetch> linkIndexQueue = new ArrayBlockingQueue<>(models.Constants.LINK_INDEX_QUEUE_LIMIT);
//        BlockingQueue<Fetch> linkIncrementQueue = new ArrayBlockingQueue<>(models.Constants.LINK_INCREMENT_QUEUE_LIMIT);
//        BlockingQueue<URL> indexQueue = new ArrayBlockingQueue<>(models.Constants.INDEX_QUEUE_LIMIT);

        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // TODO: 8/9/16 refactor connection url to use constants
            String url = "jdbc:mysql://192.168.99.100:3306/Crawl";
            conn = DriverManager.getConnection(url, "root", "root");

            for (String createStatement: Constants.createTableStatements) {
                Statement st = conn.createStatement();
                boolean result = st.execute(createStatement);
                System.out.println(createStatement);
                if (result) {
                    throw new RuntimeException("Create statement should have no return value");
                }
            }

        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }finally {
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
