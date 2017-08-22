package gui.feedback;

import gui.Tools;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Pair;
import model.Feedback;
import model.IO;

import java.io.File;
import java.util.Map;

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */
public class StudentFileViewerController {
    @FXML
    private Label fileLabel = null;
    @FXML
    private TabPane sourceTabs = null;
    @FXML
    private Label copiedLabel = null;

    private FileTab currentFileTab = null;
    private boolean feedbackLine = true, feedbackColumn = false;
    private final FileFeedbackListener listener;
    private final Feedback feedback;
    private boolean editable = false;

    public StudentFileViewerController (FileFeedbackListener listener, Feedback feedback) {
        this.listener = listener;
        this.feedback = feedback;
    }

    @FXML
    private void initialize () {
        copiedLabel.setOpacity(0);
        sourceTabs.getSelectionModel().selectedItemProperty().addListener(event -> {
            currentFileTab = (FileTab) sourceTabs.getSelectionModel().getSelectedItem();

            if (currentFileTab == null)
                fileLabel.setText("Drag and drop to add files!");
            else
                fileLabel.setText(currentFileTab.getText());
        });

        Map<String, String> files = feedback.getFiles();
        for (String key : files.keySet())
            sourceTabs.getTabs().add(new FileTab(key, files.get(key)));
    }

    public void onFeedback () {
        if (currentFileTab != null) {
            int line = getCaretLine();
            int column = getCaretColumn();
            int pos = currentFileTab.getCaretPosition();
            listener.feedbackAt(currentFileTab.getText(), line, column, pos);
        }
    }

    public void toggleEditable (Event e) {

        if (!sourceTabs.getTabs().isEmpty()) {
            ToggleButton toggleButton = (ToggleButton) e.getSource();
            editable = toggleButton.isSelected();

            if (editable) {
                toggleButton.setText("Save");
                sourceTabs.getTabs().forEach(tab -> ((FileTab) tab).setEditable(editable));
            } else {
                toggleButton.setText("Edit");
                sourceTabs.getTabs().forEach(tab -> {
                    FileTab fileTab = (FileTab) tab;
                    feedback.addFile(fileTab.getText(), fileTab.getCodeAreaContent());
                });
            }
        }
    }

    public int getCaretLine () {
        return feedbackLine ? currentFileTab.getCaretLine() : -1;
    }

    public int getCaretColumn () {
        return feedbackColumn ? currentFileTab.getCaretColumn() : -1;
    }

    public void toggleFeedbackLine (Event event) {
        feedbackLine = ((CheckBox) event.getSource()).isSelected();
    }

    public void toggleFeedbackColumn (Event event) {
        feedbackColumn = ((CheckBox) event.getSource()).isSelected();
    }

    public void onDragDropped (DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;

            for (File file : db.getFiles()) {
                String content = IO.extractContent(file);
                addFile(file.getName(), content);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    public void onDragOver (DragEvent event) {
        Dragboard db = event.getDragboard();

        if (db.hasFiles())
            event.acceptTransferModes(TransferMode.COPY);
        else
            event.consume();
    }

    public void onDelete () {
        if (currentFileTab != null) {
            feedback.removeFile(currentFileTab.getFileName());
            sourceTabs.getTabs().remove(currentFileTab);
        }
    }

    public void addFile (String fileName, String content) {
        feedback.addFile(fileName, content);
        FileTab fileTab = new FileTab(fileName, content);
        fileTab.setEditable(editable);
        sourceTabs.getTabs().add(fileTab);
    }

    public Pair<String, Integer> getCurrentFileAndCaretPos () {
        String file = currentFileTab == null ? null : currentFileTab.getText();
        int pos = currentFileTab.getCaretPosition();
        return new Pair(file, pos);
    }

    public FileTab getCurrentFileTab () {
        return currentFileTab;
    }

    public interface FileFeedbackListener {
        void feedbackAt (String file, int caretLine, int caretColumn, int caretPosition);

        void feedbackAt (String file, String content, int caretLine, int caretColumn, int caretPosition);
    }

    public void flashCopiedlabel () {
        Tools.flashNode(copiedLabel);
    }

}
