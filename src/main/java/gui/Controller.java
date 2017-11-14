package gui;

import gui.feedback.WorkspaceViewController;
import gui.pasta.PastaEditor;
import gui.pasta.PastaViewController;
import gui.settings.Settings;
import gui.settings.SettingsEditor;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Modality;
import javafx.util.Duration;
import model.Feedback;
import model.IO;
import model.Pasta;
import model.UniqueArrayList;
import zip.GroupImporter;
import zip.GroupImporterController;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class Controller implements PastaViewController.PastaControllerListener {

  private static final UniqueArrayList<String> recentWorkspaces = new UniqueArrayList();
  @FXML private PastaViewController pastaViewController = null;
  @FXML private WorkspaceViewController workspaceViewController = null;
  @FXML private Label savedLabel, lastSaveTimestampLabel, versionLabel;
  @FXML private Menu recentWorkspaceMenu;
  private Timeline autosaveTimeline = null;
  private boolean extremtFulLsng = false; // Todo proper import of About fxml.

  public void exit() {
    System.exit(0);
  }

  public void initialize() {
    if (extremtFulLsng) return;
    extremtFulLsng = true;

    pastaViewController.initialize(this);
    savedLabel.setOpacity(0);
    initTimeline(false);
    initRecentWorkspaces();

    if (Settings.FIRST_RUN) loadExample();
  }

  private void initRecentWorkspaces() {
    readRecentWorkspaces();
    for (int i = recentWorkspaces.size() - 1; i >= 0; i--) {
      File file = new File(recentWorkspaces.get(i));
      javafx.scene.control.MenuItem mi = new javafx.scene.control.MenuItem(file.getParent());

      if (i == recentWorkspaces.size() - 1) {
        javafx.scene.image.ImageView iw =
            GroupImporterController.NodeStatus.BLUE.getImageView(); // Mark current directory
        iw.setFitWidth(15);
        iw.setFitHeight(15);
        mi.setGraphic(iw);
      }

      mi.setOnAction(event -> switchWorkspace(mi.getText()));
      recentWorkspaceMenu.getItems().add(mi);
    }

    javafx.scene.control.MenuItem clearItem = new javafx.scene.control.MenuItem("Clear All");
    clearItem.setOnAction(
        event -> {
          recentWorkspaces.clear();
          recentWorkspaceMenu.setDisable(true);
        });
    recentWorkspaceMenu.getItems().add(new SeparatorMenuItem());
    recentWorkspaceMenu.getItems().add(clearItem);
  }

  private void readRecentWorkspaces() {
    String s[] = IO.getFileAsString(Tools.RECENT_WORKSPACES_FILE).split("\n");

    for (String str : s) {
      if (str != null && !str.isEmpty()) recentWorkspaces.add(str);
    }

    String current = Tools.AUTO_SAVE_TEMPLATE_FILE.getParentFile().getParent();
    recentWorkspaces.remove(current); // Remove to ensure it's last
    recentWorkspaces.add(current); // Add current

    if (recentWorkspaces.size() > 5)
      recentWorkspaces.remove(0); // Remove the first (oldest element)
  }

  private void printRecentWorkspaces() {
    StringBuilder sb = new StringBuilder();

    for (String s : recentWorkspaces) sb.append(s + "\n"); // Newest is first

    IO.printStringToFile(sb.toString(), Tools.RECENT_WORKSPACES_FILE);
  }

  public void initTimeline(boolean saveNow) {
    if (saveNow) save();

    autosaveTimeline = new Timeline(new KeyFrame(Duration.minutes(5), ae -> save()));
    autosaveTimeline.setCycleCount(Timeline.INDEFINITE);
    autosaveTimeline.play();
  }

  @Override
  public void select(Pasta pasta) {}

  public void onChangeWorkspace() {
    File file = IO.showDirectoryChooser(null);
    if (file == null || !file.isDirectory()) return;

    try {
      switchWorkspace(file.getCanonicalPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void selectFeedback() {
    workspaceViewController.selectView(0);
  }

  public void selectSetup() {
    workspaceViewController.selectView(1);
  }

  public void selectProgress() {
    workspaceViewController.selectView(2);
  }

  public void toggleAutoSave(Event event) {
    CheckMenuItem checkMenuItem = (CheckMenuItem) event.getSource();

    if (checkMenuItem.isSelected()) initTimeline(true);
    else if (autosaveTimeline != null) autosaveTimeline.stop();
  }

  public void save() {
    saveWorkspace();
    Tools.flashNode(savedLabel);

    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    lastSaveTimestampLabel.setText(
        "Saved at " + dateFormat.format(Calendar.getInstance().getTime()));
    FadeTransition ft = new FadeTransition(Duration.seconds(6), lastSaveTimestampLabel);
    ft.setFromValue(0);
    ft.setToValue(1.0);
    ft.play();
  }

  @Override
  public void quickInsert(Pasta pasta) {
    workspaceViewController.quickInsert(pasta);
  }

  public String getCurrentAssignment() {
    return workspaceViewController.getAssignment();
  }

  public void settings() {
    new SettingsEditor().showAndWait();
  }

  public void shutdown() {
    ProgressIndicator progressIndicator = new ProgressIndicator();

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.getButtonTypes().clear();
    alert.setTitle("Exiting");
    alert.getDialogPane().setExpandableContent(progressIndicator);
    alert.getDialogPane().setExpanded(true);
    // alert.initModality(Modality.WINDOW_MODAL);
    // alert.show();

    alert.setHeaderText("Saving workspace");
    saveWorkspace();
    progressIndicator.setProgress(0.5);
    alert.setHeaderText("Saving settings");
    Settings.loadFromProperties();
    progressIndicator.setProgress(0.6);
    Settings.storeStoreSettingsFile();
    progressIndicator.setProgress(0.8);
    printRecentWorkspaces();
    progressIndicator.setProgress(0.9);
    progressIndicator.setProgress(1);
    Settings.setRunningFile(false);

    alert.close();
  }

  public void saveWorkspace() {
    pastaViewController.save();
    workspaceViewController.save();
  }

  public void showRemaining() {
    workspaceViewController.showIncompleteFeedback();
  }

  public void onDefaultWorkspace() {
    switchWorkspace(Tools.DEFAULT_WORKSPACE.getAbsolutePath());
  }

  public void switchWorkspace(String workspace) {
    Settings.putValue(Settings.workspace_location, workspace);
    Settings.restartForSettingsEffect();
  }

  public void openPastaEditor() {
    UniqueArrayList<Pasta> pastaList = pastaViewController.getPastaList();

    List<Pasta> copy = Pasta.copy(pastaList);
    PastaEditor pastaEditor = new PastaEditor(copy, workspaceViewController.getAssignment());
    UniqueArrayList<Pasta> editorPastaList = pastaEditor.showAndWait();

    if (!editorPastaList.equals(pastaList)) {
      pastaList = editorPastaList;
      Alert alert =
          new Alert(
              Alert.AlertType.CONFIRMATION,
              "Replace current Pasta with editor Pasta?",
              ButtonType.YES,
              ButtonType.NO);
      alert.setHeaderText("Replace current pasta?");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.YES)
        pastaViewController.setPastaList(pastaList);
    }
  }

  public void openGroupImporter() {
    GroupImporter groupImporter = new GroupImporter();
    List<Feedback> feedbackList = groupImporter.showAndWait();

    if (!feedbackList.isEmpty()) {
      ButtonType bt1 = new ButtonType("Nothing");
      ButtonType bt2 = new ButtonType("Replace ALL groups");
      ButtonType bt3 = new ButtonType("Import new groups");

      Alert alert =
          new Alert(Alert.AlertType.CONFIRMATION, "What do you want to do?", bt1, bt2, bt3);
      alert.setHeaderText("Finish Import");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent())
        if (result.get() == bt2)
          workspaceViewController.importFeedback(feedbackList, true, true); // Replace all
        else if (result.get() == bt3)
          workspaceViewController.importFeedback(feedbackList, false, true); // Add new only
    }
  }

  public void about() {
    about_fxml();
  }

  public void about_fxml() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("About Program");
    alert.setHeaderText("About Copy Pasta");

    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/about.fxml"));
    fxmlLoader.setController(this);
    Parent root = null;
    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
      return;
    }
    versionLabel.setText(Tools.VERSION);
    alert.getDialogPane().setExpandableContent(root);
    alert.getDialogPane().setExpanded(true);
    alert.initModality(Modality.NONE);
    alert.showAndWait();
  }

  public void onMail() {
    Desktop desktop;
    if (Desktop.isDesktopSupported()
        && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
      try {
        desktop.mail(new URI("mailto:richard.sundqvist@live.se?subject=About%20CopyPasta"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void onRepo() {
    try {
      Desktop.getDesktop().browse(new URL("https://github.com/whisp91/CopyPasta").toURI());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void onLoadExample() {
    ButtonType bt = new ButtonType("Load Example");
    ButtonType bt2 = new ButtonType("Maybe Later");

    String contentText =
        "All current work (pasta, feedback, template) will be replaced! Are you sure?";
    Alert alert = new Alert(Alert.AlertType.WARNING, contentText, bt2, bt);
    alert.setHeaderText("Current work will be lost");

    Optional<ButtonType> result = alert.showAndWait();

    if (result.isPresent() && result.get() == bt) loadExample();
  }

  public void loadExample() {
    try {
      InputStream pasta = Controller.class.getResourceAsStream("/examples/pasta.json");
      InputStream template = Controller.class.getResourceAsStream("/examples/template.json");
      InputStream feedback = Controller.class.getResourceAsStream("/examples/feedback.json");

      pastaViewController.clearAllNoWarning();
      pastaViewController.importPasta(Arrays.asList(IO.extractPasta(pasta)));

      workspaceViewController.setFeedbackTemplate(IO.extractSingleFeedback(template));
      workspaceViewController.importFeedback(
          Arrays.asList(IO.extractFeedback(feedback)), true, false);
      selectSetup();

    } catch (Exception e) {
      System.err.println("Failed to load examples.");
      IO.showExceptionAlert(e);
    }
  }

  public void onUpdate() {
    UpdateView.checkUpdates(true);
  }

  public void importPasta() {
    pastaViewController.importPasta();
  }

  public void exportPasta() {
    pastaViewController.exportAllPasta();
  }

  public void clearPasta() {
    pastaViewController.clearAllPasta();
  }

  public void exportFeedback() {
    workspaceViewController.exportAllFeedback();
  }

  public void importFeedback() {
    workspaceViewController.importFeedback();
  }

  public void exportTemplate() {
    workspaceViewController.exportTemplate();
  }

  public void importTemplate() {
    workspaceViewController.importTemplate();
  }

  public void clearFeedback() {
    workspaceViewController.clear();
  }

  public void toggleFeedbackDone() {
    workspaceViewController.toggleDoneTab();
  }
}
