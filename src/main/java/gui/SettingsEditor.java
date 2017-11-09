package gui;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.IO;

/**
 * Created by Richard Sundqvist on 20/02/2017.
 */
public class SettingsEditor {
    private final Stage stage;
    private final SettingsEditorController controller;

    public SettingsEditor () {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settingsEditor.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        stage = new Stage();
        stage.setTitle("Settings Editor \u00a9 Richard Sundqvist");
        stage.getIcons().add(new Image(SettingsEditor.class.getResourceAsStream("/icon2.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .6;
        double windowHeight = screenSize.getHeight() * .5;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        controller = fxmlLoader.getController();
        controller.initialize(stage);
    }

    public void showAndWait(){
        stage.showAndWait();;
    }
}
