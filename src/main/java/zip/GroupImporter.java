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
import model.Feedback;
import model.IO;

import java.util.List;

/**
 * Created by Richard Sundqvist on 17/04/2017.
 */
public class GroupImporter {
    private final Stage stage;
    private final GroupImporterController controller;

    public GroupImporter () {

        stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/groupImporter.fxml"));
        controller = new GroupImporterController((stage));
        fxmlLoader.setController(controller);
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        stage.setTitle("Group Importer \u00a9 Richard Sundqvist");
        stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/icon.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .8;
        double windowHeight = screenSize.getHeight() * .8;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        // controller.initialize(pastaList, assignment);
    }

    public List<Feedback> showAndWait () {
        stage.showAndWait();
        controller.saveFilePatterns();
        return controller.getFeedback();
    }
}
