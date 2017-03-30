package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;

import javax.swing.*;

/**
 * Created by Richard Sundqvist on 26/03/2017.
 */
public class StudentFileViewerController {
    @FXML
    private Label fileLabel, groupLabel;
    @FXML
    private Pane contentPane;

    public void initialize (String group) {
    }

    public void onFeedback () {

    }

    public void onFeedbackLine () {

    }
}
