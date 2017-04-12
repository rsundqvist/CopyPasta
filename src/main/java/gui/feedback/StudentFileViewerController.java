package gui.feedback;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import model.Feedback;
import model.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */
public class StudentFileViewerController {
    @FXML
    private Label fileLabel;
    @FXML
    private TabPane sourceTabs;

    private FileTab currentFileTab = null;
    private boolean feedbackLine = true, feedbackColumn = true;
    private final FileFeedbackListener listener;
    private final Feedback feedback;

    public StudentFileViewerController (FileFeedbackListener listener, Feedback feedback) {
        this.listener = listener;
        this.feedback = feedback;
    }

    @FXML
    private void initialize () {
        sourceTabs.getSelectionModel().selectedItemProperty().addListener(event -> {
            System.out.println("");

            currentFileTab = (FileTab) sourceTabs.getSelectionModel().getSelectedItem();

            if (currentFileTab == null)
                fileLabel.setText("Drag and drop to add files!");
            else
                fileLabel.setText(currentFileTab.getText());
        });
    }

    public void onFeedback () {
        if (currentFileTab != null) {
            int line = feedbackLine ? currentFileTab.getCaretLine() : -1;
            int column = feedbackColumn ? currentFileTab.getCaretColumn() : -1;
            int pos = currentFileTab.getCaretPosition();
            listener.feedbackAt(currentFileTab.getText(), line, column, pos);
        }
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
            String filePath = null;

            for (File file : db.getFiles()) {
                filePath = file.getAbsolutePath();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                        stringBuilder.append(line + "\n");

                    addFile(file.getName(), stringBuilder.toString());
                } catch (Exception e) {
                    IO.showExceptionAlert(e);
                    e.printStackTrace();
                }
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
        if (currentFileTab!= null) {
            feedback.removeFile(currentFileTab.getFileName());
            sourceTabs.getTabs().remove(currentFileTab);
        }
    }

    public void addFile (String fileName, String content) {
        feedback.addFile(fileName, content);
        sourceTabs.getTabs().add(new FileTab(fileName, content));
    }

    public interface FileFeedbackListener {
        void feedbackAt (String file, int caretLine, int caretColumn, int caretPosition);
    }
}
