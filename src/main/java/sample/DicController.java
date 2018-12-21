package sample;

import Model.Indexer;
import javafx.scene.control.ListView;

import java.awt.*;
import java.util.ArrayList;

/**
 * The class represents the controller of the dictionary
 */
public class DicController {

    private ArrayList<String> DicList;
    public Indexer indexer = Controller.reader.getIndexer();
    public TextArea dicToShow;
    public ListView<String> data;

    /**
     * initialize method
     */
    public void initialize(){
        DicList = indexer.getDicToShow();
        for (int i=0; i<DicList.size(); i++){
            data.getItems().add(DicList.get(i)+System.lineSeparator());
        }
    }
}
