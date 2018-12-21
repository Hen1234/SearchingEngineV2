package Model;

import javafx.util.Pair;
import okhttp3.Headers;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.*;

public class Indexer {
    // cache - contain the terms in memory and write them to disc when full
    private TreeMap<String, ArrayList<StringBuilder>> cache;
    // dfAndTotal - key => term value , value => pair of<df,total in corpus>
    private HashMap<String, Pair<Integer, Integer>> dfAndTotal;
    // all lines of big dictionary - terms and data about them
    private ArrayList<String> DicToShow;
    // the dictionary that hold - key - term value, value - "file No. in posting,line in posting"
    private HashMap<String, String> Dictionary; //String - term value , Integer - line number in posting file
    //Hashmap for replacing char to his numeric value by O(1)
    private HashMap<Character, Integer> replaceCharWithInt;
    // set of Documents holding all of the Documents in corpus
    private HashSet<Docs> documents;
    // sorted dictionary hold as key => the term, value=>file number in posting, pointer to exact line
    static TreeMap<String, String> sorted;
    // HashMap fot docs
    static HashMap<String, Docs> docsHashMap;


    int countTotalFiles; // counter for number of termpoal files of posting
    int toWriteidx; // index for current file to write to from cache
    int fileNum = 1; // files counter
    static int lineIdx; // line idx - help to point to specific line in file
    boolean[] isOk; // is the file in the i's place done or not
    int[] lineNumberArr; // which line to read from in every document
    File[] files; // files to read from and write to
    static String pathDir; // path the user insert in main window
    private String folderName; // name of the folder the user insert
    FileInputStream[] fis; // reading from temporal files
    InputStreamReader[] isr;// reading from temporal files
    BufferedReader[] br;// reading from temporal files
    ArrayList<FileOutputStream> fos; // writing to posting files
    ArrayList<OutputStreamWriter> osr;// writing to posting files
    ArrayList<BufferedWriter> bw;// writing to posting files
    ArrayList<File> toWrite; // writing to posting files
    ArrayList<File> toRead; // writing to posting files
    PriorityQueue<Pair<String, String>> mergeQueue; // merging temporal posting to final posting files


    //public HashMap<String, Integer> getDictionary() {
    //return Dictionary;

    int docCounter;

    /**
     * constractor
     *
     * @throws FileNotFoundException
     */
    public Indexer() {
        lineIdx = 1;
        sorted = new TreeMap<>();
        DicToShow = new ArrayList<String>();
        fos = new ArrayList<>();
        osr = new ArrayList<>();
        bw = new ArrayList<>();
        dfAndTotal = new HashMap<String, Pair<Integer, Integer>>();
        documents = new HashSet<Docs>();
        cache = new TreeMap<String, ArrayList<StringBuilder>>();    //(new Indexer.MyComperator());
        Dictionary = new HashMap<String, String>();  ///////**********add pointer to posting
        toWriteidx = 1;
        docCounter = 0;
        pathDir = "";
        folderName = "";
        replaceCharWithInt = new HashMap<Character, Integer>();
        countTotalFiles = 0;
        docsHashMap = new HashMap<String, Docs>();
        mergeQueue = new PriorityQueue<Pair<String, String>>();
        toRead = new ArrayList<File>();
        initReplaceCharWithInt();
    }

    /**
     * getter
     *
     * @return
     */
    public HashMap<String, String> getDictionary() {
        return Dictionary;
    }

    /**
     * getter
     *
     * @return
     */
    public HashSet<Docs> getDocuments() {
        return documents;
    }

    /**
     * getter
     *
     * @return
     */
    public ArrayList<String> getDicToShow() {
        return DicToShow;
    }

    /**
     * setter
     *
     * @param pathDir
     */
    public void setPathDir(String pathDir) {
        this.pathDir = pathDir;
    }

    /**
     * getter
     *
     * @return
     */
    public String getPathDir() {
        return pathDir;
    }

    /**
     * getter
     *
     * @return
     */
    public TreeMap<String, String> getSorted() {
        return sorted;
    }

    public HashMap<String, Docs> getDocsHashMap() {
        return docsHashMap;
    }


    /**
     * initialize hashMap of term and replacment
     */
    private void initReplaceCharWithInt() {

        replaceCharWithInt.put('a', 1);
        replaceCharWithInt.put('b', 2);
        replaceCharWithInt.put('c', 3);
        replaceCharWithInt.put('d', 4);
        replaceCharWithInt.put('e', 5);
        replaceCharWithInt.put('f', 6);
        replaceCharWithInt.put('g', 7);
        replaceCharWithInt.put('h', 8);
        replaceCharWithInt.put('i', 9);
        replaceCharWithInt.put('j', 10);
        replaceCharWithInt.put('k', 11);
        replaceCharWithInt.put('l', 12);
        replaceCharWithInt.put('m', 13);
        replaceCharWithInt.put('n', 14);
        replaceCharWithInt.put('o', 15);
        replaceCharWithInt.put('p', 16);
        replaceCharWithInt.put('q', 17);
        replaceCharWithInt.put('r', 18);
        replaceCharWithInt.put('s', 19);
        replaceCharWithInt.put('t', 20);
        replaceCharWithInt.put('u', 21);
        replaceCharWithInt.put('v', 22);
        replaceCharWithInt.put('w', 23);
        replaceCharWithInt.put('x', 24);
        replaceCharWithInt.put('y', 25);
        replaceCharWithInt.put('z', 26);

        replaceCharWithInt.put('A', 1);
        replaceCharWithInt.put('B', 2);
        replaceCharWithInt.put('C', 3);
        replaceCharWithInt.put('D', 4);
        replaceCharWithInt.put('E', 5);
        replaceCharWithInt.put('F', 6);
        replaceCharWithInt.put('G', 7);
        replaceCharWithInt.put('H', 8);
        replaceCharWithInt.put('I', 9);
        replaceCharWithInt.put('J', 10);
        replaceCharWithInt.put('K', 11);
        replaceCharWithInt.put('L', 12);
        replaceCharWithInt.put('M', 13);
        replaceCharWithInt.put('N', 14);
        replaceCharWithInt.put('O', 15);
        replaceCharWithInt.put('P', 16);
        replaceCharWithInt.put('Q', 17);
        replaceCharWithInt.put('R', 18);
        replaceCharWithInt.put('S', 19);
        replaceCharWithInt.put('T', 20);
        replaceCharWithInt.put('U', 21);
        replaceCharWithInt.put('V', 22);
        replaceCharWithInt.put('W', 23);
        replaceCharWithInt.put('X', 24);
        replaceCharWithInt.put('Y', 25);
        replaceCharWithInt.put('Z', 26);

    }

    /**
     * adding temporary term set to the Dictionary of terms and pointers
     *
     * @param tempDictionary dictionary of terms per one document
     * @param currentDoc     the current doc we drlivered the terms from
     * @param fileCount      count the files for empty the cache
     * @param postingPath    path for wrighting the posting files
     * @param isStemming     boolean which decide if to stem or not
     * @throws IOException
     */
    public void add(HashSet<Terms> tempDictionary, Docs currentDoc, int fileCount, String postingPath, boolean isStemming) {

        setPathDir(postingPath);
        //add the doc to the doc's hashset
        currentDoc.setUniqueWords(tempDictionary.size());
        documents.add(currentDoc);
        docsHashMap.put(currentDoc.getDocNo(), currentDoc);
        Iterator<Terms> iteratorDict = tempDictionary.iterator();

        //run the tempDic get from the parse
        while (iteratorDict.hasNext()) {
            Terms nextTerm = (Terms) iteratorDict.next();
            if (dfAndTotal.containsKey(nextTerm.getValue())) {
                dfAndTotal.put(nextTerm.getValue(), new Pair<Integer, Integer>((dfAndTotal.get(nextTerm.getValue()).getKey() + 1), dfAndTotal.get(nextTerm.getValue()).getValue() + nextTerm.getDocsAndAmount().get(currentDoc).getKey()));
            } else {
                dfAndTotal.put(nextTerm.getValue(), new Pair<Integer, Integer>(1, nextTerm.getDocsAndAmount().get(currentDoc).getKey()));
            }

            if (nextTerm.value.charAt(0) == ' ') {
                nextTerm.setValue(nextTerm.getValue().substring(1, nextTerm.getValue().length()));
            }
            if (Dictionary.containsKey(nextTerm.value)) { ////////////term in dictionary - (for uniqu values ans pointers (later))
                if (cache.containsKey(nextTerm.getValue())) {  //if the Term exist in the cache
                    cache.get(nextTerm.getValue()).add(new StringBuilder("doc:").append(new StringBuilder(currentDoc.getDocNo())).append("#").append(new StringBuilder(nextTerm.docsAndAmount.get(currentDoc).toString())).append(" ")); // add another doc and number of times
                    Integer count = nextTerm.getDocsAndAmount().get(currentDoc).getKey();
                    if (currentDoc.getMaxft() < count) {
                        currentDoc.setMaxft(count);
                    }
                } else {   //The Term does not exist in the cache
                    ArrayList<StringBuilder> value = new ArrayList<StringBuilder>();
                    value.add(new StringBuilder("doc:").append(new StringBuilder(currentDoc.getDocNo())).append("#").append(new StringBuilder(nextTerm.docsAndAmount.get(currentDoc).toString())).append(" "));
                    cache.put(nextTerm.value, value);
                    Integer count = nextTerm.getDocsAndAmount().get(currentDoc).getKey();
                    if (currentDoc.getMaxft() < count) {
                        currentDoc.setMaxft(count);
                    }
                }
            } else { // not in dictionaey

                Dictionary.put(nextTerm.value, null);
                //currentDoc.setUniqueWords(currentDoc.getUniqueWords() + 1);
                ArrayList<StringBuilder> value = new ArrayList<StringBuilder>();
                value.add(new StringBuilder("doc:").append(new StringBuilder(currentDoc.getDocNo())).append("#").append(new StringBuilder(nextTerm.docsAndAmount.get(currentDoc).toString())).append(" "));
                cache.put(nextTerm.value, value);
            }
        }
        if (ReadFile.countFiles > 100) {
            try {
                writeToFile();
            } catch (Exception e) {

            }

            ReadFile.countFiles = 0;
        }
    }

    /**
     * empty the cache after 100 files to posting files
     *
     * @throws IOException
     */
    public void writeToFile() throws IOException {
        countTotalFiles++;
        String num = "name1";
        File f = new File(pathDir + "\\" + num + countTotalFiles + ".txt");
        FileOutputStream fos;
        OutputStreamWriter osr;
        Writer w;
        try {
            fos = new FileOutputStream(f);
            osr = new OutputStreamWriter(fos);
            w = new BufferedWriter(osr);
        } catch (Exception e) {
            return;
        }

        Iterator it = cache.entrySet().iterator();
        StringBuilder text = new StringBuilder();
        while (it.hasNext()) {
            //Terms nextTerm = (Terms) it.next();
            //text.append(nextTerm.getValue());
            Map.Entry pair = (Map.Entry) it.next();
            text.append(pair.getKey().toString() + "###");
            for (int i = 0; i < ((ArrayList<StringBuilder>) pair.getValue()).size(); i++) {
                text.append(((ArrayList<StringBuilder>) pair.getValue()).get(i));
            }
            text.append(System.lineSeparator());
        }
        try {
            w.write(text.toString());
            w.flush();
        } catch (Exception e) {
            return;
        }
        toRead.add(f);
        //w.close();
        cache = new TreeMap<>();
        fileNum++;
        try {
            fos.close();
            osr.close();
            w.close();
        } catch (Exception e) {
            return;
        }

    }

    public void deleteTemporaryFiles(String path) {
        String finalPath = pathDir + "\\name1" + countTotalFiles + ".txt";
        String bigLetter = pathDir + "\\toWriteToBigLetter.txt";
        while (countTotalFiles > 0) {
            try {
                finalPath = pathDir + "\\name1" + countTotalFiles + ".txt";
                FileUtils.forceDelete(new File(finalPath));
            } catch (IOException e) {
                countTotalFiles--;
                continue;
            }
            countTotalFiles--;
        }
        try {
            FileUtils.forceDelete(new File(bigLetter));
        } catch (Exception e) {
            return;
        }
    }


/**
 * merge between temporary posting files and set the Sorted Dictionary
 *
 * @throws IOException
 */
    /**
     * merge between temporary posting files and set the Sorted Dictionary
     *
     * @throws IOException
     */
    public void merge() throws IOException { /// to update pointer from dictionary to final posting
        if (ReadFile.countFiles > 0) {
            try {
                writeToFile();
            } catch (IOException e) {

            }
        }
        toWriteidx = 1;
        int lineInFile = 0;
        int countDequeues = 0;
        fos = new ArrayList<>();
        osr = new ArrayList<>();
        bw = new ArrayList<>();
        toWrite = new ArrayList<>();
        isOk = new boolean[toRead.size() + 1];
        try {
            init();
        } catch (IOException e) {

        }

        mergeQueue = new PriorityQueue<Pair<String, String>>(toRead.size(), new MyComperator());
        Integer toReadIdx = 0;
        StringBuilder lineToWriteToFinalFile = new StringBuilder("");
        String fileToPoll = "";
        int countWritenLines = 0;
        String line = "";
        lineNumberArr = new int[toRead.size()];

        while (toReadIdx < toRead.size() - 1) { // intialize the Pqueue
            try {
                line = br[toReadIdx].readLine();
            } catch (IOException e) {
            }
            mergeQueue.add(new Pair<String, String>(line, toReadIdx.toString()));
            toReadIdx++;
        }

        Pair<String, String> curTerm = null;
        boolean isBigLetterInQueue = false;

        while (!mergeQueue.isEmpty()) {
            curTerm = mergeQueue.poll();
            String curTermKey = curTerm.getKey();
            countDequeues++;
            fileToPoll = curTerm.getValue(); // file number to insert from into the queue
            try {
                toReadIdx = Integer.parseInt(fileToPoll); // to integer
            } catch (Exception e) {
                continue;
            }

            if (toReadIdx == (toRead.size() - 1)) {
                isBigLetterInQueue = false;
            }
            lineNumberArr[toReadIdx]++;
            if (isOk[toReadIdx]) {
                try {
                    line = br[toReadIdx].readLine();
                } catch (IOException e) {

                }
                if (line != null)
                    mergeQueue.add(new Pair<String, String>(line, toReadIdx.toString()));
                else {
                    if (toReadIdx != toRead.size() - 1)
                        isOk[toReadIdx] = false;
                }
            }
            if (!isBigLetterInQueue) {
                try {
                    line = br[toRead.size() - 1].readLine();
                } catch (IOException e) {
                }
                if (line != null) {
                    mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                    isBigLetterInQueue = true;
                }
            }

            if (mergeQueue.size() > 0 && firstWordInLine(curTerm.getKey()).equals(firstWordInLine(mergeQueue.peek().getKey()))) {// equal terms in queue and outside
                if (Character.isUpperCase(curTerm.getKey().charAt(0))) { // upper case
                    // upper case appear in dictionary as lower case
                    if (Dictionary.containsKey(firstWordInLine(curTerm.getKey().toLowerCase()))) {
                        Dictionary.remove(firstWordInLine(curTerm.getKey()).toUpperCase());
                        StringBuilder toInsertToBigLetterFile = new StringBuilder(firstWordInLine(curTerm.getKey()).toLowerCase() + "###" + restOfInLine(curTerm.getKey()));
                        while (firstWordInLine(mergeQueue.peek().getKey()).equals(firstWordInLine(curTerm.getKey()))) {
                            curTerm = mergeQueue.poll();
                            countDequeues++;
                            fileToPoll = curTerm.getValue(); // file number to insert from into the queue
                            try {
                                toReadIdx = Integer.parseInt(fileToPoll); // to integer
                            } catch (Exception e) {
                                continue;
                            }

                            if (toReadIdx == toRead.size() - 1) {
                                isBigLetterInQueue = false;
                            }
                            toInsertToBigLetterFile.append(" " + restOfInLine(curTerm.getKey()));
                            if (isOk[toReadIdx]) {
                                lineNumberArr[toReadIdx]++;
                                try {
                                    line = br[toReadIdx].readLine();
                                } catch (IOException e) {

                                }
                                if (line != null) {
                                    mergeQueue.add(new Pair<String, String>(line, toReadIdx.toString()));
                                    if (toReadIdx == toRead.size() - 1)
                                        isBigLetterInQueue = true;

                                } else {
                                    isOk[toReadIdx] = false;
                                }
                            }
                        }
                        toInsertToBigLetterFile.append(System.lineSeparator());
                        try {
                            bw.get(0).write(toInsertToBigLetterFile.toString());
                            bw.get(0).flush();
                        } catch (Exception e) {
                        }

                        toInsertToBigLetterFile = new StringBuilder("");
                        if (!isBigLetterInQueue) {
                            try {
                                line = br[toRead.size() - 1].readLine();
                            } catch (IOException e) {
                            }
                            if (line != null) {
                                mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                                isBigLetterInQueue = true;
                            }
                        }
                        continue;

                    }
                    lineToWriteToFinalFile.append(firstWordInLine(curTerm.getKey()) + " " + restOfInLine(curTerm.getKey()));
                    sorted.put(firstWordInLine(curTerm.getKey()).toUpperCase(), Integer.toString(toWriteidx) + "," + Integer.toString(lineIdx));
                    // not exist in dictionary - sequance of equals big letters
                    while (mergeQueue.size() > 0 && firstWordInLine(mergeQueue.peek().getKey()).equals(firstWordInLine(curTerm.getKey()))) {
                        curTerm = mergeQueue.poll();
                        countDequeues++;
                        fileToPoll = curTerm.getValue(); // file number to insert from into the queue
                        try {
                            toReadIdx = Integer.parseInt(fileToPoll); // to integer
                        } catch (Exception e) {
                            continue;
                        }
                        if (toReadIdx == toRead.size() - 1) {
                            isBigLetterInQueue = false;
                        }
                        lineToWriteToFinalFile.append(" " + restOfInLine(curTerm.getKey()));
                        if (isOk[toReadIdx]) {
                            lineNumberArr[toReadIdx]++;
                            try {
                                line = br[toReadIdx].readLine();
                            } catch (IOException e) {
                            }
                            if (line != null) {
                                mergeQueue.add(new Pair<String, String>(line, toReadIdx.toString()));
                                if (toReadIdx == toRead.size() - 1)
                                    isBigLetterInQueue = true;

                            } else {
                                isOk[toReadIdx] = false;
                            }
                        }
                    }
                    //int[] totalAndDf = TotalAndDf1(lineToWriteToFinalFile.toString());
                    //lineToWriteToFinalFile.append("Total in Corpus: " + totalAndDf[0] + " Df: " + totalAndDf[1]);
                    try {
                        DicToShow.add(firstWordInLine(curTerm.getKey()) + " TIC: " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());//+" Total In Corpus:"+totalAndDf[0]);
                        lineToWriteToFinalFile.append("DF- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getKey() + " TIC- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());
                        lineToWriteToFinalFile.append(System.lineSeparator());
                    } catch (Exception e) {
                    }
                    if (!isBigLetterInQueue) {
                        try {
                            line = br[toRead.size() - 1].readLine();
                        } catch (IOException e) {
                        }
                        if (line != null) {
                            mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                            isBigLetterInQueue = true;
                        }
                    }

                    if (countWritenLines > 90000) {
                        lineIdx = 1;
                        toWriteidx++;
                        countWritenLines = 0;
                        try {
                            bw.get(bw.size() - 1).flush();
                            bw.get(bw.size() - 1).close();
                        } catch (Exception e) {
                        }
                        toWrite.add(new File(pathDir + "\\finalposting" + toWriteidx + ".txt"));
                        try {
                            fos.add(new FileOutputStream(toWrite.get(toWrite.size() - 1).getPath()));
                            osr.add(new OutputStreamWriter(fos.get(fos.size() - 1)));
                            bw.add(new BufferedWriter(osr.get(osr.size() - 1)));
                        } catch (Exception e) {
                        }

                    }
                    lineIdx++;

                    try {
                        if (Character.isUpperCase(firstWordInLine(curTermKey).charAt(0))) {
                            insertDocQueue(firstWordInLine(curTermKey), lineToWriteToFinalFile.toString());
                        }
                    } catch (Exception e) {
                    }
                    try {

                        bw.get(bw.size() - 1).write(lineToWriteToFinalFile.toString());
                        bw.get(bw.size() - 1).flush();
                    } catch (Exception e) {
                    }
                    countWritenLines++;
                    lineToWriteToFinalFile = new StringBuilder("");
                    continue;


                    // Lower case sequance
                } else {
                    lineToWriteToFinalFile.append(firstWordInLine(curTerm.getKey()) + " " + restOfInLine(curTerm.getKey())); //2nd part of line from queue
                    sorted.put(firstWordInLine(curTerm.getKey()).toLowerCase(), Integer.toString(toWriteidx) + "," + Integer.toString(lineIdx));
                    while (mergeQueue.size() > 0 && mergeQueue.size() > 0 && firstWordInLine(mergeQueue.peek().getKey()).equals(firstWordInLine(curTerm.getKey()))) {
                        curTerm = mergeQueue.poll(); // the equaled term
                        countDequeues++;

                        lineToWriteToFinalFile.append(restOfInLine(curTerm.getKey()));
                        fileToPoll = curTerm.getValue(); // file number to insert from into the queue
                        try {
                            toReadIdx = Integer.parseInt(fileToPoll); // to integer
                        } catch (Exception e) {
                            continue;
                        }
                        if (toReadIdx == toRead.size() - 1) {
                            isBigLetterInQueue = false;
                        }
                        //lineToWriteToFinalFile.append(restOfInLine(curTerm.getKey()));
                        if (isOk[toReadIdx]) {
                            lineNumberArr[toReadIdx]++;
                            try {
                                line = br[toReadIdx].readLine();
                            } catch (IOException e) {
                            }
                            if (line != null) {
                                mergeQueue.add(new Pair<String, String>(line, toReadIdx.toString()));
                                if (toReadIdx == toRead.size() - 1)
                                    isBigLetterInQueue = true;
                            } else {
                                isOk[toReadIdx] = false;
                            }
                        }
                    }
                    //int[] totalAndDf = TotalAndDf1(lineToWriteToFinalFile.toString());
                    //lineToWriteToFinalFile.append("Total in Corpus: " + totalAndDf[0] + " Df: " + totalAndDf[1]);
                    try {
                        DicToShow.add(firstWordInLine(curTerm.getKey()) + " TIC: " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());//+" Total In Corpus:"+totalAndDf[0]);
                        lineToWriteToFinalFile.append("DF- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getKey() + " TIC- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());
                        lineToWriteToFinalFile.append(System.lineSeparator());
                    } catch (Exception e) {
                    }
                    if (!isBigLetterInQueue) {
                        try {
                            line = br[toRead.size() - 1].readLine();
                        } catch (IOException e) {
                        }
                        if (line != null) {
                            mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                            isBigLetterInQueue = true;
                        }
                    }
                    if (countWritenLines > 90000) {
                        toWriteidx++;
                        lineIdx = 1;
                        countWritenLines = 0;
                        try {
                            bw.get(bw.size() - 1).flush();
                            bw.get(bw.size() - 1).close();
                        } catch (Exception e) {
                        }

                        try {
                            toWrite.add(new File(pathDir + "\\finalposting" + toWriteidx + ".txt"));
                            fos.add(new FileOutputStream(toWrite.get(toWrite.size() - 1).getPath()));
                            osr.add(new OutputStreamWriter(fos.get(fos.size() - 1)));
                            bw.add(new BufferedWriter(osr.get(osr.size() - 1)));
                        } catch (Exception e) {
                        }
                    }

                    try {
                        if (Character.isUpperCase(firstWordInLine(curTermKey).charAt(0))) {
                            insertDocQueue(firstWordInLine(curTermKey), lineToWriteToFinalFile.toString());
                        }
                    } catch (Exception e) {
                    }

                    try {
                        bw.get(bw.size() - 1).write(lineToWriteToFinalFile.toString());
                        bw.get(bw.size() - 1).flush();
                    } catch (Exception e) {
                    }
                    lineIdx++;
                    countWritenLines++;
                    lineToWriteToFinalFile = new StringBuilder("");
                    continue;
                }
            } else { /// not sequance of terms
                if (Character.isUpperCase(curTerm.getKey().charAt(0))) { // upper case term
                    // not sequance of big letters but appear in dictionary
                    if (Dictionary.containsKey(firstWordInLine(curTerm.getKey().toLowerCase()))) { // exist in dic
                        Dictionary.remove(firstWordInLine(curTerm.getKey()).toUpperCase());
                        StringBuilder toInsertToBigLetterFile = new StringBuilder(firstWordInLine(curTerm.getKey()).toLowerCase() + "###" + restOfInLine(curTerm.getKey() + System.lineSeparator()));
                        try {
                            bw.get(0).write(toInsertToBigLetterFile.toString());
                            bw.get(0).flush();
                        } catch (Exception e) {
                        }
                        if (!isBigLetterInQueue) {
                            try {
                                line = br[toRead.size() - 1].readLine();
                            } catch (IOException e) {
                            }
                            if (line != null) {
                                mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                                isBigLetterInQueue = true;
                            }
                        }
                        toInsertToBigLetterFile = new StringBuilder("");
                        continue;
                    } else { // big letter word not in sequance - not in dictionary
                        lineToWriteToFinalFile.append(firstWordInLine(curTerm.getKey()) + " " + restOfInLine(curTerm.getKey()));
                        //int[] totalAndDf = TotalAndDf1(lineToWriteToFinalFile.toString());
                        //lineToWriteToFinalFile.append("Total in Corpus: " + totalAndDf[0] + " Df: " + totalAndDf[1]);
                        try {
                            DicToShow.add(firstWordInLine(curTerm.getKey()) + " TIC: " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());//+" Total In Corpus:"+totalAndDf[0]);
                            lineToWriteToFinalFile.append("DF- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getKey() + " TIC- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());
                            lineToWriteToFinalFile.append(System.lineSeparator());
                        } catch (Exception e) {
                        }
                        sorted.put(firstWordInLine(curTerm.getKey()).toUpperCase(), Integer.toString(toWriteidx) + "," + Integer.toString(lineIdx));
                        if (!isBigLetterInQueue) {
                            try {
                                line = br[toRead.size() - 1].readLine();
                            } catch (IOException e) {
                            }
                            if (line != null) {
                                mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                                isBigLetterInQueue = true;
                            }
                        }
                    }
                    if (countWritenLines > 90000) {
                        toWriteidx++;
                        lineIdx = 1;
                        countWritenLines = 0;
                        try {
                            bw.get(bw.size() - 1).flush();
                            bw.get(bw.size() - 1).close();
                        } catch (Exception e) {
                        }
                        try {
                            toWrite.add(new File(pathDir + "\\finalposting" + toWriteidx + ".txt"));
                            fos.add(new FileOutputStream(toWrite.get(toWrite.size() - 1).getPath()));
                            osr.add(new OutputStreamWriter(fos.get(fos.size() - 1)));
                            bw.add(new BufferedWriter(osr.get(osr.size() - 1)));
                        } catch (Exception e) {
                        }

                    }

                    try {
                        if (Character.isUpperCase(firstWordInLine(curTermKey).charAt(0))) {
                            insertDocQueue(firstWordInLine(curTermKey), lineToWriteToFinalFile.toString());
                        }

                    } catch (Exception e) {
                    }


                    try {
                        bw.get(bw.size() - 1).write(lineToWriteToFinalFile.toString());
                        bw.get(bw.size() - 1).flush();
                    } catch (Exception e) {
                    }

                    lineIdx++;
                    countWritenLines++;
                    lineToWriteToFinalFile = new StringBuilder("");
                    continue;
                } else {
                    lineToWriteToFinalFile = lineToWriteToFinalFile.append(firstWordInLine(curTerm.getKey()) + " " + restOfInLine(curTerm.getKey()));
                    //int[] totalAndDf = TotalAndDf1(lineToWriteToFinalFile.toString());
                    //lineToWriteToFinalFile.append("Total in Corpus: " + totalAndDf[0] + " Df: " + totalAndDf[1]);
                    try {
                        lineToWriteToFinalFile.append("DF- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getKey() + " TIC- " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());
                        lineToWriteToFinalFile.append(System.lineSeparator());
                        DicToShow.add(firstWordInLine(curTerm.getKey()) + " TIC: " + dfAndTotal.get(firstWordInLine(curTerm.getKey())).getValue());//+" Total In Corpus:"+totalAndDf[0]);

                    } catch (Exception e) {
                    }
                    sorted.put(firstWordInLine(curTerm.getKey()).toLowerCase(), Integer.toString(toWriteidx) + "," + Integer.toString(lineIdx));
                    if (!isBigLetterInQueue) {
                        try {
                            line = br[toRead.size() - 1].readLine();
                        } catch (IOException e) {
                        }
                        if (line != null) {
                            mergeQueue.add(new Pair<String, String>(line, Integer.toString(toRead.size() - 1)));
                            isBigLetterInQueue = true;
                        }
                    }
                    if (countWritenLines > 90000) {
                        toWriteidx++;
                        lineIdx = 1;
                        countWritenLines = 0;
                        try {
                            bw.get(bw.size() - 1).flush();
                            bw.get(bw.size() - 1).close();
                        } catch (Exception e) {
                        }
                        try {
                            toWrite.add(new File(pathDir + "\\finalposting" + toWriteidx + ".txt"));
                            fos.add(new FileOutputStream(toWrite.get(toWrite.size() - 1).getPath()));
                            osr.add(new OutputStreamWriter(fos.get(fos.size() - 1)));
                            bw.add(new BufferedWriter(osr.get(osr.size() - 1)));
                        } catch (Exception e) {
                        }
                    }

                    try {
                        if (Character.isUpperCase(firstWordInLine(curTermKey).charAt(0))) {
                            insertDocQueue(firstWordInLine(curTermKey), lineToWriteToFinalFile.toString());
                        }
                    } catch (Exception e) {
                    }
                    try {

                        bw.get(bw.size() - 1).write(lineToWriteToFinalFile.toString());
                        bw.get(bw.size() - 1).flush();
                    } catch (Exception e) {
                    }
                    lineIdx++;
                    countWritenLines++;
                    lineToWriteToFinalFile = new StringBuilder("");
                    countWritenLines++;
                }
            }
        }
        try {
            initClose();
        } catch (Exception e) {
        }
    }

    /**
     * close all files and buffers
     *
     * @throws IOException
     */
    private void initClose() throws IOException {
        toRead = new ArrayList<>();
        try {
            for (int i = 0; i < fos.size(); i++) {
                fos.get(i).close();
            }
            for (int i = 0; i < osr.size(); i++) {
                osr.get(i).close();
            }
            for (int i = 0; i < bw.size(); i++) {
                bw.get(i).close();
            }
            for (int j = 0; j < fis.length; j++) {
                fis[j].close();
                isr[j].close();
                br[j].close();
            }
        } catch (Exception e) {
            return;
        }
    }

    /**
     * initialize the the arrays and feilds the merge uses
     *
     * @throws IOException
     */
    private void init() throws IOException {
        int name = 1;
        // initialize toWrite in 0 and toRead in length-1
        try {
            toWrite.add(new File(pathDir + "\\toWriteToBigLetter.txt"));
            toWrite.add(new File(pathDir + "\\finalposting" + "1" + ".txt"));
            toRead.add(toWrite.get(0));
        } catch (Exception e) {
        }
        try {
            fos.add(new FileOutputStream(toWrite.get(0).getPath()));
            osr.add(new OutputStreamWriter(fos.get(0)));
            bw.add(new BufferedWriter(osr.get(0)));

            fos.add(new FileOutputStream(toWrite.get(1).getPath()));
            osr.add(new OutputStreamWriter(fos.get(1)));
            bw.add(new BufferedWriter(osr.get(1)));

            fis = new FileInputStream[toRead.size()];
            isr = new InputStreamReader[toRead.size()];
            br = new BufferedReader[toRead.size()];

            for (int j = 0; j < fis.length; j++) {
                fis[j] = new FileInputStream(toRead.get(j).getPath());
                isr[j] = new InputStreamReader(fis[j]);
                br[j] = new BufferedReader(isr[j]);
            }
        } catch (Exception e) {
        }

        for (int j = 0; j < isOk.length; j++) {
            isOk[j] = true;
        }
    }


    /**
     * split between term and data about the term
     *
     * @param str line in the template of term - data about it
     * @return the term
     */
    static private String firstWordInLine(String str) { // return first word in line
        String[] splited = str.split("###");
        return splited[0];
    }


    /**
     * split between term and data about the term
     *
     * @param str line in the template of term - data about it
     * @return the data
     */
    static String restOfInLine(String str) { // return last word in line
        String[] splited = str.split("###");
        return splited[1];
    }


    /**
     * compare between two pair - by their alphabetic value of the key
     */
    static class MyComperator implements Comparator<Pair<String, String>> {
        @Override
        public int compare(Pair<String, String> pair1, Pair<String, String> pair2) {

            String str1 = (pair1.getKey());
            String str2 = (pair2.getKey());
            return str1.compareTo(str2);
        }
    }


    /**
     * write the Dictionary as a file
     *
     * @throws IOException
     */
    public void writeTheDictionary() throws IOException {
        try {
            FileOutputStream f = new FileOutputStream(new File(pathDir + "\\DicPost.txt"));
            OutputStreamWriter osr = new OutputStreamWriter(f);
            BufferedWriter bw = new BufferedWriter(osr);
            Iterator it = sorted.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String key = (String) pair.getKey() + " " + (String) pair.getValue();
                bw.write(key + System.lineSeparator());
                bw.flush();
            }
            f.close();
            osr.close();
            bw.close();
        } catch (Exception e) {
            return;
        }
    }

    private void insertDocQueue(String termValue, String line) {

        //line = doc:FBIS3-1844#1=659 doc:
        String docNo = "";
        String tfString = "";
        for (int i = 0; i < line.length(); i++) {

            if (line.charAt(i) == ':') {
                i++;
                while (line.charAt(i) != '#') {
                    docNo = docNo + line.charAt(i);
                    i++;
                }
                i++;
                while (line.charAt(i) != '=') {
                    tfString = tfString + line.charAt(i);
                    i++;
                }

                int tf = Integer.parseInt(tfString);
                docPriorityQueue(tf, docNo, termValue);


            }


        }


    }

    //
    private void docPriorityQueue(int tf, String docNo, String termVlue) {

        PriorityQueue<TermsPerDoc> currentQueue = docsHashMap.get(docNo).getMostFiveFrequencyEssences();
        //if the queue little than 5
        if (currentQueue.size() < 5) {
            currentQueue.add(new TermsPerDoc(tf, termVlue));
        } else {
            //if the current tf of the term is bigger than the minimum tf in the queue
            if (currentQueue.peek().getTf() < tf) {
                currentQueue.poll();
                currentQueue.add(new TermsPerDoc(tf, termVlue));
            }
        }
    }
}
