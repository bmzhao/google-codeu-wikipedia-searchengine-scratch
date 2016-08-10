package utils;


public class StringData {
    private final String word;
    private final String unstemmedWord;
    private double df = 1;
    private final int indexNumber;


    public StringData(String word, int indexNumber, String unstemmedWord) {
        this.word = word;
        this.indexNumber = indexNumber;
        this.unstemmedWord = unstemmedWord;
    }

    public double getDf() {
        return df;
    }

    public void setDf(double df) {
        this.df = df;
    }

    public String getWord() {
        return word;
    }

    public String getUnstemmedWord() {
        return unstemmedWord;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringData that = (StringData) o;

        return word.equals(that.word);
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public String toString() {
        return "StringData{" +
                "word='" + word + '\'' +
                ", unstemmedWord='" + unstemmedWord + '\'' +
                ", df=" + df +
                ", indexNumber=" + indexNumber +
                '}';
    }
}
