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

        QueryResultsList = searcher.getQueryResults();
        for (int i = 0; i < QueryResultsList.size(); i++) {
            data.getItems().add(QueryResultsList.get(i) + System.lineSeparator());
//        }
//
//        //init the QueryResults list
//        searcher.setQueryResults(new ArrayList<String>());

        }
        System.out.println("QueryResultsList size="+QueryResultsList.size());

//    public static class HBoxCell extends HBox {
//        Label label = new Label();
//        Button button = new Button();
//
//        HBoxCell(String labelText, String buttonText) {
//            super();
//
//            label.setText(labelText);
//            label.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(label, Priority.ALWAYS);
//
//            button.setText(buttonText);
//
//            this.getChildren().addAll(label, button);
//        }
//    }
//
//    public Parent createContent() {
//        BorderPane layout = new BorderPane();
//
//        List<HBoxCell> list = new ArrayList<>();
//        for (int i = 0; i < 12; i++) {
//            list.add(new HBoxCell("Item " + i, "Button " + i));
//        }
//
//        ListView<HBoxCell> listView = new ListView<HBoxCell>();
//        ObservableList<HBoxCell> myObservableList = FXCollections.observableList(list);
//        listView.setItems(myObservableList);
//
//        layout.setCenter(listView);
//
//        return layout;
//    }

//    public void OnClickEntities(ActionEvent event){
//
//        Button b = ((Button)event.getSource());
//        int i = Integer.parseInt(b.getId());
////        String docName = map_docIndex.get(i);
////        String [] array_Entity = indexer.getDict_docs().get(docName).getArr_entities();
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Entities");
//        alert.setHeaderText("There are the 5 strong Entities");
//        String allTheEntites = "";
////        for(String s: array_Entity)
////            allTheEntites = allTheEntites + "\n" + s;
//        alert.setContentText(allTheEntites);
//        alert.showAndWait();
//    }
//
//
//    private void DisplayDocs() {
//
//        QueryResultsList = searcher.getQueryResults();
//        for (int i = 0; i < QueryResultsList.size(); i++) {
//            data.getItems().add(QueryResultsList.get(i) + System.lineSeparator());
//            HBox hBox = new HBox();
//            hBox.resize(526, 267 / 4);
//            Button button1 = new javafx.scene.control.Button();
//
//
//            button1.setText("Show Entities");
//            button1.resize(61, 31);
//            button1.setId("" + i);
//            button1.setOnAction(this::OnClickEntities);
//            Label docName = new Label();
//            docName.setText("   " +QueryResultsList.get(i));
//            hBox.getChildren().add(button1);
//            //hBox.getChildren().add(docName);
//            //data.getItems().add(hBox);
//            i++;
//
//        }
//
//        //init the QueryResults list
//
//
////        listView_docs.getItems().clear();
////        int i = 0;
////        for(RankingObject r : ranker.getSorted_rankingobject()){
////            if(i>=50)
////                break;
////            HBox hBox = new HBox();
////            hBox.resize(526,267/4);
////            Button button = new Button();
////            button.setText("Show Entities");
////            button.resize(61,31);
////            button.setId(""+i);
////            button.setOnAction(this::OnClickEntities);
////            Label docName = new Label();
////            docName.setText("   " + r.getDOCNO());
////            map_docIndex.put(i,r.getDOCNO());
////            System.out.println(r.getDOCNO());
////            hBox.getChildren().add(button);
////            hBox.getChildren().add(docName);
////            listView_docs.getItems().add(hBox);
////            i++;
////        }
//
////    public void setList(){
////
////        Label label = new Label();
////        Button button = new Button();
////
////
////        label.setText("Show Entities");
////        label.setMaxWidth(Double.MAX_VALUE);
////        HBox.setHgrow(label, Priority.ALWAYS);
////
////        button.setText(buttonText);
////
////        this.getChildren().addAll(label, button);
////        }
////
////    }
    }
}


