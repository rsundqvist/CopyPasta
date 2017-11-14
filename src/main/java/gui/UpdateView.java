package gui;

import gui.pasta.PastaEditor;
import gui.settings.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateView {
  private static final URL url = getUrl();

  private static URL getUrl() {
    try {
      return new URL("https://raw.githubusercontent.com/whisp91/CopyPasta/master/VERSION");
    } catch (MalformedURLException e) {
      e.printStackTrace(); // Should never happen.
    }
    return null;
  }

  private final Stage stage;
  private final UpdateViewController controller;

  public UpdateView() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/updateView.fxml"));
    BorderPane root = null;
    try {
      root = fxmlLoader.load();
    } catch (Exception e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
    }

    stage = new Stage();
    stage.setTitle("CopyPasta \u00a9 Richard Sundqvist");
    stage.getIcons().add(new Image(PastaEditor.class.getResourceAsStream("/img/icon.png")));

    Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
    stage.setScene(scene);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setResizable(false);
    stage.setOnCloseRequest(event -> onCloseRequest());

    controller = fxmlLoader.getController();
    controller.setCurrentVersion(Tools.VERSION);
    controller.setUrl(url);
    controller.setStage(stage);
    controller.setCheckOnStartup(Settings.STARTUP_VERSION_CHECK);
  }

  public void onCloseRequest() {
    Settings.STARTUP_VERSION_CHECK = controller.getCheckOnStartup();
    Settings.putValue(Settings.startup_version_check, "" + Settings.STARTUP_VERSION_CHECK);
  }

  public static void checkUpdates(boolean showOnFalse) {
    UpdateView updateView = null;

    if (showOnFalse) {
      updateView = new UpdateView();
      updateView.stage.show();
    }

    // Check for new version
    List<String> versionLines = null;
    boolean isNewer = false, forceShow = false;
    String repoVersion = "?", patchNotes = "You're not supposed to see this string.";
    try {
      versionLines = getVersionLines();
      repoVersion = versionLines.get(0);
      isNewer = Tools.isNewer(repoVersion);
    } catch (Exception e) {
      e.printStackTrace();
      patchNotes = "Failed to check version: " + e.getMessage();
      forceShow = true;
    }

    if (!forceShow && !isNewer && !showOnFalse) return;

    if (updateView == null) {
      updateView = new UpdateView();
    }

    // Update the view
    if (forceShow) {
      updateView.controller.patchRetreived("Failed to connect to repo.", patchNotes);
    } else {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < versionLines.size(); i++) sb.append(versionLines.get(i) + "\n");
      patchNotes = sb.toString();

      if (isNewer) {
        updateView.controller.updateAvailable(repoVersion, patchNotes);
      } else {
        updateView.controller.upToDate(repoVersion, patchNotes);
      }
    }
    if (!updateView.stage.isShowing()) updateView.stage.showAndWait();
  }

  private static List<String> getVersionLines() throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
    List<String> versionLines = new ArrayList<>();
    String inputLine;
    while ((inputLine = in.readLine()) != null) versionLines.add(inputLine);
    return versionLines;
  }
}
