package Model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

/**
 * The class responsible for reading the corpus, separating the documents and send them to the Parse and the Indexer
 */
public class ReadFile {


    private Docs doc;
    private HashMap<String, String> replacements;

    File[] files;
    private ArrayList<String> words;
    static Parse p;
    private Indexer indexer;
    private CitiesIndexer citiesIndexer;
    static int countFiles;
    static int countDocs;
    private String corpusPath;
    private String stopWordsPath;
    private String postingPath;
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

    public HashMap<String, City> getCities() {
        return cities;
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
                System.out.println(f.getName());
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
                    text = replaceFromMap(text, this.replacements);
                    Docs curerntDoc = new Docs(serial, city, element.select("DATE1").text());
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
                    p.parser(curerntDoc, text, toStem,false);


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

        //write the Documents as object
        File toWriteDocsObject = new File(postingPath + "\\" + "DocsAsObject.txt");
        ObjectOutputStream oos1 = null;
        try {
            oos1 = new ObjectOutputStream(new FileOutputStream(toWriteDocsObject));
        } catch (IOException e) {
        }
        try {
            oos1.writeObject(indexer.getDocsHashMap());
            oos1.close();
        } catch (Exception e) {
        }


        citiesIndexer.APIConnection();
        try {
            citiesIndexer.mergeTheCities(p.getCities());
        } catch (Exception e) {
        }
        indexer.deleteTemporaryFiles(postingPath);
        createDocsPosting(indexer.getDocuments());

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
    private void createDocsPosting(HashSet<Docs> docs) throws Exception {
        String dirPath = indexer.getPathDir();
        File f = new File(dirPath + "\\" + "DocsPosting.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
        }
        OutputStreamWriter osr = new OutputStreamWriter(fos);
        Writer w = new BufferedWriter(osr);

        Iterator it = docs.iterator();
        StringBuilder text = new StringBuilder("");
        while (it.hasNext()) {

            Docs nextDoc = (Docs) it.next();
            PriorityQueue<TermsPerDoc> docQueue = nextDoc.getMostFiveFrequencyEssences();
            StringBuilder FiveMostFreqEssences = new StringBuilder("");
            while(!docQueue.isEmpty()){
                TermsPerDoc current = docQueue.poll();
                FiveMostFreqEssences.append(current.getValue()+"-"+current.getTf()+", ");
            }

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
