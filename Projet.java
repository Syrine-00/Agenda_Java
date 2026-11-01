package projet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Projet extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/projet/agendaUI.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Passer la scène au contrôleur pour le dark mode
        controleur ctrl = loader.getController();
        ctrl.setScene(scene);

        primaryStage.setTitle("Agenda");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}