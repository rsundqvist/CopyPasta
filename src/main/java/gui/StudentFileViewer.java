package gui;

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */

import javafx.application.Application;
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

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */
public class StudentFileViewer extends Application  {
    private Stage stage;
    private StudentFileViewerController controller;

    /*
    public StudentFileViewer (String group){

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/studentView.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        stage = new Stage();
        stage.setTitle("Pasta Editor \u00a9 Richard Sundqvist");
        stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/icon2.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .8;
        double windowHeight = screenSize.getHeight() * .8;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        controller = fxmlLoader.getController();
        controller.initialize(group);
    }
    */

    @Override
    public void start (Stage primaryStage) throws Exception {
        stage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/studentView.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }

        //stage = new Stage();
        stage.setTitle("Pasta Editor \u00a9 Richard Sundqvist");
        stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/icon2.png")));

        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenSize.getWidth() * .8;
        double windowHeight = screenSize.getHeight() * .8;
        Scene scene = new Scene(root, windowWidth, windowHeight);
        stage.setScene(scene);
        //stage.initModality(Modality.APPLICATION_MODAL);

        controller = fxmlLoader.getController();
        controller.initialize("foo");
        stage.show();
    }
}

