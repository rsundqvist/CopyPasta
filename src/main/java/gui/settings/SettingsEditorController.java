package gui.settings;

import gui.Tools;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Map;

/** Created by Richard Sundqvist on 20/02/2017. */
public class SettingsEditorController {
  @FXML private GridPane optionsGrid;
  @FXML private Label fileLabel;

  private Stage stage;

  public void initialize(Stage stage) {
    this.stage = stage;

    fileLabel.setText(Tools.SETTINGS_FILE.getAbsolutePath());

    int row = 1;
    Map<String, String[]> about = Settings.getAbout();
    ArrayList<String> keySet = new ArrayList<>(about.keySet());
    keySet.sort(String::compareToIgnoreCase); // Alphabetic order
    for (String key : keySet) {
      String[] s = about.get(key);
      Label optionlabel = new Label(s[Settings.OPTION_INDEX]);
      TextFlow aboutText = new TextFlow(new Text(s[Settings.ABOUT_INDEX]));
      Label typeLabel = new Label(s[Settings.TYPE_INDEX]);
      aboutText.setStyle("-fx-font-style1: italic;");
      typeLabel.setStyle("-fx-font-style1: italic; -fox-font-weight: bold;");
      optionsGrid.addRow(
          row++,
          optionlabel,
          aboutText,
          new Label(key), // The value key
          typeLabel,
          new TextField(Settings.properties.getProperty(key)) // The value in file
          );
    }
  }

  public void onSave() {
    int row = 1;
    int valueCol = 4;
    int numCols = 5;

    ArrayList<String> keySet = new ArrayList<>(Settings.getAbout().keySet());
    keySet.sort(String::compareToIgnoreCase); // Alphabetic order
    for (String key : keySet) {
      int i = row * numCols + valueCol;
      String value = ((TextField) (optionsGrid.getChildren().get(i))).getText();
      Settings.putValue(key, value);
      row++;
    }
    Settings.restartForSettingsEffect();
    stage.close();
  }

  public void onDiscard() {
    stage.close();
  }
}
