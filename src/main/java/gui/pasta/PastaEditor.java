package gui.pasta;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.IO;
import model.ManagerListener;
import model.PastaManager;

/** Created by Richard Sundqvist on 20/02/2017. */
public class PastaEditor implements ManagerListener {
  private final Stage stage;
  private final PastaEditorController controller;
  private final ManagerListener listener;

  public PastaEditor(ManagerListener listener, PastaManager pastaManager, String assignment) {
    this.listener = listener;

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/pastaEditor.fxml"));
    SplitPane root = null;
    try {
      root = fxmlLoader.load();
    } catch (Exception e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
    }

    stage = new Stage();
    stage.setTitle("Pasta Editor \u00a9 Richard Sundqvist");
    stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/img/icon.png")));

    Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
    stage.setMinWidth(root.getMinWidth());
    stage.setMinHeight(root.getMinHeight());
    stage.setScene(scene);
    stage.initModality(Modality.APPLICATION_MODAL);

    controller = fxmlLoader.getController();
    controller.initialize(this, pastaManager, assignment);
    stage.showAndWait();
  }

  @Override
  public void close(boolean managerChanged) {
    stage.close();
    listener.close(managerChanged);
  }
}
