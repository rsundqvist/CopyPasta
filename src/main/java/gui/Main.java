package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Arrays;

public class Main extends Application {

    public static void main (String[] args) {
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Copy Pasta \u00a9 Richard Sundqvist");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon2.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .9;
        double windowHeight = screenSize.getHeight() * .9;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        primaryStage.setScene(scene);

        primaryStage.show();

        final Controller controller = fxmlLoader.getController();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(controller)));
    }

    public void shutdown (Controller controller) {
        controller.shutdown();
    }
}
