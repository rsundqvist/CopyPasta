package gui.feedback;

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import model.Feedback;
import model.IO;

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */
public class StudentFileViewer extends BorderPane {
    private StudentFileViewerController controller;

    public StudentFileViewer (StudentFileViewerController.FileFeedbackListener listener, Feedback feedback) {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/studentFileViewer.fxml"));
        fxmlLoader.setController(new StudentFileViewerController(listener, feedback));
        BorderPane root;
        try {
            root = fxmlLoader.load();
            setCenter(root);
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        maxWidth(-1);
        maxHeight(-1);
        setStyle("-fx-background: rgb(225, 228, 203);");
        // setStyle("-fx-background: #123456;");
        controller = fxmlLoader.getController();
    }

    public void addFile (String fileName, String content) {
        controller.addFile(fileName, content);
    }

    public StudentFileViewerController getController () {
        return controller;
    }
}
