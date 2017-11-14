package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.IO;

import java.awt.*;
import java.net.URL;

public class UpdateViewController {
  @FXML private Label currentVersionLabel, repoVersionLabel, upToDateLabel, updateLabel;
  @FXML private VBox waitIndicator;
  @FXML private TextArea changesTextArea;
  @FXML private CheckBox startupCheckCheckBox;
  @FXML private Button downloadButton, closeButton;

  private URL url;
  private Stage stage;

  public UpdateViewController() {}

  public void onClose() {
    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    // stage.close(); // does not fire event
  }

  public void onDownload() {
    if (!Tools.isDesktopSupported()) return;
    try {
      Desktop.getDesktop().browse(url.toURI());
    } catch (Exception e) {
      e.printStackTrace();
      IO.showExceptionAlert(e);
    }
  }

  public void initialize() {
    upToDateLabel.setVisible(false);
    updateLabel.setVisible(false);
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public void updateAvailable(String newVersion, String patchNotes) {
    waitIndicator.setVisible(false);
    updateLabel.setVisible(true);
    patchRetreived(newVersion, patchNotes);
  }

  public void upToDate(String newVersion, String patchNotes) {
    waitIndicator.setVisible(false);
    upToDateLabel.setVisible(true);
    patchRetreived(newVersion, patchNotes);
    downloadButton.setDefaultButton(false);
    closeButton.setDefaultButton(true);
  }

  public void patchRetreived(String newVersion, String patchNotes) {
    changesTextArea.setText(patchNotes);
    repoVersionLabel.setText(newVersion);
  }

  public void setCurrentVersion(String currentVersion) {
    currentVersionLabel.setText(currentVersion);
  }

  public boolean getCheckOnStartup() {
    return startupCheckCheckBox.isSelected();
  }

  public void setCheckOnStartup(boolean checkOnStartup) {
    startupCheckCheckBox.setSelected(checkOnStartup);
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
