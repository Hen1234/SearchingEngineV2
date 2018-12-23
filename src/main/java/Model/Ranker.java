package Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

public class Ranker {

    private PriorityQueue<QueryDoc> qDocQueue;
    private int queryLength;


    public Ranker() {
        qDocQueue = new PriorityQueue<QueryDoc>();
        queryLength = 0;
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
            currentQueryDoc.setRank(currentQueryDoc.getRank() + 0.75*BM25func(currentQueryTerm, currentQueryDoc));
            //System.out.println(currentQueryDoc.getDocNO()+"rank= "+currentQueryDoc.getRank());



        }

        //update the currentQueryDoc's rank by cosSim
        currentQueryDoc.setRank(currentQueryDoc.getRank() + 0.25*cosSim(currentQueryDoc, queryLength));
        qDocQueue.add(currentQueryDoc);
    }

    private double BM25func(QueryTerm currentQueryTerm, QueryDoc currentQueryDoc) {



        int cwq = currentQueryTerm.getAppearanceInQuery();
        int cwd = currentQueryTerm.getDocsAndAmount().get(currentQueryDoc.getDocNO());
        int d = currentQueryDoc.getLength();
        int df = currentQueryTerm.getDf();
        double avdl = Searcher.avdl;
        int M = Searcher.numOfDocumentsInCorpus;

        //k=2, B=0.75

        if(currentQueryTerm.isSynonym() && ! currentQueryDoc.isContainsQueryTermInHeader() )
            return 0.5*(Math.log10((M + 1) / df) * cwq * ((3 * cwd) / (cwd + (2 * (0.25 + (0.75 * (d / avdl)))))));

        if(currentQueryDoc.isContainsQueryTermInHeader() && !currentQueryTerm.isSynonym())
            return 2*(Math.log10((M + 1) / df) * cwq * ((3 * cwd) / (cwd + (2 * (0.25 + (0.75 * (d / avdl)))))));


        return Math.log10((M + 1) / df) * cwq * ((3 * cwd) / (cwd + (2 * (0.25 + (0.75 * (d / avdl))))));

    }

    private double cosSim(QueryDoc currentQueryDoc, int queryLength) {

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
    }

}
