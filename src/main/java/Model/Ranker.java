package Model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

public class Ranker {

    private PriorityQueue<QueryDoc> qDocQueue;
    private int queryLength;

    /////\\
    private HashSet<String> debug;
    //\\\\\

    public Ranker() {
        qDocQueue = new PriorityQueue<QueryDoc>();
        queryLength = 0;
        debug = new HashSet<String>();
    }



    public PriorityQueue<QueryDoc> getqDocQueue() {
        return qDocQueue;
    }

    public void setqDocQueue(PriorityQueue<QueryDoc> qDocQueue) {
        this.qDocQueue = qDocQueue;
    }

    public void getQueryDocFromSearcher(QueryDoc currentQueryDoc, int queryLength) {


        //set the queryLength
        this.queryLength = queryLength;
        //iterator for the QueryTermsInTheQueryDoc
        Iterator it = currentQueryDoc.getQueryTermsInDocsAndQuery().entrySet().iterator();
        while (it.hasNext()) {
            //Terms nextTerm = (Terms) it.next();
            //text.append(nextTerm.getValue());
            Map.Entry pair = (Map.Entry) it.next();
            QueryTerm currentQueryTerm = (QueryTerm) pair.getValue();
            //System.out.println(currentQueryDoc.getDocNO()+"rank= "+currentQueryDoc.getRank());
            /*double BM25Value = BM25func(currentQueryTerm, currentQueryDoc,(double)queryLength);
            double tfIDFValue = tfIDF(currentQueryTerm, currentQueryDoc,(double)queryLength);*/
            currentQueryDoc.setRank(currentQueryDoc.getRank() + BM25func(currentQueryTerm, currentQueryDoc,(double)queryLength));
            /*+ tfIDF(currentQueryTerm, currentQueryDoc,(double)queryLength))*/;
            //System.out.println(currentQueryDoc.getDocNO()+"rank= "+currentQueryDoc.getRank());
        }
        currentQueryDoc.setRank(currentQueryDoc.getRank()*currentQueryDoc.getQueryTermsInDocsAndQuery().size());
        if(currentQueryDoc.isQueryContainEntitiy){
            currentQueryDoc.setRank(currentQueryDoc.getRank()*5);
        }
        //update the currentQueryDoc's rank by cosSim
        //currentQueryDoc.setRank(currentQueryDoc.getRank() + 0.25*cosSim(currentQueryDoc, queryLength));
        qDocQueue.add(currentQueryDoc);
    }

    private double tfIDF (QueryTerm currentQueryTerm, QueryDoc currentQueryDoc , double queryLength) {
        double cwq = currentQueryTerm.getAppearanceInQuery() /  queryLength;
        double d = currentQueryDoc.getLength();
        double cwd = currentQueryTerm.getDocsAndAmount().get(currentQueryDoc.getDocNO()) / d ; // normalization
        if (currentQueryDoc.isContainsQueryTermInHeader()){
            cwd = cwd +2;
        }

        double M = Searcher.numOfDocumentsInCorpus;
        double df = currentQueryTerm.getDf();
        if (df==0) {
            df = 1.5;
        }
        /*if(currentQueryTerm.isSynonym() && ! currentQueryDoc.isContainsQueryTermInHeader() ){
            return (cwq * cwd * Math.log10((M+1)/df)*0.5);
        }
        if(currentQueryDoc.isContainsQueryTermInHeader() && !currentQueryTerm.isSynonym()){
            return (cwq * cwd * Math.log10((M+1)/df)*5);
        }*/
        return (cwq * cwd * Math.log10((M+1)/df));

    }

    private double BM25func(QueryTerm currentQueryTerm, QueryDoc currentQueryDoc , double queryLength) {


        double cwq = currentQueryTerm.getAppearanceInQuery();
        double d = currentQueryDoc.getLength();
        double df = currentQueryTerm.getDf();
        double avdl = Searcher.avdl;
        double M = Searcher.numOfDocumentsInCorpus;
        double k = 2;
        double b = 0.75;

        //double cwd = currentQueryTerm.getDocsAndAmount().get(currentQueryDoc.getDocNO()) d ; // normalization
        double cwd = currentQueryDoc.queryTermsInDocsAndQuery.get(currentQueryTerm.value).docsAndAmount.get(currentQueryDoc.docNO);
        if (currentQueryDoc.isContainsQueryTermInHeader()){
            cwd = cwd + 5; // 5 or 4
            //df++;
        }

        /*return (Math.log10((M + 1) / df) * cwq * (((b+1) * cwd) / (cwd + (k * ((1-b) + (b * (d / avdl)))))));*/
        if(currentQueryTerm.isFirstWordInQuery())
            return 5 * Math.log10((M + 1) / df) * cwq * (((k+1) * cwd) / (cwd + (k * ((1-b) + (b * (d / avdl))))));

        if(currentQueryTerm.isSynonym() && ! currentQueryDoc.isContainsQueryTermInHeader() ) {
            return 0.5 * Math.log10((M + 1) / df) * cwq * (((k+1) * cwd) / (cwd + (k * ((1-b) + (b * (d / avdl))))));
        }

        if(currentQueryTerm.isSynonym() && currentQueryDoc.isContainsQueryTermInHeader() ) {
            return 0.6 * Math.log10((M + 1) / df) * cwq * (((k+1) * cwd) / (cwd + (k * ((1-b) + (b * (d / avdl))))));
        }

        if(currentQueryDoc.isContainsQueryTermInHeader() && !currentQueryTerm.isSynonym())
            return Math.log10((M + 1) / df) * cwq * (((k+1) * cwd) / (cwd + (k * ((1-b) + (b * (d / avdl))))));





        return Math.log10((M + 1) / df) * cwq * (((k+1) * cwd) / (cwd + (k * ((1-b) + (b * (d / avdl))))));

    }

    /*private double cosSim(QueryDoc currentQueryDoc, int queryLength) {

        //iterator for the QueryTermsInTheQueryDoc
        Iterator it = currentQueryDoc.getQueryTermsInDocsAndQuery().entrySet().iterator();

        int cwq=0;   //amount of appearances in the query
        int cwd=0;    //amount of appearances in the doc
        int d=0;      //length of the doc
        int dQuery=0;
        int df=0;     //the df of the term in the corpus
        double avdl=0;
        int N=0;     // number of docs in corpus
        avdl = Searcher.avdl;
        N = Searcher.numOfDocumentsInCorpus; // number of docs in corpus
        d = currentQueryDoc.getLength(); //length of the doc
        dQuery = queryLength;

        double Mone=0;
        double Mechane =0;
        double firstSigmaMechane =0;
        double secondSigmaMechane =0;

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();
            QueryTerm currentQueryTerm = (QueryTerm) pair.getValue();

            cwq = currentQueryTerm.getAppearanceInQuery(); //amount of appearances in the query
            cwd = currentQueryTerm.getDocsAndAmount().get(currentQueryDoc.getDocNO()); //amount of appearances in the doc
            df = currentQueryTerm.getDf();  //the df of the term in the corpus

            double temp = 2*Math.log10(N / df)* (cwd / d) * (cwq/dQuery);
            if(currentQueryTerm.isSynonym()){

                Mone = Mone+ 0.5*temp;
                firstSigmaMechane = firstSigmaMechane+ 0.5*Math.pow((cwd / d) * (Math.log10(N / df)),2);
                secondSigmaMechane = secondSigmaMechane+ 0.5*Math.pow((cwq/dQuery) * (Math.log10(N / df)),2);

            }else{

                Mone = Mone+ temp;
                firstSigmaMechane = firstSigmaMechane+ Math.pow((cwd / d) * (Math.log10(N / df)),2);
                secondSigmaMechane = secondSigmaMechane+ Math.pow((cwq/dQuery) * (Math.log10(N / df)),2);

            }

        }

        Mechane = Math.pow(firstSigmaMechane*secondSigmaMechane,(1/2));
        System.out.println("RankCosSim= "+Mone/Mechane);
        return (Mone/Mechane);
    }*/


}
