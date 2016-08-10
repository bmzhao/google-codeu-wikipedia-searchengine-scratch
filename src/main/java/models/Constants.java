package models;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by brianzhao on 8/8/16.
 */
public class Constants {
    public static final int FETCH_QUEUE_LIMIT = 1000;
    public static final int PARSE_QUEUE_LIMIT = 1000;
    public static final int LINK_INDEX_QUEUE_LIMIT = 1000;

    public static final int INDEX_QUEUE_LIMIT = 1000;
    public static final int LINK_INCREMENT_QUEUE_LIMIT = 10000;

    public static final int NUM_FETCH_WORKERS = 2;
    public static final int NUM_PARSE_WORKERS = 2;
    public static final int LINK_INDEX_WORKERS = 1;
    public static final int LINK_INCREMENT_QUEUE_WORKERS = 1;
    public static final int INDEX_QUEUE_WORKERS = 1;



    public static final String DATABASE_NAME = "Crawl";

    public static final Map<String, Map<String, String>> TABLE_TO_COLUMNS = new HashMap<>();
    public static final String INDEX_TABLE_NAME = "CrawlIndex";
    public static final String LINK_TABLE_NAME = "Links";
    public static final String WORDS_TABLE_NAME = "Words";

    static {
        Map<String, String> INDEX_TYPE_MAP = new HashMap<>();
        INDEX_TYPE_MAP.put("Url", "VARCHAR(255) NOT NULL PRIMARY KEY");
        INDEX_TYPE_MAP.put("Title", "VARCHAR(255)");
        INDEX_TYPE_MAP.put("TopWords", "MEDIUMTEXT");
        INDEX_TYPE_MAP.put("NumInlinks", "INT NOT NULL");
        INDEX_TYPE_MAP.put("Vector", "MEDIUMTEXT NOT NULL");
        INDEX_TYPE_MAP.put("Summary", "MEDIUMTEXT");

        Map<String, String> LINKS_TYPE_MAP = new HashMap<>();
        LINKS_TYPE_MAP.put("SourceURL", "VARCHAR(255) NOT NULL");
        LINKS_TYPE_MAP.put("TargetURL", "VARCHAR(255) NOT NULL");
        LINKS_TYPE_MAP.put("AnchorText", "VARCHAR(255)");


        Map<String, String> WORDS_TYPE_MAP = new HashMap<>();
        WORDS_TYPE_MAP.put("Word", "VARCHAR(255) NOT NULL PRIMARY KEY");
        //num is the index of the high dimension sparse vector
        WORDS_TYPE_MAP.put("Num", "INT NOT NULL");
        WORDS_TYPE_MAP.put("DocFreq", "INT");

        TABLE_TO_COLUMNS.put(INDEX_TABLE_NAME,
                INDEX_TYPE_MAP);
        TABLE_TO_COLUMNS.put(LINK_TABLE_NAME,
                LINKS_TYPE_MAP);
        TABLE_TO_COLUMNS.put(WORDS_TABLE_NAME,
                WORDS_TYPE_MAP);
    }


    public static List<String> createTableStatements = new ArrayList<>();

    // TODO: 8/10/16 maybe add index on text column of crawlindex
    static {
        for (String tableName : TABLE_TO_COLUMNS.keySet()) {
            if (tableName.equals(LINK_TABLE_NAME)) {
                // TODO: 8/9/16 probably wanna refactor this edge case later
                createTableStatements.add(getCreateStatementCompoundPrimaryKey(tableName, Arrays.asList("SourceURL", "TargetURL")));
            }else {
                createTableStatements.add(getCreateStatement(tableName));
            }
        }
    }


    private static String getCreateStatement(String tableName) {
        return createStatementBeginning(tableName) + "\n);";
    }

    private static String createStatementBeginning(String tableName) {
        StringBuilder createStatement = new StringBuilder();
        createStatement
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append("\n(\n");

        Map<String, String> columnToType = TABLE_TO_COLUMNS.get(tableName);
        int count = 0;
        for (String column : columnToType.keySet()) {
            count++;
            createStatement
                    .append(column)
                    .append(' ')
                    .append(columnToType.get(column));
            if (count != columnToType.keySet().size()) {
                createStatement.append(",\n");
            }
        }
        return createStatement.toString();
    }

    private static String getCreateStatementCompoundPrimaryKey(String tableName, List<String> keyFields) {
        String beginning = createStatementBeginning(tableName);
        StringBuilder primaryKey = new StringBuilder(",\nPRIMARY KEY (");
        primaryKey.append(StringUtils.join(keyFields, ", "));
        primaryKey.append(')');
        return beginning + primaryKey + "\n);";
    }

}
