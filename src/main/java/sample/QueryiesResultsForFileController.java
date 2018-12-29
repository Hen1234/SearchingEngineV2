package sample;

import Model.Searcher;
import javafx.scene.control.ListView;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class QueryiesResultsForFileController {

    private ArrayList<String> QueryResultsForFileList;
    private TreeMap<String, ArrayList<String>> QueryIDandResultsForFile;  //TreeMap: key->queryID, value->queryResults
    public Searcher searcher = Controller.searcher;
    public TextArea QueryDocsResults;
    public ListView<String> data;


    /**
     * initialize method
     */
    public void initialize() {

        QueryIDandResultsForFile = searcher.getQueryIDandResultsForFile();
        //iterator fot the TreeMAP
        Iterator it= QueryIDandResultsForFile.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String keyQueryID = (String) pair.getKey();
            ArrayList<String> queryResults = (ArrayList<String>) pair.getValue();
            data.getItems().add("*****QUERY ID: " + keyQueryID + "*****" + System.lineSeparator());
            for (int i = 0; i < queryResults.size(); i++) {
                data.getItems().add(queryResults.get(i) + System.lineSeparator());
            }

        }

    }
}
