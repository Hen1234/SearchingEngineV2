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
        queryLength=0;
    }

    public PriorityQueue<QueryDoc> getqDocQueue() {
        return qDocQueue;
    }

    public void setqDocQueue(PriorityQueue<QueryDoc> qDocQueue) {
        this.qDocQueue = qDocQueue;
    }

    public void getQueryDocFromSearcher(QueryDoc currentQueryDoc, int queryLength){


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
            currentQueryDoc.setRank(currentQueryDoc.getRank()+BM25func(currentQueryTerm, currentQueryDoc));
            //System.out.println(currentQueryDoc.getDocNO()+"rank= "+currentQueryDoc.getRank());


        }
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

        return Math.log10((M+1)/df)*cwq*((3*cwd)/(cwd+(2*(0.25+(0.75*(d/avdl))))));

    }

    private double cosSim(QueryTerm currentQueryTerm, QueryDoc currentQueryDoc) {

        int cwq = currentQueryTerm.getAppearanceInQuery(); //amount of appearances in the query
        int cwd = currentQueryTerm.getDocsAndAmount().get(currentQueryDoc.getDocNO()); //amount of appearances in the doc
        int d = currentQueryDoc.getLength(); //length of the doc
        int dQuery = queryLength;
        int df = currentQueryTerm.getDf();  //the df of the term in the corpus
        double avdl = Searcher.avdl;
        int N = Searcher.numOfDocumentsInCorpus; // number of docs in corpus


        double Mone = (cwd/d) * (Math.log10(N/df));

        return 0;

    }


}
