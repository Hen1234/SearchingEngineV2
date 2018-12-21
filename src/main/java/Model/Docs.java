package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Docs implements Serializable {
    // Doc Serial
    private String DocNo;
    // Doc city between tags
    private String city;
    // max ft
    private int maxft;
    // number of unique words in docs
    private int uniqueWords;
    // languages in doc
    private String language;
    private int docLength;

    public void setDocLength(int docLength) {
        this.docLength = docLength;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDocLength() {
        return docLength;
    }

    // date of the doc
    private String date;
    // PriorityQueue for the 5 most frequency essences
    private PriorityQueue<TermsPerDoc> mostFiveFrequencyEssences ;
    // writer
    private String writer;


    //C'TORS WITH DIFFERENT PARAMETERS
    public Docs(String docNo, String city, String date) {
        DocNo = docNo;
        this.city = city;
        this.date = date;
        this.writer = "";
        docLength =0;
        maxft = 1;
        uniqueWords = 0;
        language = "";
        mostFiveFrequencyEssences= new PriorityQueue<>(5);

    }
    public Docs() {
        DocNo = "";
        city = "";
        mostFiveFrequencyEssences= new PriorityQueue<>(5);
    }
    public Docs(String docNo, String city) {
        DocNo = docNo;
        this.city = city;
        maxft = 1;
        uniqueWords = 0;
        mostFiveFrequencyEssences= new PriorityQueue<>(5);
    }
    public Docs(String docNo) {

        DocNo = docNo;
        mostFiveFrequencyEssences= new PriorityQueue<>(5);
    }
    public Docs(String docNo, ArrayList<String> text) {
        DocNo = docNo;
        maxft = 0;
        uniqueWords = 0;
        mostFiveFrequencyEssences= new PriorityQueue<>(5);

        //  Text = text;
    }
    // getter
    public String getDate() {
        return date;
    }
    //setter
    public void setLanguage(String language) {
        this.language = language;
    }

    //getter
    public PriorityQueue<TermsPerDoc> getMostFiveFrequencyEssences() {
        return mostFiveFrequencyEssences;
    }

    // getter
    public String getLanguage() {
        return language;
    }
    @Override
    public int hashCode() {
        return DocNo.hashCode();
    }
    // to string of doc
    @Override
    public String toString() {
        //return "docNo = "+this.getDocNo()+" city = "+this.city+" maxft = "+this.maxft+" uniqueWords = "+this.uniqueWords;
        return this.DocNo;
    }
    // getter
    public int getMaxft() {
        return maxft;
    }
    // getter
    public int getUniqueWords() {
        return uniqueWords;
    }
    //setter
    public void setMaxft(int maxft) {
        this.maxft = maxft;
    }
    //setter
    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords = uniqueWords;
    }
    // setter
    public void setDocNo(String docNo) {
        DocNo = docNo;
    }
    //setter
    public void setCity(String city) {
        this.city = city;
    }
    //getter
    public String getDocNo() {
        return DocNo;
    }
    //getter
    public String getCity() {
        return city;
    }
    @Override
    public boolean equals(Object o) {

        Docs docs = (Docs) o;
        return ((Docs) o).DocNo.equals(this.DocNo);
    }

}
