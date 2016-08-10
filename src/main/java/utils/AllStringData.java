package utils;

import java.util.*;

/**
 * Created by brianzhao on 10/31/15.
 */
public class AllStringData {
    private Map<String, StringData> stringDataHashMap = new HashMap<>();

    /**
     * you should only add strings that do not already exist
     * will throw a runtime exception if you attempt to add an already existing string
     *
     * @param input
     * @return
     */
    public void addString(String input, int indexNumber, String unstemmed) {
        if (containsString(input)) {
            throw new RuntimeException("Attempted to add already existing string");
        }
        stringDataHashMap.put(input, new StringData(input, indexNumber, unstemmed));
    }

    public boolean containsString(String input) {
        return stringDataHashMap.containsKey(input);
    }


    public double getDF(String inputString) {
        if (!containsString(inputString)) {
            throw new RuntimeException("Attempted to getDf of non existing string");
        }
        return stringDataHashMap.get(inputString).getDf();
    }


    public String getUnstemmedVersion(String inputString) {
        if (!containsString(inputString)) {
            throw new RuntimeException("Attempted to lookup non existing string");
        }
        return stringDataHashMap.get(inputString).getUnstemmedWord();
    }


    public void updateDF(String inputString, double df) {
        if (!containsString(inputString)) {
            throw new RuntimeException("Attempted to updateDf of non existing string");
        }
        stringDataHashMap.get(inputString).setDf(df);
    }


    public void removeEmptyString() {
        if (stringDataHashMap.containsKey("")) {
            stringDataHashMap.remove("");
        }
    }

    public Set<String> allContainedStrings() {
        return stringDataHashMap.keySet();
    }

}
