package wareHouse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        String css = this.getClass().getResource("login.css").toExternalForm();
        Scene scene = new Scene(root);
        Image icon = new Image("ico.png");
        stage.getIcons().add(icon);
        stage.setScene(scene);

        scene.getStylesheets().add(css);
        stage.setTitle("Warehouse");
        stage.setResizable(false);

        stage.centerOnScreen();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
