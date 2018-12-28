package sample;


import Model.*;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Path;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.controlsfx.control.CheckComboBox;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class represent the Controller of the GUI
 */
public class Controller implements Initializable {

    static ReadFile reader;
    static Searcher searcher;
    public TreeMap<String, String> Dictionary;
    public HashMap<String, Docs> Documents;
    //public Stage stage;
    @FXML
    public boolean corpusPathIsNull;
    public boolean postingPathIsNull;
    public Button Run;
    public Button LoadCorpus;
    public Button SavePosting;
    public Button reset;
    public Button ShowDictionary;
    public Button LoadDictionary;
    public Button LoadQueryFile;
    public Button RunQuery;
    public TextField txt_fiedCorpus;
    public TextField txt_fiedPosting;
    public TextField txt_fiedQueries;
    public TextField txt_fiedInsertQuery;
    public CheckBox Stemming;
    public CheckBox FilterByCity;
    public CheckBox isSemantic;
    public ComboBox Languages;
    public Alert badPathAlert;
    //public ComboBox Cities;
    public CheckComboBox Cities;
    public String FirstPath;
    public String SecondPath;
    public String quertPathFromUser;
    public String pathFromUser;


    @Override
    public void initialize(URL location, ResourceBundle resources) {


        try {
            reader = new ReadFile();
        } catch (Exception e) {
        }
        FirstPath = "";
        SecondPath = "";
        corpusPathIsNull = true;
        postingPathIsNull = true;
        badPathAlert = new Alert(Alert.AlertType.ERROR,"Please insert Valid path",ButtonType.OK);
        searcher = new Searcher();
        Stemming.setSelected(false);
        reset.setDisable(true);
        ShowDictionary.setDisable(true);
        quertPathFromUser = "";
        FilterByCity.setSelected(false);
        RunQuery.setDisable(true);
        LoadQueryFile.setDisable(true);
        LoadDictionary.setDisable(true);
        Run.setDisable(true);


    }

    /**
     * The method is called while the user press the "Run" button.
     * it presents a message if the user did not fill the all the fields.
     * it operates the method "ReadJsoup" of the ReadFile object.
     * it calculates the time of the whole process.
     * it gets the languages from the ReadFile object and initialize the "combobox" of the languages.
     * it presents an information message of the process at the end of it.
     *
     * @param event
     * @throws Exception
     */
    public void run(ActionEvent event) throws Exception {

        if (txt_fiedCorpus.getText().isEmpty() || txt_fiedPosting.getText().isEmpty()) {

            showAlert("Message", "Error", "All the fields should be full");
        } else {
            final long startTime = System.nanoTime();
            try {
                reader.ReadJsoup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (FirstPath.equals(""))
                FirstPath = reader.getPostingPath();
            else {
                SecondPath = reader.getPostingPath();
            }
            HashSet<String> languages = reader.getLanguages();
            Iterator it = languages.iterator();

            Languages.setItems(FXCollections.observableArrayList(languages));
            Languages.setDisable(false);
            reset.setDisable(false);
            ShowDictionary.setDisable(false);
            LoadDictionary.setDisable(false);

            HashMap<String, City> cities = reader.getCities();
//            HashSet<String> citiesName = new HashSet<String>();
//            Iterator it2 = cities.entrySet().iterator();
//            while (it2.hasNext()) {
//                Map.Entry pair = (Map.Entry) it2.next();
//                String key = (String) pair.getKey();
//                citiesName.add(key);
//            }
//            for (String string: citiesName){
//                System.out.println(string);
//            }


            Cities.getItems().addAll(citiesObservableList(cities));
            Cities.setDisable(false);
            LoadQueryFile.setDisable(false);
            RunQuery.setDisable(false);

            StringBuilder data = new StringBuilder("Number of Documents: ");
            data.append(reader.getIndexer().getDocuments().size());
            data.append(System.lineSeparator());
            data.append("Number of Unique Terms: ");
            data.append(reader.getIndexer().getSorted().size());
            data.append(System.lineSeparator());
            data.append("Run Time: ");
            final long duration = System.nanoTime() - startTime;
            data.append(duration * (Math.pow(10, -9)));
            data.append(" seconds");
            data.append(System.lineSeparator());
            showAlert("Data Message", "Process Information", data.toString());

        }

    }

    /**
     * The method presents a message according to the arguments it gets
     *
     * @param message - the content of the message
     * @param title   - the title of the message
     * @param header  - the header of the message
     */
    private void showAlert(String message, String title, String header) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.show();
    }

    /**
     * The method is called while the user enters the path of the corpus and the
     * stop words list.
     * it sets the given path to the field of the ReadFile object.
     *
     * @param event
     * @throws IOException
     */
    public void corpusPath(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser dir = new DirectoryChooser();
        File corpusFromUser = dir.showDialog(stage);
        if (corpusFromUser != null) {
            //pathFromUser = corpusFromUser.getPath();
            reader.setCorpusPath(corpusFromUser.getPath());
            txt_fiedCorpus.setText(corpusFromUser.getPath());
            corpusPathIsNull = false;
            if (!postingPathIsNull)
                Run.setDisable(false);
        }

    }

    /**
     * The method is called while the user enters the path of the posting files
     * it sets the given path to the field of the ReadFile object.
     *
     * @param event
     * @throws IOException
     */
    public void postingPath(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        DirectoryChooser dir = new DirectoryChooser();
        File postingPathFromUser = dir.showDialog(stage);
        if (postingPathFromUser != null) {
            pathFromUser = postingPathFromUser.getPath();
            reader.setPostingPath(postingPathFromUser.getPath());
            txt_fiedPosting.setText(postingPathFromUser.getPath());
            postingPathIsNull = false;
            LoadDictionary.setDisable(false);
            if (!corpusPathIsNull) {
                Run.setDisable(false);
            }
        }

    }

    /**
     * The method is called while the user marks the option of "Stemming".
     * it sets the given choice to the field of the ReadFile object.
     *
     * @param event
     */
    public void stemming(ActionEvent event) {
        if (Stemming.isSelected()) {
            reader.setStemming(true);
        } else {
            reader.setStemming(false);
        }
    }

    /**
     * The method is called while the user press the "Reset" button.
     * it deletes the whole posting files and dictionary and reset the main memory of the process.
     *
     * @param event
     * @throws IOException
     */
    public void reset(ActionEvent event) throws IOException {
        String pathToDelete = reader.getPostingPath();
        //FileUtils.cleanDirectory(new File(pathToDelete));
        try {
            FileUtils.deleteDirectory(new File(FirstPath));
        } catch (IOException e) {
        }
        if (!SecondPath.equals(""))
            try {
                FileUtils.deleteDirectory(new File(SecondPath));
            } catch (IOException e) {
            }
        reader = new ReadFile();

    }

    /**
     * The method is called while the user press the "Show Dictionary" button.
     * it creats a new stage for the list of the words in the dictionary
     *
     * @param event
     */
    public void showDictionary(ActionEvent event) {

        try {
            if (reader.getIndexer().getSorted() == null || reader.getIndexer().getSorted().size() == 0) {
                loadDictionary();
            }
            Stage stage = new Stage();
            stage.setTitle("Dictionary");
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("ShowDic.fxml"));
            Scene scene = new Scene(root, 700, 500);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();


        } catch (Exception e) {   //////exception not found


        }
    }

    /**
     * The method is called while the user press the "Load Dictionary" button.
     * it loads the dictionary to the memory.
     *
     * @throws IOException
     */
    public void loadDictionary() throws IOException, ClassNotFoundException {

        String postpath = pathFromUser;
        //String postpath = reader.getPostingPath();
        FileInputStream f = null;
        try {
            f = new FileInputStream(new File(postpath + "\\" + "SortedAsObject.txt"));
            ObjectInputStream o = new ObjectInputStream(f);
            Dictionary = (TreeMap<String, String>) o.readObject();
            reader.getIndexer().setSorted(Dictionary);
            //searcher.setDictionary(Dictionary);
            o.close();
        }
        catch (Exception e){
            badPathAlert.show();
            return;
        }

        try {
            loadDocuments();
        }
        catch (Exception e){
            badPathAlert.show();
            return;
        }

        try {
            loadHeaders();
        }
        catch (Exception e){
            badPathAlert.show();
            return;
        }

        reader.getIndexer().setPathDir(postpath);
        ShowDictionary.setDisable(false);
        RunQuery.setDisable(false);
        LoadQueryFile.setDisable(false);

    }

    private void loadDocuments() throws IOException {
        String postpath = pathFromUser;
        /*if (Stemming.isSelected()) {
            postpath = pathFromUser + "\\WithStemming";
        } else {
            postpath = pathFromUser + "\\WithoutStemming";
        }*/
            byte[] encode = Files.readAllBytes(Paths.get(postpath + File.separator + "DocsAsObject.txt"));
            byte[] output = Base64.getMimeDecoder().decode(encode);
            Object out = SerializationUtils.deserialize(output);
            Documents = ((HashMap<String, Docs>) out);
            //searcher.setDocuments(Documents);
            reader.getIndexer().setDocsHashMap(Documents);
    }

    private void loadHeaders() throws IOException {
        String postpath = pathFromUser;
        /*if (Stemming.isSelected()) {
            postpath = pathFromUser + "\\WithStemming";
        } else {
            postpath = pathFromUser + "\\WithoutStemming";
        }*/

        byte[] encode= Files.readAllBytes(Paths.get(postpath+File.separator+"TermsInHeaderAsObject.txt"));
        byte[] output = Base64.getMimeDecoder().decode(encode);
        Object out = SerializationUtils.deserialize(output);
        HashMap temp = ((HashMap<String,HashSet<String>>)out);
        reader.getP().setTermsInHeaderToDoc(temp);
    }

    public void queriesPath(ActionEvent event) throws IOException {

        Stage stage = new Stage();
        DirectoryChooser dir = new DirectoryChooser();
        File queriesFromUser = dir.showDialog(stage);
        if (queriesFromUser != null) {
            //pathFromUser = corpusFromUser.getPath();
            //reader.setCorpusPath(corpusFromUser.getPath());
            txt_fiedQueries.setText(queriesFromUser.getPath());
            System.out.println(txt_fiedQueries);
            try {
                searcher.readQueriesFile(queriesFromUser.getPath());
            }
            catch (Exception e){
                badPathAlert.setContentText("Please choose a valid path for queries file");
                badPathAlert.show();
                return;
            }
        }
    }

    public void FilterByCity(ActionEvent event) {
        if (FilterByCity.isSelected()) {
            //ObservableList<
            ObservableList<String> list = Cities.getCheckModel().getCheckedItems();
            citiesFromFilter(list);
        }/* else {

        }*/
    }


    private ObservableList<String> citiesObservableList(HashMap<String, City> cities) {
        //ConcurrentHashMap<String, City> map = cities;
        ObservableList<String> citiesObservableList = FXCollections.observableArrayList();
        for (String key : cities.keySet()) {
            citiesObservableList.add(key);
        }
        return citiesObservableList;
    }

    private void citiesFromFilter(ObservableList<String> list) {

        HashSet<String> citiesHashSet = new HashSet<>();
        for(String key: list){
            citiesHashSet.add(key);
        }

        searcher.setCities(citiesHashSet);
    }

    public void getQueryFromUser () throws IOException {

        String query = txt_fiedInsertQuery.getText();
        searcher.pasreQuery(query);

        //new stage for the list of the ranked doc
        Stage stage = new Stage();
        stage.setTitle("Results");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("ShowQueryResults.fxml"));
        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

    }

    public void isSemantic(){

        if (isSemantic.isSelected()) {

            searcher.setSemantic(true);
        }

    }

}