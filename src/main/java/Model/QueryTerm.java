package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryTerm {

    String value;
    HashMap<String, Integer> docsAndAmount; //docNo -> amountOfAppearanceInDoc
    //ArrayList<QueryDoc> DocsContainTerm;

    int df;
    /*ArrayList<String> docs;
    ArrayList<Integer> timesInDocs;*/
    int appearanceInQuery; // number of appearance of this term in query

    public QueryTerm(String value) {
        /*docs = new ArrayList<String>();
        timesInDocs = new ArrayList<Integer>();*/
        appearanceInQuery =0;
        this.value = value;
        docsAndAmount = new HashMap<String, Integer>();
        df =0;
    }

    public String getValue() {
        return value;
    }

    public HashMap<String, Integer> getDocsAndAmount() {
        return docsAndAmount;
    }

    public int getDf() {
        return df;
    }

    public int getAppearanceInQuery() {
        return appearanceInQuery;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDocsAndAmount(HashMap<String, Integer> docsAndAmount) {
        this.docsAndAmount = docsAndAmount;
    }

    public void setDf(int df) {
        this.df = df;
    }

    public void setAppearanceInQuery(int appearanceInQuery) {
        this.appearanceInQuery = appearanceInQuery;
    }
}

