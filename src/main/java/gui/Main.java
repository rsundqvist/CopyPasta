package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;

public class Main extends Application {

    public static void main (String[] args) {
        launch(args);
    }

    private Controller controller;

    @Override
    public void start (Stage primaryStage) throws Exception {
        checkRunning();
        Settings.loadSettingsFile();
        Tools.initializeWorkspaceFiles();
        if (Settings.STARTUP_VERSION_CHECK)
            Controller.checkUpdates(false);

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();

        primaryStage.setTitle("Copy Pasta \u00a9 Richard Sundqvist" + "          -          Workspace: \"" + Tools.AUTO_SAVE_FEEDBACK_FILE.getParentFile().getAbsolutePath() + "\"");
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/icon.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .9;
        double windowHeight = screenSize.getHeight() * .9;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        scene.getStylesheets().add(Main.class.getResource("/highlighting.css").toExternalForm()); // Syntax highlighting
        primaryStage.setScene(scene);

        primaryStage.show();
        controller = fxmlLoader.getController();

        Settings.FIRST_RUN = false;
        Settings.putValue(Settings.first_run, Settings.FIRST_RUN+"");
    }

    public static void checkRunning () {
        boolean isRunning = Settings.getRunningFile();

        if (isRunning) {
            System.err.println("Faulty controlfile detected: " + Tools.SETTINGS_FILE.getAbsolutePath());
            ButtonType bt1 = new ButtonType("I understand the risk. Start anyway.");

            Alert alert = new Alert(Alert.AlertType.WARNING, "Another instance of CopyPasta appears to be running." + " This is not recommended as it may cause data loss. If the program was not shut down properly, this message may be shown erroneously.", new ButtonType("Abort"), bt1);

            alert.setHeaderText("Another instance may be running");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == bt1) {
                System.err.println("Continuing in spite of faulty controlfile.");
            } else {
                System.err.println("Shutting down: faulty controlfile.");
                System.exit(-1);
            }
        }
        Settings.setRunningFile(true);
    }

    public void stop () {
        controller.shutdown();
    }
}
