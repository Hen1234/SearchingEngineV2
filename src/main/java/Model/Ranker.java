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
        initDebug();
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
            currentQueryDoc.setRank(currentQueryDoc.getRank() + BM25func(currentQueryTerm, currentQueryDoc,(double)queryLength))
            /*+ tfIDF(currentQueryTerm, currentQueryDoc,(double)queryLength))*/;
            //System.out.println(currentQueryDoc.getDocNO()+"rank= "+currentQueryDoc.getRank());
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
            df = 2;
        }
        if(currentQueryTerm.isSynonym() && ! currentQueryDoc.isContainsQueryTermInHeader() ){
            return (cwq * cwd * Math.log10((M+1)/df)*0.5);
        }
        if(currentQueryDoc.isContainsQueryTermInHeader() && !currentQueryTerm.isSynonym()){
            return (cwq * cwd * Math.log10((M+1)/df)*5);
        }
        return (cwq * cwd * Math.log10((M+1)/df));

    }

    private double BM25func(QueryTerm currentQueryTerm, QueryDoc currentQueryDoc , double queryLength) {


        if (currentQueryDoc.docNO.equals("FBIS3-59016")){
            System.out.println("here");
        }
        double cwq = currentQueryTerm.getAppearanceInQuery()/* / queryLength*/;
        double d = currentQueryDoc.getLength();
        double df = currentQueryTerm.getDf();
        double avdl = Searcher.avdl;
        double M = Searcher.numOfDocumentsInCorpus;
        //double cwd = currentQueryTerm.getDocsAndAmount().get(currentQueryDoc.getDocNO()) /*/d*/ ; // normalization
        double cwd = currentQueryDoc.queryTermsInDocsAndQuery.get(currentQueryTerm.value).docsAndAmount.get(currentQueryDoc.docNO);
        if (currentQueryDoc.isContainsQueryTermInHeader()){
            cwd = cwd +2;
            df++;
        }
        /*if (Searcher.docRelevantForTheQuery.get(currentQueryDoc.docNO).containsQueryTermInHeader){
            cwd = cwd +2;
            df++;
        }*/

        //k=2, B=0.75

        if(currentQueryTerm.isSynonym() && ! currentQueryDoc.isContainsQueryTermInHeader() )
            return 0.5*(Math.log10((M + 1) / df) * cwq * ((3 * cwd) / (cwd + (2 * (0.25 + (0.75 * (d / avdl)))))));

        if(currentQueryDoc.isContainsQueryTermInHeader() && !currentQueryTerm.isSynonym())
            return 2*(Math.log10((M + 1) / df) * cwq * ((3 * cwd) / (cwd + (2 * (0.25 + (0.75 * (d / avdl)))))));


        return Math.log10((M + 1) / df) * cwq * ((3 * cwd) / (cwd + (2 * (0.25 + (0.75 * (d / avdl))))));

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
    private void initDebug() {

        debug.add("FBIS3-10551");
        debug.add("FBIS3-10646");
        debug.add("FBIS3-10697");
        debug.add("FBIS3-11107");
        debug.add("FBIS3-19947");
        debug.add("FBIS3-33035");
        debug.add("FBIS3-33505");
        debug.add("FBIS3-50570");
        debug.add("FBIS3-59016");
        debug.add("FBIS4-10762");
        debug.add("FBIS4-11114");
        debug.add("FBIS4-34579");
        debug.add("FBIS4-34996");
        debug.add("FBIS4-35048");
        debug.add("FBIS4-56243");
        debug.add("FBIS4-56741");
        debug.add("FBIS4-57354");
        debug.add("FBIS4-64976");
        debug.add("FBIS4-9937");
        debug.add("FT921-2097");
        debug.add("FT921-6272");
        debug.add("FT921-6603");
        debug.add("FT921-8458");
        debug.add("FT922-14936");
        debug.add("FT922-15099");
        debug.add("FT922-3165");
        debug.add("FT922-8324");
        debug.add("FT923-11890");
        debug.add("FT923-1456");
        debug.add("FT924-1564");
        debug.add("FT931-10913");
        debug.add("FT931-16617");
        debug.add("FT931-932");
        debug.add("FT932-16710");
        debug.add("FT932-6577");
        debug.add("FT934-13429");
        debug.add("FT934-13954");
        debug.add("FT934-4629");
        debug.add("FT934-4848");
        debug.add("FT934-4856");
        debug.add("FT941-13429");
        debug.add("FT941-7250");
        debug.add("FT941-9999");
        debug.add("FT942-12805");
        debug.add("FT943-14758");
        debug.add("FT943-15117");
        debug.add("FT944-15849");


    }

}
