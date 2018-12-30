package Model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * The class responsible for reading the corpus, separating the documents and send them to the Parse and the Indexer
 */
public class ReadFile implements Serializable {


    private Docs doc;
    private HashMap<String, String> replacements;


    ////////////////////
    HashSet<String> debug;
    //\\\\\\\\\\\\\\\
    File[] files;
    private ArrayList<String> words;
    static Parse p;
    private Indexer indexer;
    private CitiesIndexer citiesIndexer;
    static int countFiles;
    static int countDocs;
    private String corpusPath;
    private String stopWordsPath;
    static String postingPath;
    static boolean toStem;
    private HashSet<String> languages;
    private HashMap<String,City> cities;

    /**
     * Constructor- initialize the fields and call the initial methods
     *
     * @throws FileNotFoundException
     */
    public ReadFile() {
        p = new Parse();
        debug = new HashSet<>();
        initHashet();
        indexer = new Indexer();
        citiesIndexer = new CitiesIndexer();
        replacements = new HashMap<String, String>();
        this.intitialMap();
        countFiles = 0;
        corpusPath = "";
        stopWordsPath = "";
        postingPath = "";
        languages = p.getLanguages();
        cities = p.getCities();
    }

    private void initHashet() {
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
    }

    /**
     * Getter for the Parse object of the class
     *
     * @return
     */
    public Parse getP() {
        return p;
    }

    /**
     * Getter for the Indexer object of the class
     *
     * @return
     */
    public Indexer getIndexer() {
        return indexer;
    }

    /**
     * Setter for the path of the stopWords got from the user
     *
     * @param stopWordsPath
     */
    public void setStopWordsPath(String stopWordsPath) {
        this.stopWordsPath = stopWordsPath;
    }

    /**
     * Getter for the language HashSet includes the whole languages of the docs in corpus
     *
     * @return
     */
    public HashSet<String> getLanguages() {
        return languages;
    }

    public void setLanguages(HashSet<String> languages) {
        this.languages = languages;
    }

    public HashMap<String, City> getCities() {
        return cities;
    }

    public void setCities(HashMap<String, City> cities) {
        this.cities = cities;
    }

    /**
     * Setter for the path of the corpus got from the user
     *
     * @param corpusPath
     */
    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    /**
     * Setter for the path of the posting files got from the user
     *
     * @param postingPath
     */
    public void setPostingPath(String postingPath) {
        this.postingPath = postingPath;
    }

    /**
     * Setter for the value of the stemming object came from the user
     *
     * @param stemming
     */
    public void setStemming(boolean stemming) {
        this.toStem = stemming;
    }

    public String getPostingPath() {
        return postingPath;
    }

    /**
     * The method reads the corpus, separating the documents and send them to the Parse and the Indexer
     *
     * @throws IOException
     */
    public void ReadJsoup() throws Exception {
        p.insertStopWords(corpusPath + "\\stop_words.txt");
        String folderName;
        //create a new folder for stemming/not stemming
        if (toStem) {
            folderName = "WithStemming";
        } else {
            folderName = "WithoutStemming";
        }
        try {
            File dir = new File(postingPath + "\\" + folderName);
            if (!dir.exists())
                dir.mkdir();

        } catch (SecurityException se) {

        }

        //init the postingPath with\without stemming
        setPostingPath(postingPath + "\\" + folderName);

        File resource = new File(corpusPath);
        File[] Directories = resource.listFiles();
        for (File dir : Directories) {
            if (dir.getName().equals("stop_words.txt"))
                continue;
            files = dir.listFiles();

            for (File f : files) {
                System.out.println("file name: "+f.getName());
                ReadFile.countFiles++;
                Document doc = null;
                try {
                    doc = Jsoup.parse(f, "UTF-8");
                } catch (IOException e) {
                }
                String text = "";
                String serial = "";
                String city = "";
                String language = "";
                Elements docs = doc.select("DOC");

                for (Element element : docs) {
                    text = element.select("TEXT").text();
                    serial = element.select("DOCNO").text();
//                    if(serial.equals("FBIS3-14832")){
//                        System.out.println("FBIS3-14832 fileName= "+f.getName());
//                        return;
//                    }
                    text = replaceFromMap(text, this.replacements);
                    Docs curerntDoc = new Docs(serial, city, element.select("DATE1").text());
                    /////////////////////checkk
                    if (debug.contains(curerntDoc.getDocNo()))
                        System.out.println("docNum: "+curerntDoc.getDocNo()+"  fileName: "+f.getName());
                    String header = element.select("TI").text();
                    String headline = element.select("HEADLINE").text();
                    if (headline!= null && header!=null){
                        if (headline.length() >= header.length()){
                            curerntDoc.setHeader(headline);
                        }
                        else{
                            curerntDoc.setHeader(header);
                        }
                    }

                    String findTheCity = findCity(element.outerHtml());
                    for (int i = 0; i < findTheCity.length(); i++) {
                        if (Character.isDigit(findTheCity.charAt(i)) || findTheCity.length()<2 ||
                                findTheCity.equals("THE")|| findTheCity.equals("The")|| findTheCity.equals("by")
                                || findTheCity.equals("FOR")|| findTheCity.equals("--FOR")|| findTheCity.equals("--")) {
                            findTheCity = null;
                            break;
                        }
                    }
                    if (findTheCity != null) {
                        findTheCity = replaceFromMap(findTheCity, replacements);
                    }

                    curerntDoc.setCity(findTheCity);
                    String findTheLanguage = findLanguage(element.outerHtml());
                    for (int i = 0; i < findTheLanguage.length(); i++) {
                        if (Character.isDigit(findTheLanguage.charAt(i)) || findTheLanguage.charAt(findTheLanguage.length() - 1) == '-') {
                            findTheLanguage = null;
                            break;
                        }
                    }
                    if (findTheLanguage != null) {
                        findTheLanguage = replaceFromMap(findTheLanguage, replacements);
                    }


                    curerntDoc.setLanguage(findTheLanguage);

                    curerntDoc.setHeader(p.parser(curerntDoc, curerntDoc.getHeader(), toStem,false,true));
                    p.parser(curerntDoc, text, toStem,false,false);


                    indexer.add(p.getTempDictionary(), curerntDoc, ReadFile.countFiles, postingPath, toStem);
                    p.setTempDictionary(new HashSet<Terms>());

                }



            }

        }

        try {
            indexer.merge();
            indexer.writeTheDictionary();
        } catch (Exception e) {
        }


        //write the Dictionary as object
        File toWriteSortedAsObject = new File(postingPath + "\\" + "SortedAsObject.txt");
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(toWriteSortedAsObject));
        } catch (IOException e) {
        }
        try {
            oos.writeObject(indexer.getSorted());
            oos.close();
        } catch (Exception e) {
        }

        //write the next as object
        writeDocumentsAsObject();
        writeTermsInHeaderAsObject();
        writeDicToShowAsObject();
        writeCitiesAsObject();
        writeLanguagesAsObject();

        //write the Documents as object
//        File toWriteDocsObject = new File(postingPath + "\\" + "DocsAsObject.txt");
//        ObjectOutputStream oos1 = null;
//        try {
//            oos1 = new ObjectOutputStream(new FileOutputStream(toWriteDocsObject));
//        } catch (IOException e) {
//        }
//        try {
//            oos1.writeObject(indexer.getDocsHashMap());
//            oos1.close();
//        } catch (Exception e) {
//        }


        citiesIndexer.APIConnection();
        try {
            citiesIndexer.mergeTheCities(p.getCities());
        } catch (Exception e) {
        }

        //delete temporary files
        indexer.deleteTemporaryFiles(postingPath);

        //create docs posting
        createDocsPosting(indexer.getDocsHashMap());

    }

    private void writeLanguagesAsObject() {

        //write the Languages as object
        File toWriteSortedAsObject = new File(postingPath + "\\" + "LanguagesAsObject.txt");
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(toWriteSortedAsObject));
        } catch (IOException e) {
        }
        try {
            oos.writeObject(this.languages);
            oos.close();
        } catch (Exception e) {
        }


    }

    private void writeCitiesAsObject() {

        byte[] input = SerializationUtils.serialize(this.cities);
        byte[] encode = Base64.getEncoder().encode(input);
        try {
            FileUtils.writeByteArrayToFile(new File(postingPath + "\\" + "CitiesAsObject.txt"), encode);
        } catch (IOException e) {

        }


    }

    private void writeDocumentsAsObject() throws IOException {

        byte[] input = SerializationUtils.serialize(indexer.getDocsHashMap());
        byte[] encode = Base64.getEncoder().encode(input);
        FileUtils.writeByteArrayToFile(new File(postingPath + "\\" + "DocsAsObject.txt"), encode);


       // byte[] input = SerializationUtils
    }
    private void writeTermsInHeaderAsObject() throws IOException {

        byte[] input = SerializationUtils.serialize(Parse.getTermsInHeaderToDoc());
        byte[] encode = Base64.getEncoder().encode(input);
        FileUtils.writeByteArrayToFile(new File(postingPath + "\\" + "TermsInHeaderAsObject.txt"), encode);
        // byte[] input = SerializationUtils
    }

    private void writeDicToShowAsObject() throws IOException {
        //write the Dictionary as object
        File toWriteSortedAsObject = new File(postingPath + "\\" + "DicToShowAsObject.txt");
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(toWriteSortedAsObject));
        } catch (IOException e) {
        }
        try {
            oos.writeObject(indexer.getDicToShow());
            oos.close();
        } catch (Exception e) {
        }
    }


    /**
     * The method initialize the HashMap of the punctuation marks
     */
    private void intitialMap() {
        this.replacements.put(",", "");
        this.replacements.put("\'", "");
        this.replacements.put("]", "");
        this.replacements.put("[", "");
        this.replacements.put("}", "");
        this.replacements.put("{", "");
        this.replacements.put("!", "");
        this.replacements.put("?", "");
        this.replacements.put(":", "");
        this.replacements.put(";", "");
        this.replacements.put("\"", "");
        this.replacements.put("*", "");
        this.replacements.put(")", "");
        this.replacements.put("(", "");
        this.replacements.put(".", "");
        this.replacements.put("\n", " ");
//        /**/this.replacements.put(",","");
//        this.replacements.put(",","");*/
    }


    /**
     * The method replace the given string by the matched string from the HashSet
     *
     * @param string
     * @param replacements
     * @return
     */
    public static String replaceFromMap(String string, HashMap<String, String> replacements) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            int start = sb.indexOf(key, 0);
            while (start > -1) {
                int end = start + key.length();
                int nextSearchStart = start + value.length();
                sb.replace(start, end, value);
                start = sb.indexOf(key, nextSearchStart);
            }
        }
        return sb.toString();
    }

    /**
     * The method creates the posting of the documents in the corpus with the relevant information
     *
     * @param docs
     * @throws IOException
     */
    private void createDocsPosting(HashMap<String,Docs> docs) throws Exception {
        String dirPath = indexer.getPathDir();
        File f = new File(dirPath + "\\" + "DocsPosting.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
        }
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        Writer w = new BufferedWriter(osr);

        StringBuilder text = new StringBuilder("");
        Iterator it = docs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            Docs nextDoc = (Docs) pair.getValue();
            PriorityQueue<TermsPerDoc> docQueue = nextDoc.getMostFiveFrequencyEssences();
            StringBuilder FiveMostFreqEssences = new StringBuilder("");

            PriorityQueue<TermsPerDoc> newDocQueue = new PriorityQueue<>();
            while(!docQueue.isEmpty()){
                TermsPerDoc current = docQueue.poll();
                newDocQueue.add(current);
                FiveMostFreqEssences.append(current.getValue()+"-"+current.getTf()+", ");
            }
            nextDoc.setMostFiveFrequencyEssences(newDocQueue);



            if ((nextDoc.getCity() == null || nextDoc.getCity().equals("")) && (nextDoc.getDate() != null || nextDoc.getDate().equals(""))) {
                text.append(nextDoc.getDocNo() + ": DocLength="+nextDoc.getDocLength() + " maxtf=" + nextDoc.getMaxft() + ", uniqueWords=" +
                        nextDoc.getUniqueWords() + ", date:" + nextDoc.getDate()+", FiveMostFreqEssences:"+FiveMostFreqEssences);
                text.append(System.lineSeparator());
                try {
                    w.write(text.toString());
                    w.flush();
                } catch (Exception e) {
                }
                text = new StringBuilder("");
                continue;

            }
            if ((nextDoc.getCity() == null || nextDoc.getCity().equals("")) && (nextDoc.getDate().equals("") || nextDoc.getDate() == null)) {
                text.append(nextDoc.getDocNo() + ": DocLength="+nextDoc.getDocLength() + " maxtf=" + nextDoc.getMaxft() + ", uniqueWords=" +
                        nextDoc.getUniqueWords()+", FiveMostFreqEssences:"+FiveMostFreqEssences);
                text.append(System.lineSeparator());
                try {
                    w.write(text.toString());
                    w.flush();
                } catch (Exception e) {
                }
                text = new StringBuilder("");
                continue;

            }
            if ((nextDoc.getCity() != null || !nextDoc.getCity().equals("")) && (!nextDoc.getDate().equals("") || nextDoc.getDate() != null)) {
                text.append(nextDoc.getDocNo() + ": DocLength="+nextDoc.getDocLength() + " maxtf=" + nextDoc.getMaxft() + ", uniqueWords=" +
                        nextDoc.getUniqueWords() + ", city=" + nextDoc.getCity() + ", date:" + nextDoc.getDate()+", FiveMostFreqEssences:"+FiveMostFreqEssences);
                text.append(System.lineSeparator());
                try {
                    w.write(text.toString());
                    w.flush();
                } catch (Exception e) {
                }
                text = new StringBuilder("");
                continue;
            }
            if ((nextDoc.getCity() != null || !nextDoc.getCity().equals("")) && nextDoc.getDate() == null || nextDoc.getDate().equals("")) {
                text.append(nextDoc.getDocNo() + ": DocLength="+nextDoc.getDocLength() + " maxtf=" + nextDoc.getMaxft() + ", uniqueWords=" +
                        nextDoc.getUniqueWords() + ", city=" + nextDoc.getCity()+", FiveMostFreqEssences:"+FiveMostFreqEssences);
                text.append(System.lineSeparator());
                try {
                    w.write(text.toString());
                    w.flush();
                } catch (Exception e) {
                }
                text = new StringBuilder("");
                continue;
            }
            nextDoc.setMostFiveFrequencyEssences(newDocQueue);
        }

        w.close();
    }

    /**
     * The method returns the city from the given string of a specific doc
     *
     * @param text
     * @return
     */
    private String findCity(String text) {

        String city = "";
        String temp[];
        String[] splitedByLines = text.split("\n");
        for (int i = 0; i < splitedByLines.length; i++) {

            if (splitedByLines[i].equals(" <f p=\"104\">") || splitedByLines[i].equals("  <f p=\"104\">") || splitedByLines[i].equals("   <f p=\"104\">")) {

                temp = splitedByLines[i + 1].split(" ");
                for (int j = 0; j < temp.length; j++) {
                    if (!temp[j].equals("")) {
                        return temp[j];
                    }

                }

            }
        }

        return "";
    }

    /**
     * The method returns the language from the given string of a specific doc
     *
     * @param text
     * @return
     */
    private String findLanguage(String text) {

        String language = "";
        String temp[];
        String[] splitedByLines = text.split("\n");
        for (int i = 0; i < splitedByLines.length; i++) {
            if (splitedByLines[i].equals(" <f p=\"105\">")) {
                temp = splitedByLines[i + 1].split(" ");
                return temp[3];
            }
        }

        return "";
    }



}
