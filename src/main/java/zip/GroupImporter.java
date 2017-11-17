package zip;

import gui.pasta.PastaEditor;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.FeedbackManager;
import model.IO;
import model.ManagerListener;

/** Created by Richard Sundqvist on 17/04/2017. */
public class GroupImporter implements ManagerListener {
  private final Stage stage;
  private final GroupImporterController controller;
  private final ManagerListener listener;

  public GroupImporter(FeedbackManager feedbackManager, ManagerListener listener) {
    this.listener = listener;

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/groupImporter.fxml"));
    GridPane root = null;
    try {
      root = fxmlLoader.load();
    } catch (Exception e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
    }

    stage = new Stage();
    stage.setTitle("Group Importer \u00a9 Richard Sundqvist");
    stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/img/icon.png")));

    Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
    scene
        .getStylesheets()
        .add(
            GroupImporter.class
                .getResource("/highlighting.css")
                .toExternalForm()); // Syntax highlighting§
    stage.setMinWidth(root.getMinWidth());
    stage.setMinHeight(root.getMinHeight());
    stage.setScene(scene);
    stage.initModality(Modality.APPLICATION_MODAL);

    controller = fxmlLoader.getController();
    controller.initialize(feedbackManager);
    controller.setListener(this);
    stage.showAndWait();
  }

  @Override
  public void close(boolean managerChanged) {
    stage.close();
    listener.close(managerChanged);
  }
}
