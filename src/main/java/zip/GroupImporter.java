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

/** Created by Richard Sundqvist on 17/04/2017. */
public class GroupImporter implements GroupImporterController.GroupImporterListener {
  private final Stage stage;
  private final GroupImporterController controller;
  private final GroupImporterController.GroupImporterListener listener;

  public GroupImporter(
      FeedbackManager feedbackManager, GroupImporterController.GroupImporterListener listener) {
    this.listener = listener;

    stage = new Stage();
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/groupImporter.fxml"));
    GridPane root = null;
    try {
      root = fxmlLoader.load();
    } catch (Exception e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
    }

    stage.setTitle("Group Importer \u00a9 Richard Sundqvist");
    stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/img/icon.png")));
    Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
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
