package gui.feedback;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import gui.Tools;
import gui.settings.Settings;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import model.Feedback;
import model.IO;

import java.io.File;
import java.util.Map;

/** Created by Richard Sundqvist on 26/03/2017. */
public class FileViewController {
  private FileFeedbackListener listener;
  private Feedback feedback;
  @FXML private Label fileLabel = null;
  @FXML private TabPane sourceTabs = null;
  @FXML private Label copiedLabel = null;
  private FileTab currentFileTab = null;
  private boolean feedbackLine = true, feedbackColumn = false;
  private boolean editable = false;

  /**
   * Indent a String using Google-style java indentation.
   *
   * @param javaSource The source to format.
   * @return Formatted source.
   * @throws FormatterException If {@code javaSource} contains syntax errors.
   */
  public static String indent(String javaSource) throws FormatterException {
    switch (Settings.INDENTATION_STYLE) {
      case "google":
        javaSource = new Formatter().formatSource(javaSource);
        break;
      default:
        System.err.println(
            "Unknown indentation style: \""
                + Settings.INDENTATION_STYLE
                + "\". Using default (\"google\").");
        javaSource = new Formatter().formatSource(javaSource);
        break;
    }
    return javaSource;
  }

  @FXML
  private void initialize() {
    copiedLabel.setOpacity(0);
    sourceTabs
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            event -> {
              currentFileTab = (FileTab) sourceTabs.getSelectionModel().getSelectedItem();

              if (currentFileTab == null) fileLabel.setText("Drag and drop to add files!");
              else fileLabel.setText(currentFileTab.getText());
            });
  }

  public ObservableList getSourceTabs() {
    return sourceTabs.getTabs();
  }

  public void update() {
    sourceTabs.getTabs().clear();
    Map<String, String> files = feedback.getFiles();
    for (String key : files.keySet()) sourceTabs.getTabs().add(new FileTab(key, files.get(key)));
  }

  public void onFeedback() {
    if (currentFileTab != null) {
      int line = getCaretLine();
      int column = getCaretColumn();
      int pos = currentFileTab.getCaretPosition();
      listener.feedbackAt(currentFileTab.getText(), line, column, pos);
    }
  }

  public void toggleEditable(Event e) {
    if (!sourceTabs.getTabs().isEmpty()) {
      ToggleButton toggleButton = (ToggleButton) e.getSource();
      editable = toggleButton.isSelected();

      if (editable) {
        toggleButton.setText("Save");
        sourceTabs.getTabs().forEach(tab -> ((FileTab) tab).setEditable(true));
      } else {
        toggleButton.setText("Edit");
        sourceTabs
            .getTabs()
            .forEach(
                tab -> {
                  FileTab fileTab = (FileTab) tab;
                  feedback.addFile(fileTab.getText(), fileTab.getCodeAreaContent());
                  fileTab.setEditable(false);
                });
      }
    }
  }

  public int getCaretLine() {
    return feedbackLine ? currentFileTab.getCaretLine() : -1;
  }

  public int getCaretColumn() {
    return feedbackColumn ? currentFileTab.getCaretColumn() : -1;
  }

  public void toggleFeedbackLine(Event event) {
    feedbackLine = ((CheckBox) event.getSource()).isSelected();
  }

  public void toggleFeedbackColumn(Event event) {
    feedbackColumn = ((CheckBox) event.getSource()).isSelected();
  }

  public void onDragDropped(DragEvent event) {
    Dragboard db = event.getDragboard();
    boolean success = false;
    if (db.hasFiles()) {
      success = true;

      for (File file : db.getFiles()) {
        String content = IO.getFileAsString(file);
        addFile(file.getName(), content);
      }
    }
    event.setDropCompleted(success);
    event.consume();
  }

  public void onDragOver(DragEvent event) {
    Dragboard db = event.getDragboard();

    if (db.hasFiles()) event.acceptTransferModes(TransferMode.COPY);
    else event.consume();
  }

  public void onAdd() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(null);
    if (file != null) addFile(file.getName(), IO.getFileAsString(file));
  }

  public void onDelete() {
    if (currentFileTab != null) {
      feedback.removeFile(currentFileTab.getFileName());
      sourceTabs.getTabs().remove(currentFileTab);
    }
  }

  public void onIndent() {
    if (currentFileTab != null) {
      try {
        String fileName = currentFileTab.getFileName();
        String fileContent = feedback.getFiles().get(fileName);
        fileContent = indent(fileContent);
        feedback.getFiles().put(fileName, fileContent);
        currentFileTab.setContentText(fileContent);
      } catch (FormatterException e) {
        e.printStackTrace();
        IO.showExceptionAlert(e);
      }
    }
  }

  public void addFile(String fileName, String content) {
    feedback.addFile(fileName, content);
    FileTab fileTab = new FileTab(fileName, content);
    fileTab.setEditable(editable);
    sourceTabs.getTabs().add(fileTab);
  }

  public Pair<String, Integer> getCurrentFileAndCaretPos() {
    String file = currentFileTab == null ? null : currentFileTab.getText();
    int pos = currentFileTab.getCaretPosition();
    return new Pair(file, pos);
  }

  public void flashCopiedlabel() {
    Tools.flashNode(copiedLabel);
  }

  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
    update();
  }

  public void setListener(FileFeedbackListener listener) {
    this.listener = listener;
  }

  public interface FileFeedbackListener {
    /** Indicates the user wants enter feedback at the given position */
    void feedbackAt(String file, int caretLine, int caretColumn, int caretPosition);

    /** Insert content at the given position */
    void feedbackAt(String file, String content, int caretLine, int caretColumn, int caretPosition);
  }
}
