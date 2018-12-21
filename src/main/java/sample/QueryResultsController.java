package sample;

import Model.Indexer;
import Model.Searcher;
import javafx.scene.control.ListView;

import java.awt.*;
import java.util.ArrayList;

public class QueryResultsController {

    private ArrayList<String> QueryResultsList;
    public Searcher searcher = Controller.searcher;
    public TextArea QueryDocsResults;
    public ListView<String> data;

    /**
     * initialize method
     */
    public void initialize(){
        QueryResultsList = searcher.getQueryResults();
        for (int i=0; i<QueryResultsList.size(); i++){
            data.getItems().add(QueryResultsList.get(i)+System.lineSeparator());
        }

        //init the QueryResults list
        searcher.setQueryResults(new ArrayList<String>());
    }
}


