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

    HashMap<String, String> codesAndQueries;
    private TreeMap<String, ArrayList<String>> QueryIDandResultsForFile;  //TreeMap: key->queryID, value->queryResults
    public TreeMap<String, String> Dictionary;
    public HashMap<String, Docs> Documents;
    static HashMap<String, QueryDoc> docRelevantForTheQuery;
    PriorityQueue<QueryDoc> RankedQueryDocs;
    private ArrayList<String> QueryResults;
    private ArrayList<String> QueryResultsForFile;
    HashSet<String> citiesFromFilter; //hashSet for cities if the user chose filter by city
    static double avdl;
    static int numOfDocumentsInCorpus;
    String resultPath;


    public Searcher() {

        docRelevantForTheQuery = new HashMap<String, QueryDoc>();
        QueryResults = new ArrayList<>();
        QueryIDandResultsForFile = new TreeMap<>();
        QueryResultsForFile = new ArrayList<>();
        RankedQueryDocs = new PriorityQueue();
        ranker = new Ranker();
        //numOfDocumentsInCorpus = Documents.size();
        citiesFromFilter = null;
        Documents = Indexer.docsHashMap;
        Dictionary = Indexer.sorted;

    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public TreeMap<String, ArrayList<String>> getQueryIDandResultsForFile() {
        return QueryIDandResultsForFile;
    }

    public void setQueryIDandResultsForFile(TreeMap<String, ArrayList<String>> queryIDandResultsForFile) {
        QueryIDandResultsForFile = queryIDandResultsForFile;
    }

    public ArrayList<String> getQueryResultsForFile() {
        return QueryResultsForFile;
    }

    public void setQueryResultsForFile(ArrayList<String> queryResultsForFile) {
        QueryResultsForFile = queryResultsForFile;
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

    /**
     * @param query
     * @return
     * @throws IOException
     */
    public ArrayList<String> pasreQuery(String query) throws IOException {
        System.out.println("Query: " + query);
        //init the Documents and Dictionary HashMap from the index
        Documents = Indexer.docsHashMap;
        Dictionary = Indexer.sorted;
        //initAvdl
        initAvdl();
        //init the size of the numOfDocumentsInCorpus
        numOfDocumentsInCorpus = Documents.size();

        queryAfterParse = ReadFile.p.parser(null, query, ReadFile.toStem, true, false);
        int queryAfterParseLengthBeforeAddSynonym = queryAfterParse.split(" ").length;
        if (isSemantic) getSemanticSynonym();
        splitedQueryAfterParse = queryAfterParse.split(" ");

        //check dash
        for (int k = 0; k < splitedQueryAfterParse.length; k++) {

            if (isContainDash(splitedQueryAfterParse[k])) {

                String beforeDash = "";
                String afterDash = "";

                int i = 0;
                for (i = 0; i < splitedQueryAfterParse[k].length(); i++) {
                    if (!(splitedQueryAfterParse[k].charAt(i) == '-')) {
                        beforeDash = beforeDash + splitedQueryAfterParse[k].charAt(i);

                    } else {
                        break;
                    }

                }
                for (int j = i + 1; j < splitedQueryAfterParse[k].length(); j++) {
                    afterDash = afterDash + splitedQueryAfterParse[k].charAt(j);

                }

                try {
                    String[] newSplitedQueryAfterParse = new String[splitedQueryAfterParse.length + 2];

                    int j = 0;
                    for (j = 0; j < splitedQueryAfterParse.length; j++) {
                        newSplitedQueryAfterParse[j] = splitedQueryAfterParse[j];

                    }
                    newSplitedQueryAfterParse[j] = beforeDash;
                    newSplitedQueryAfterParse[j + 1] = afterDash;
                    splitedQueryAfterParse = newSplitedQueryAfterParse;
                } catch (Exception e) {
                }


            }
            String curretTermOfQuery = splitedQueryAfterParse[k];

            //if the word is a synonym
            if (isSemantic && k > queryAfterParseLengthBeforeAddSynonym) {
                QueryTerm current = initQueryTermAndQueryDocs(curretTermOfQuery, true);
                addDocsRelevantFromHeaders(current);

            } else {
                QueryTerm current = initQueryTermAndQueryDocs(curretTermOfQuery, false);
                if (k == 0) current.setFirstWordInQuery(true);
                addDocsRelevantFromHeaders(current);

            }

        }


        sendToRanker();

        return poll50MostRankedDocs();

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
                if (!docRelevantForTheQuery.containsKey(docName)) {
                    QueryDoc toInsert = new QueryDoc(docName);
                    toInsert.setLength(Documents.get(docName).getDocLength());
                    toInsert.getQueryTermsInDocsAndQuery().put(current.getValue(), current);
                    toInsert.setContainsQueryTermInHeader(true);
                    current.getDocsAndAmount().put(toInsert.getDocNO(), 1);
                    docRelevantForTheQuery.put(toInsert.getDocNO(), toInsert);
                } else {
                    docRelevantForTheQuery.get(docName).setContainsQueryTermInHeader(true);
                }
            }
        }
    }


    private ArrayList<String> poll50MostRankedDocs() throws IOException {

        //poll the 50 most ranked docs from the qDocQueue
        String folderName = ReadFile.postingPath;
        /*if (ReadFile.toStem) {
            folderName = folderName + "\\" + "WithStemming";
        } else {
            folderName = folderName + "\\" + "WithoutStemming";
        }*/
       /* File f = new File(folderName + "\\" + "result.txt");
        FileOutputStream fos = new FileOutputStream(f.getPath());
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osr);*/

        int b = 0;
        while (!ranker.getqDocQueue().isEmpty() && b < 50) {
            QueryDoc currentQueryDocFromQueue = (QueryDoc) ranker.getqDocQueue().poll();
            /*String s = "351 0 " + currentQueryDocFromQueue.docNO + " " + " 1 42.38 mt" + System.lineSeparator();
            bw.write(s);
            bw.flush();*/
            QueryResults.add(currentQueryDocFromQueue.docNO);
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
        return QueryResults;


    }

    private void sendToRanker() throws IOException {

        String folderName = ReadFile.postingPath;
        /*if (ReadFile.toStem) {
            folderName = folderName + "\\" + "WithStemming";
        } else {
            folderName = folderName + "\\" + "WithoutStemming";
        }*/
        File f = new File(folderName + "\\" + "result.txt");
        FileOutputStream fos = new FileOutputStream(f.getPath());
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osr);

        //iterate each relevant doc and send to Ranker
        Iterator it = docRelevantForTheQuery.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ranker.getQueryDocFromSearcher((QueryDoc) pair.getValue(), splitedQueryAfterParse.length);
            RankedQueryDocs.add((QueryDoc) pair.getValue());
        }
    }


    private QueryTerm initQueryTermAndQueryDocs(String StringcurretTermOfQuery, boolean isSynonym) {

        QueryTerm currentQueryTerm = null;

        //check if the term exists the dictionary
        boolean isNull = true;
        if (Dictionary.containsKey(StringcurretTermOfQuery.toUpperCase())) {
            //toUpperCase
            if (Dictionary.containsKey(StringcurretTermOfQuery.toUpperCase())) {
                //create a new QueryTerm
                currentQueryTerm = new QueryTerm(StringcurretTermOfQuery.toUpperCase());
                isNull = false;
                if (isSynonym) currentQueryTerm.setSynonym(true);
            }
        } else if
                (Dictionary.containsKey(StringcurretTermOfQuery.toLowerCase())) {
            //create a new QueryTerm
            currentQueryTerm = new QueryTerm(StringcurretTermOfQuery.toLowerCase());
            isNull = false;
            //update isSynonym
            if (isSynonym) currentQueryTerm.setSynonym(true);

        }
        if (isNull) {
            String withS = "";
            if (Character.isLowerCase(StringcurretTermOfQuery.charAt(StringcurretTermOfQuery.length() - 1))) {
                if (Dictionary.containsKey(StringcurretTermOfQuery.toLowerCase() + "s")) {
                    currentQueryTerm = new QueryTerm(StringcurretTermOfQuery.toLowerCase() + "s");
                }
            } else {
                if (Dictionary.containsKey(StringcurretTermOfQuery.toUpperCase() + "S")) {
                    currentQueryTerm = new QueryTerm(StringcurretTermOfQuery.toUpperCase() + "S");
                }
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
            String locations = "";

            //update the hashMap of docs and df of the currentQueryTerm
            for (int k = 0; k < lineFromFile.length(); k++) {

                docNo = "";
                tfString = "";
                locations = "";
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

                    //find location in the doc
                    k++;
                    while (k > lineFromFile.length() - 1 && lineFromFile.charAt(k) != 'd') {
                        locations = locations + lineFromFile.charAt(k);
                        k++;
                    }

                    String[] locations1 = locations.split(" ");
                    for (int i = 0; i < locations1.length; i++) {
                        for (int j = 0; j < locations1[i].length(); j++) {
                            if (locations1[i].charAt(j) == ',') {
                                locations1[i] = locations1[i].substring(i, locations1[i].length());
                                break;

                            }

                        }

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

                                    Iterator it = docFromOriginalDocs.getMostFiveFrequencyEssences().iterator();
                                    while (it.hasNext()) {
                                        TermsPerDoc cur = (TermsPerDoc) it.next();
                                        if (cur.getValue().equals(currentQueryTerm.getValue())) {
                                            newQueryDoc.setQueryContainEntitiy(true);
                                        }
                                    }

                                    newQueryDoc.setLength(docFromOriginalDocs.getDocLength());
                                    //update the locations
                                    ArrayList<String> locat = newQueryDoc.getLocations();
                                    for (int i = 0; i < locations1.length; i++) {
                                        locat.add(locations1[i]);

                                    }
                                    //add the QueryTerm to the relevant doc
                                    newQueryDoc.getQueryTermsInDocsAndQuery().put(currentQueryTerm.getValue(), currentQueryTerm);
                                    //add the new QueryDoc to the HashSet of the relevant docs for the query


                                    if (!docRelevantForTheQuery.containsKey(newQueryDoc.getDocNO()))
                                        docRelevantForTheQuery.put(newQueryDoc.getDocNO(), newQueryDoc);
                                    else {
                                        if (!docRelevantForTheQuery.get(newQueryDoc.getDocNO()).getQueryTermsInDocsAndQuery().containsKey(currentQueryTerm.getValue())) {
                                            docRelevantForTheQuery.get(newQueryDoc.getDocNO()).getQueryTermsInDocsAndQuery().put(currentQueryTerm.getValue(), currentQueryTerm);
                                        }
                                    }
                                }
                            }

                            //there is no filter by city
                        } else {


                            //add the doc to the QueryTerm
                            currentQueryTerm.getDocsAndAmount().put(docNo, tf);

                            QueryDoc newQueryDoc = new QueryDoc(docFromOriginalDocs.getDocNo());

                            Iterator it = docFromOriginalDocs.getMostFiveFrequencyEssences().iterator();
                            while (it.hasNext()) {
                                TermsPerDoc cur = (TermsPerDoc) it.next();
                                if (cur.getValue().equals(currentQueryTerm.getValue())) {
                                    newQueryDoc.setQueryContainEntitiy(true);
                                }
                            }

                            //set the length of the relevant doc
                            newQueryDoc.setLength(docFromOriginalDocs.getDocLength());
                            ArrayList<String> locat = newQueryDoc.getLocations();
                            for (int i = 0; i < locations1.length; i++) {
                                locat.add(locations1[i]);

                            }
                            //add the QueryTerm to the relevant doc
                            newQueryDoc.getQueryTermsInDocsAndQuery().put(currentQueryTerm.getValue(), currentQueryTerm);
                            //add the new QueryDoc to the HashSet of the relevant docs for the query
                            if (!docRelevantForTheQuery.containsKey(newQueryDoc.getDocNO()))
                                docRelevantForTheQuery.put(newQueryDoc.getDocNO(), newQueryDoc);
                            else {
                                if (!docRelevantForTheQuery.get(newQueryDoc.getDocNO()).getQueryTermsInDocsAndQuery().containsKey(currentQueryTerm.getValue())) {
                                    docRelevantForTheQuery.get(newQueryDoc.getDocNO()).getQueryTermsInDocsAndQuery().put(currentQueryTerm.getValue(), currentQueryTerm);
                                }
                            }
                        }
                    }
                }


            }
            //update df
            for (int j = lineFromFile.length() - 1; j < lineFromFile.length(); j--) {
                if (lineFromFile.charAt(j) == 'D' && j + 5 < lineFromFile.length() &&
                        lineFromFile.charAt(j + 1) == 'F' && lineFromFile.charAt(j + 2) == '-' &&
                        lineFromFile.charAt(j + 3) == ' ') {
                    String df = "";
                    j = j + 4;
                    while (lineFromFile.charAt(j) != ' ') {
                        df = df + lineFromFile.charAt(j);
                        j++;
                    }

                    try {
                        Integer dfInt = Integer.parseInt(df);
                        currentQueryTerm.setDf(dfInt);
                        break;
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
        } else

        { // query term not in dictionary (null)
            return new QueryTerm(StringcurretTermOfQuery);
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


    public HashMap<String, String> readQueriesFile(String path) throws IOException {
        int k = 0;
        HashMap<String, String> ans = new HashMap<>();
        String queryNum = "";
        String query = "";
        FileInputStream f = new FileInputStream(new File(path));// + "\\queries.txt"));
        InputStreamReader isr = new InputStreamReader(f);
        BufferedReader br = new BufferedReader(isr);
        //searcher.setDictionary(Dictionary);
        String line = "";
        while (line != null) {
            line = br.readLine();
            if (line == null)
                break;
            while (line.length() > 0 && line.charAt(0) != '<') {
                line = br.readLine();
                if (line == null)
                    break;
            }
            while (!(line.length() > 6 && line.charAt(1) == 'n' && line.charAt(2) == 'u' && line.charAt(3) == 'm'
                    && line.charAt(4) == '>')) {
                line = br.readLine();
                if (line == null)
                    break;
            }
            if (line == null)
                break;
            if (line.length() > 6 && line.charAt(1) == 'n' && line.charAt(2) == 'u' && line.charAt(3) == 'm'
                    && line.charAt(4) == '>') {
                k = line.length() - 1;
                while (line.charAt(k) == ' ')
                    k--;
                while (line.charAt(k) != ' ') {
                    queryNum = line.charAt(k) + queryNum;
                    k--;
                }
            }
            line = br.readLine();
            if (line == null)
                break;
            while (line.length() == 0 || line.charAt(0) != '<') {
                line = br.readLine();
                if (line == null)
                    break;
            }
            k = 0;
            if (line == null)
                break;
            if (line.length() > 6 && line.charAt(1) == 't' && line.charAt(2) == 'i' && line.charAt(3) == 't'
                    && line.charAt(4) == 'l' && line.charAt(5) == 'e' && line.charAt(6) == '>') {
                k = 7;
                while (line.charAt(k) == ' ')
                    k++;
                while (k < line.length()) {
                    query = query + line.charAt(k);
                    k++;
                }
            }


            while (queryNum != null && queryNum.length() > 0 && queryNum.charAt(queryNum.length() - 1) == ' ') {
                queryNum = queryNum.substring(0, queryNum.length() - 1);
            }
            while (query != null && query.length() > 0 && query.charAt(query.length() - 1) == ' ') {
                query = query.substring(0, query.length() - 1);
            }
//description
//            line = br.readLine();
//            if(line.equals("")){
//                line = br.readLine();
//            }
//            if(line.charAt(0)== '<') {
//                for (int i = 1; i < line.length(); i++) {
//                    if (line.charAt(i) == 'd' && i + 1 < line.length() && line.charAt(i + 1) == 'e' &&
//                            i + 2 < line.length() && line.charAt(i + 2) == 's' && i + 3 < line.length() && line.charAt(i + 3) == 'c') {
//                        line = br.readLine();
//
//                        query = query + " " + line;
//                        break;
//
//                    }
//
//                }
//
//
//            }
//            line = br.readLine();
//            while(line!=null &&!(line.equals(""))){
//                if(!(line.charAt(0)=='<')){
//                    query = query + " " + line;
//                    line = br.readLine();
//                }
//               break;
//
//            }

            ans.put(queryNum, query);
            queryNum = "";
            query = "";


        }
        codesAndQueries = ans;

        Iterator it = codesAndQueries.entrySet().iterator();
        //init thre TreeMap of queriesResults
        QueryIDandResultsForFile = new TreeMap<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            String queryString = (String) pair.getValue();
            ArrayList<String> temp = pasreQuery(queryString);

            //add the queryResults to the queryResultsForFile
            for (int i = 0; i < temp.size(); i++) {
                QueryResultsForFile.add(temp.get(i));
            }

            QueryIDandResultsForFile.put(key, temp);
            QueryResults = new ArrayList<>();
        }

        //File res = new File(ReadFile.postingPath + "\\" + "result.txt");
        File res = new File(resultPath + "\\result.txt");
        FileOutputStream fos = new FileOutputStream(res.getPath());
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        BufferedWriter bw = new BufferedWriter(osr);
        StringBuilder s = new StringBuilder("");
        Iterator iter = QueryIDandResultsForFile.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry pair = (Map.Entry) iter.next();
            String key = (String) pair.getKey();
            ArrayList<String> queryString = (ArrayList<String>) pair.getValue();
            //ArrayList<String> temp = pasreQuery(queryString);
            for (int i = 0; i < queryString.size(); i++) {
                s.append(key + " 0 " + queryString.get(i) + " " + " 1 42.38 mt" + System.lineSeparator());
                /*bw.write(s);
                bw.flush();*/
            }
            QueryResults = new ArrayList<>();
        }
        bw.write(s.toString());
        bw.flush();
        osr.close();
        fos.close();
        bw.close();

        return codesAndQueries;
    }

    // is the given string contain '-'
    private boolean isContainDash(String str) {
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '-') return true;
        return false;
    }
}