package gui;

import com.sun.javafx.application.LauncherImpl;
import gui.settings.Settings;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.IO;

import java.util.Optional;

public class Main extends Application {

  private static final double splashFadeDuration = 0.75;
  private Controller controller;
  private boolean runningFileSet;
  private Stage splashStage;

  public static void main(String[] args) {
    LauncherImpl.launchApplication(Main.class, Splash.class, args);
    // launch(args);
  }

  public void alertRunning() {
    System.err.println("Faulty controlfile detected: " + Tools.SETTINGS_FILE.getAbsolutePath());
    ButtonType bt1 = new ButtonType("Launch", ButtonBar.ButtonData.OK_DONE);

    Alert alert =
        new Alert(
            Alert.AlertType.WARNING,
            "Running two instances of CopyPasta at once may cause data loss. Press Launch to force start.",
            new ButtonType("Abort", ButtonBar.ButtonData.CANCEL_CLOSE),
            bt1);

    alert.setHeaderText("Another instance may be running");
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == bt1) {
      System.err.println("Continuing in spite of faulty controlfile.");
    } else {
      System.err.println("Shutting down: faulty controlfile.");
      System.exit(-1);
    }
  }

  @Override
  public void init() {
    runningFileSet = Settings.getRunningFile();
    Settings.loadSettingsFile();
    Tools.initializeWorkspaceFiles();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    IO.SUPPRESS_FXML = false;
    if (runningFileSet) alertRunning();
    if (Settings.STARTUP_VERSION_CHECK) UpdateView.checkUpdates(false);

    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
    Parent root = fxmlLoader.load();
    primaryStage.setTitle(
        "Copy Pasta \u00a9 Richard Sundqvist"
            + "          -          Workspace: \""
            + Tools.AUTO_SAVE_FEEDBACK_FILE.getParentFile().getAbsolutePath()
            + "\"");
    primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/icon.png")));

    Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
    double windowWidth = screenSize.getWidth() * .8;
    double windowHeight = screenSize.getHeight() * .8;
    Scene scene = new Scene(root, windowWidth, windowHeight);
    scene
        .getStylesheets()
        .add(Main.class.getResource("/highlighting.css").toExternalForm()); // Syntax highlighting
    primaryStage.setScene(scene);

    controller = fxmlLoader.getController();
    Settings.FIRST_RUN = false;
    Settings.putValue(Settings.first_run, Settings.FIRST_RUN + "");

    primaryStage.centerOnScreen();
    Settings.setRunningFile(true);

    primaryStage.show();

    splashStage.setAlwaysOnTop(true);
    FadeTransition fadeTransition =
        new FadeTransition(Duration.seconds(splashFadeDuration), splashStage.getScene().getRoot());
    fadeTransition.setOnFinished(event -> splashStage.close());
    fadeTransition.setFromValue(1);
    fadeTransition.setToValue(0);
    fadeTransition.playFromStart();
  }

  public void setSplashStage(Stage splashStage) {
    this.splashStage = splashStage;
  }

  public void stop() {
    controller.shutdown();
  }
}
