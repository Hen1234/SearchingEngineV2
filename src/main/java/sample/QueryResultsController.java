package sample;

import Model.Indexer;
import Model.Searcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;

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
    public void initialize() {

        int randonID = (int)(Math.random()*100);
        data.getItems().add("*****QUERY ID: "+randonID+"*****"+System.lineSeparator());
        QueryResultsList = searcher.getQueryResults();
        for (int i = 0; i < QueryResultsList.size(); i++) {
            data.getItems().add(QueryResultsList.get(i) + System.lineSeparator());

        }

    }
}


