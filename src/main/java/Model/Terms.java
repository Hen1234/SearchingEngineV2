package Model;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The class represents the Term object which represent a token after
 * the various rules have been applied to it
 */
public class Terms implements Comparable<Terms> {
    HashMap<Docs,Pair<Integer, StringBuilder>> docsAndAmount;  //doc->amount, locations
    StringBuilder docsAndAm;
    String value;
    int df; // number of documents the term appears
    int totalInCorpus;


    /**
     * Constructor- initialize the fields of the class
     * @param value- the String value of the Term
     */
    public Terms(String value) {
        this.value = (value);
        docsAndAmount = new HashMap<Docs, Pair<Integer, StringBuilder>>();
        df = 0;
    }

    /**
     * Constructor- initialize the fields of the class
     * @param value- the String value of the Term
     * @param hash- the HashMap of per doc and it's tf and locations for the Term
     */
    public Terms(String value , HashMap<Docs,Pair<Integer, StringBuilder>> hash) {
        this.value = (value);
        docsAndAmount = hash;
        df = 0;
        totalInCorpus =0;
    }

    /**
     * Setter- for the HashMap of docs and it's tf and locations for the Term
     * @param docsAndAmount
     */
    public void setDocsAndAmount(HashMap<Docs,Pair<Integer, StringBuilder>> docsAndAmount) {
        this.docsAndAmount = docsAndAmount;
    }

    /**
     * Getter
     * @return the field of "totalInCorpus" of the Term
     */
    public int getTotalInCorpus() {
        return totalInCorpus;
    }

    /**
     * Setter
     * @param value- the String value of the Term
     */
    public void setValue(String value) {
        this.value = (value);
    }

    /**
     * Setter
     * @param df- the document frequency of the Term
     */
    public void setDf(int df) {
        this.df = df;
    }

    /**
     * Getter
     * @return the HashMap of docs and it's tf and locations for the Term
     */
    public HashMap<Docs,Pair<Integer, StringBuilder>> getDocsAndAmount() {
        return docsAndAmount;
    }

    /**
     * Getter
     * @return the String value of the Term
     */
    public String getValue() {
        return value;
    }

    /**
     * Getter
     * @return the document frequency of the Term
     */
    public int getDf() {
        return df;
    }


    /**
     * Override- toString method for the String value of the Term
     * @return
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Override- the method of compare between two Object
     * @param o
     * @return
     */
    @Override
    public int compareTo(Terms o) {

        if(this.df > o.df )
            return 1;
        if(this.df==o.df)return 0;
        return -1;
    }

    /**
     * Override- the "equals" method of Object
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        return ((Terms)o).value.equals(this.value);
    }

    /**
     * Override- the "hashCode" method of Object
     * @return
     */
    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
