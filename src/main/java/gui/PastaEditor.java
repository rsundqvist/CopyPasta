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
import model.Pasta;
import model.UniqueArrayList;

import java.util.List;

/**
 * Created by Richard Sundqvist on 20/02/2017.
 */
public class PastaEditor {
    private final Stage stage;
    private final PastaEditorController controller;

    public PastaEditor (List<Pasta> pastaList, String assignment) {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/pastaEditor.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        stage = new Stage();
        stage.setTitle("Pasta Editor \u00a9 Richard Sundqvist");
        stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/icon.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .8;
        double windowHeight = screenSize.getHeight() * .8;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        controller = fxmlLoader.getController();
        controller.initialize(pastaList, assignment);
    }

    public UniqueArrayList<Pasta> showAndWait () {
        stage.showAndWait();
        return controller.getPastaList();
    }
}
