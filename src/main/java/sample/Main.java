package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.omg.CORBA.CODESET_INCOMPATIBLE;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Controller controller = new Controller();
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setTitle("Searching Engine");
        primaryStage.setScene(new Scene(root, 700, 500));
        

        primaryStage.show();
    }




    public static void main(String[] args) {
        launch(args);
    }
}
