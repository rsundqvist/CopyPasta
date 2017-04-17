package zip;

import gui.pasta.PastaEditor;
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
 * Created by Richard Sundqvist on 17/04/2017.
 */
public class GroupImporter {
    private final Stage stage;
    private final GroupImporterController controller;

    public GroupImporter () {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/groupImporter.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        stage = new Stage();
        stage.setTitle("Group Importer \u00a9 Richard Sundqvist");
        stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/icon2.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .8;
        double windowHeight = screenSize.getHeight() * .8;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        controller = fxmlLoader.getController();
        //controller.initialize(pastaList, assignment);
    }

    public String showAndWait () {
        stage.showAndWait();
        //return controller.getPastaList();
        return null;
    }
}
