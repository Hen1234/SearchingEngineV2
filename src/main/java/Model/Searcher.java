package Model;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Searcher {

    Ranker ranker;
    String query;
    String[] splitedQueryAfterParse;
    String queryAfterParse;
    boolean isSemantic;

    public TreeMap<String, String> Dictionary;
    public HashMap<String, Docs> Documents;
    HashMap<String, QueryDoc> docRelevantForTheQuery;
    PriorityQueue<QueryDoc> RankedQueryDocs;
    private ArrayList<String> QueryResults;
    HashSet<String> citiesFromFilter; //hashSet for cities if the user chose filter by city
    static double avdl;
    static int numOfDocumentsInCorpus;


    public Searcher() {

        docRelevantForTheQuery = new HashMap<String, QueryDoc>();
        QueryResults = new ArrayList<>();
        RankedQueryDocs = new PriorityQueue();
        ranker = new Ranker();
        //numOfDocumentsInCorpus = Documents.size();
        citiesFromFilter = null;
        Documents = Indexer.docsHashMap;
        Dictionary = Indexer.sorted;

    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setQueryAfterParse(String queryAfterParse) {
        this.queryAfterParse = queryAfterParse;
    }

    /**
     * Setter for the citiesFromFilter
     *
     * @param cities
     */
    public void setCities(HashSet<String> cities) {
        this.citiesFromFilter = cities;

    }

    public void setSemantic(boolean semantic) {
        isSemantic = semantic;
    }

    public void setDictionary(TreeMap<String, String> dictionary) {
        Dictionary = dictionary;
    }

    public void setDocuments(HashMap<String, Docs> documents) {
        Documents = documents;
    }

    public ArrayList<String> getQueryResults() {
        return QueryResults;
    }

    public void setQueryResults(ArrayList<String> queryResults) {
        QueryResults = queryResults;
    }

    public void pasreQuery(String query) throws IOException {

        //init the Documents and Dictionary HashMap from the index
        Documents = Indexer.docsHashMap;
        Dictionary = Indexer.sorted;
        //initAvdl
        initAvdl();
        //init the size of the numOfDocumentsInCorpus
        numOfDocumentsInCorpus = Documents.size();

        queryAfterParse = ReadFile.p.parser(null, query, ReadFile.toStem, true, false);
        int queryAfterParseLengthBeforeAddSynonym = queryAfterParse.length();
        if (isSemantic) getSemanticSynonym();
        splitedQueryAfterParse = queryAfterParse.split(" ");


        for (int i = 0; i < splitedQueryAfterParse.length; i++) {
            String curretTermOfQuery = splitedQueryAfterParse[i];

            //if the word is a synonym
            if (isSemantic && i > queryAfterParseLengthBeforeAddSynonym) {
                QueryTerm current = initQueryTermAndQueryDocs(curretTermOfQuery, true);
                addDocsRelevantFromHeaders(current);

            } else {
                QueryTerm current = initQueryTermAndQueryDocs(curretTermOfQuery, false);
                addDocsRelevantFromHeaders(current);

            }

        }


        sendToRanker();
        poll50MostRankedDocs();

    }

    private void addDocsRelevantFromHeaders(QueryTerm current) {
        Stemmer stem = new Stemmer();
        HashSet<String> temp;
        if (!ReadFile.toStem)
            temp = Parse.termsInHeaderToDoc.get(current.getValue().toLowerCase());
        else
            temp = Parse.termsInHeaderToDoc.get(stem.stemming(current.getValue().toLowerCase()));
        if (temp != null) { // the term doesnt exist in any header
            Iterator it = temp.iterator();
            while (it.hasNext()) {
                String docName = (String) it.next();
                System.out.println("itratorDocsHeader: "+docName);
                if (!docRelevantForTheQuery.containsKey(docName)) {
                    QueryDoc toInsert = new QueryDoc(docName);
                    toInsert.setLength(Documents.get(docName).getDocLength());
                    toInsert.getQueryTermsInDocsAndQuery().put(current.getValue(), current);
                    toInsert.setContainsQueryTermInHeader(true);
                    current.getDocsAndAmount().put(toInsert.getDocNO(),1);
                    docRelevantForTheQuery.put(toInsert.getDocNO(), toInsert);
                }
            }
        }
    }


    private void poll50MostRankedDocs() throws IOException {

        //poll the 50 most ranked docs from the qDocQueue
        String folderName = ReadFile.postingPath;
        if (ReadFile.toStem) {
            folderName = folderName + "\\" + "WithStemming";
        } else {
            folderName = folderName + "\\" + "WithoutStemming";
        }
        File f = new File(folderName+"\\" + "result.txt");
        FileOutputStream fos = new FileOutputStream(f.getPath());
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osr);

        int b = 0;
        while (!ranker.getqDocQueue().isEmpty() && b < 50) {
            QueryDoc currentQueryDocFromQueue = (QueryDoc) ranker.getqDocQueue().poll();
            String s = "351 0 "+currentQueryDocFromQueue.docNO+" "+" 1 42.38 mt"+System.lineSeparator();
            bw.write(s);
            bw.flush();
            QueryResults.add(currentQueryDocFromQueue.docNO);
            System.out.println(currentQueryDocFromQueue.toString() + System.lineSeparator()+" after rankin");
            currentQueryDocFromQueue.setRank(0);
            b++;
        }

        //set the rank of the rest of the docs in the queue to 0
        while (!ranker.getqDocQueue().isEmpty()) {
            QueryDoc currentQueryDocFromQueue = (QueryDoc) ranker.getqDocQueue().poll();
            currentQueryDocFromQueue.setRank(0);
        }
        //init the HashMap of the relevantDoc
        docRelevantForTheQuery = new HashMap<>();
        //init the qDocQueue
        ranker.setqDocQueue(new PriorityQueue<>());


    }

    private void sendToRanker() throws IOException {
        String folderName = ReadFile.postingPath;
        if (ReadFile.toStem) {
            folderName = folderName + "\\" + "WithStemming";
        } else {
            folderName = folderName + "\\" + "WithoutStemming";
        }
        File f = new File(folderName+"\\" + "result.txt");
        FileOutputStream fos = new FileOutputStream(f.getPath());
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osr);

        //iterate each relevant doc and send to Ranker
        Iterator it = docRelevantForTheQuery.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            /*String s = "351 0 "+((QueryDoc) pair.getValue()).docNO+" "+" 1 42.38 mt"+System.lineSeparator();
            bw.write(s);
            bw.flush();*/
            ranker.getQueryDocFromSearcher((QueryDoc) pair.getValue(), splitedQueryAfterParse.length);
            RankedQueryDocs.add((QueryDoc) pair.getValue());
            System.out.println(docRelevantForTheQuery.size());
            System.out.println(pair.getKey());
        }
    }


    private QueryTerm initQueryTermAndQueryDocs(String StringcurretTermOfQuery, boolean isSynonym) {

        QueryTerm currentQueryTerm = null;

        //check if the term exists the dictionary
        if (Dictionary.containsKey(StringcurretTermOfQuery.toLowerCase())) {
            //create a new QueryTerm
            currentQueryTerm = new QueryTerm(StringcurretTermOfQuery.toLowerCase());
            //update isSynonym
            if (isSynonym) currentQueryTerm.setSynonym(true);
        } else {
            //toUpperCase
            if (Dictionary.containsKey(StringcurretTermOfQuery.toUpperCase())) {
                //create a new QueryTerm
                currentQueryTerm = new QueryTerm(StringcurretTermOfQuery.toUpperCase());
                if (isSynonym) currentQueryTerm.setSynonym(true);
            }
        }

        if (currentQueryTerm != null) {


            //take the term's pointer from the dictionary
            String pointer = Indexer.sorted.get(currentQueryTerm.getValue());
            String[] numOfFileAndLineOfTerm = pointer.split(",");
            String fileNum = numOfFileAndLineOfTerm[0];
            String lineNum = numOfFileAndLineOfTerm[1];
            Integer lineNumInt = Integer.parseInt(lineNum) - 1;
            String lineFromFile = "";
            try {
                //doc:FBIS3-29#2=27066 ,27079 doc:FBIS3-5232#1=481 DF- 2 TIC- 3
                lineFromFile = Files.readAllLines(Paths.get(Indexer.pathDir + "\\finalposting" + fileNum + ".txt")).get(lineNumInt);
            } catch (Exception e) {
            }

            //ArrayList<String> docs = new ArrayList<>();
            //ArrayList<Integer> amountsPerDoc = new ArrayList<>();
            String docNo = "";
            String tfString = "";

            //update the hashMap of docs and df of the currentQueryTerm
            for (int k = 0; k < lineFromFile.length(); k++) {

                docNo = "";
                tfString = "";
                if (lineFromFile.charAt(k) == ':') {
                    k++;

                    //find the doc
                    while (lineFromFile.charAt(k) != '#') {
                        docNo = docNo + lineFromFile.charAt(k);
                        k++;
                    }
                    k++;

                    //find the amountAppearence in the doc
                    while (lineFromFile.charAt(k) != '=') {
                        tfString = tfString + lineFromFile.charAt(k);
                        k++;
                    }

                    int tf = Integer.parseInt(tfString);

                    if (Documents.containsKey(docNo)) {
                        Docs docFromOriginalDocs = Documents.get(docNo);

                        //if there is filter by city
                        if (citiesFromFilter != null) {
                            for (String city : citiesFromFilter) {

                                //the doc's city included the filter
                                if (city.equals(docFromOriginalDocs.getCity())) {
                                    //add the doc to the QueryTerm
                                    currentQueryTerm.getDocsAndAmount().put(docNo, tf);
                                    //add the QueryTerm to the relevant doc
                                    QueryDoc newQueryDoc = new QueryDoc(docFromOriginalDocs.getDocNo());
                                    newQueryDoc.setLength(docFromOriginalDocs.getDocLength());
                                    //add the QueryTerm to the relevant doc
                                    newQueryDoc.getQueryTermsInDocsAndQuery().put(currentQueryTerm.getValue(), currentQueryTerm);
                                    //add the new QueryDoc to the HashSet of the relevant docs for the query


                                    if (!docRelevantForTheQuery.containsKey(newQueryDoc.getDocNO()))
                                        docRelevantForTheQuery.put(newQueryDoc.getDocNO(), newQueryDoc);


                                }


                            }

                            //there is no filter by city
                        } else {


                            //add the doc to the QueryTerm
                            currentQueryTerm.getDocsAndAmount().put(docNo, tf);

                            QueryDoc newQueryDoc = new QueryDoc(docFromOriginalDocs.getDocNo());
                            //set the length of the relevant doc
                            newQueryDoc.setLength(docFromOriginalDocs.getDocLength());
                            //add the QueryTerm to the relevant doc
                            newQueryDoc.getQueryTermsInDocsAndQuery().put(currentQueryTerm.getValue(), currentQueryTerm);
                            //add the new QueryDoc to the HashSet of the relevant docs for the query
                            if (!docRelevantForTheQuery.containsKey(newQueryDoc.getDocNO()))
                                docRelevantForTheQuery.put(newQueryDoc.getDocNO(), newQueryDoc);

                        }
                    }


                }
                if (lineFromFile.charAt(k) == 'D' && k + 5 < lineFromFile.length() &&
                        lineFromFile.charAt(k + 1) == 'F' && lineFromFile.charAt(k + 2) == '-' &&
                        lineFromFile.charAt(k + 3) == ' ') {

                    String df = "";
                    int q = 4;
                    while (k + q < lineFromFile.length()) {
                        if (lineFromFile.charAt(k + q) != ' ') {
                            df = df + lineFromFile.charAt(k + q);
                            break;
                        }
                        q++;

                    }

                    try {
                        Integer dfInt = Integer.parseInt(df);
                        currentQueryTerm.setDf(dfInt);
                    } catch (Exception e) {
                    }

                }


            }


            //update the amount of appearence in the query
            for (int i = 0; i < splitedQueryAfterParse.length; i++) {

                if (splitedQueryAfterParse[i].equals(StringcurretTermOfQuery)) {
                    currentQueryTerm.setAppearanceInQuery(currentQueryTerm.getAppearanceInQuery() + 1);
                }


            }

        }

        return currentQueryTerm;

    }

    private void initAvdl() {
        Integer countDocsLength = 0;
        Iterator it = Documents.entrySet().iterator();
        while (it.hasNext()) {
            //Terms nextTerm = (Terms) it.next();
            //text.append(nextTerm.getValue());
            Map.Entry pair = (Map.Entry) it.next();
            countDocsLength = countDocsLength + ((Docs) pair.getValue()).getDocLength();
        }
        avdl = countDocsLength / Documents.size();
    }

//    private void loadDocuments() {
//
//        try {
//
//            FileInputStream f = new FileInputStream(new File(Indexer.pathDir + "\\" + "DocsAsObject.txt"));
//
//            ObjectInputStream o = new ObjectInputStream(f);
//            Documents = (HashMap<String, Docs>) o.readUnshared();
//            o.close();
//
//        } catch (Exception e) {
//        }
//
//
//    }

    /**
     * The method update the query with the synonym words
     */
    private void getSemanticSynonym() {

        try {

            splitedQueryAfterParse = queryAfterParse.split(" ");
            URL APILink;
            for (String queryTerm : splitedQueryAfterParse) {

                APILink = new URL("https://api.datamuse.com/words?rel_syn=" + queryTerm);
                BufferedReader read = new BufferedReader(new InputStreamReader(APILink.openStream()));
                String line = read.readLine();
                read.close();

                if (line.equals("[]")) {
                    continue;
                }
                String[] synonymsTerms = line.split("\":\"");
                for (int i = 1; i < synonymsTerms.length && i < 2; i++) {
                    int temp = 0;
                    while (synonymsTerms[i].charAt(temp) != '"')
                        temp++;

                    setQueryAfterParse(queryAfterParse + " " + synonymsTerms[i].substring(0, temp));

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}