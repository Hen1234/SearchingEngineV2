package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class QueryDoc implements Comparable {

    String docNO;
    HashMap<String, QueryTerm> queryTermsInDocsAndQuery;
    int length;
    double rank;
    boolean containsQueryTermInHeader;
    boolean isQueryContainEntitiy;
    ArrayList<String> locations;

    public QueryDoc(String docNO) {
        this.docNO = docNO;
        queryTermsInDocsAndQuery = new HashMap<>();
        containsQueryTermInHeader=false;
        locations = new ArrayList<>();
    }

    public boolean isContainsQueryTermInHeader() {
        return containsQueryTermInHeader;
    }

    public void setContainsQueryTermInHeader(boolean containsQueryTermInHeader) {
        this.containsQueryTermInHeader = containsQueryTermInHeader;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }



    public boolean isQueryContainEntitiy() {
        return isQueryContainEntitiy;
    }

    public void setQueryContainEntitiy(boolean queryContainEntitiy) {
        isQueryContainEntitiy = queryContainEntitiy;
    }

    public String getDocNO() {
        return docNO;
    }

    public HashMap<String, QueryTerm> getQueryTermsInDocsAndQuery() {
        return queryTermsInDocsAndQuery;
    }

    public int getLength() {
        return length;
    }

    public void setDocNO(String docNO) {
        this.docNO = docNO;
    }

    public void setQueryTermsInDocsAndQuery(HashMap<String, QueryTerm> queryTermsInDocsAndQuery) {
        this.queryTermsInDocsAndQuery = queryTermsInDocsAndQuery;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }


    @Override
    public int compareTo(Object o) {
        if (((QueryDoc)o).rank<this.rank)
            return -1;
        if (((QueryDoc)o).rank>this.rank)
            return 1;
        if (((QueryDoc)o).rank==this.rank)
            return 0;
        return 0;
    }


    @Override
    public String toString() {
        return "docNO='" + docNO + '\'' +
                ", rank=" + rank +
                '}';
    }

}
