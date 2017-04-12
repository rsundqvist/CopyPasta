package gui.feedback;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import model.Feedback;

/**
 * Created by Richard Sundqvist on 19/02/2017.
 */
public class FeedbackTab extends Tab implements StudentFileViewerController.FileFeedbackListener {
    public static final int MIN_TITLE_LENGTH = 4;

    private final Feedback feedback;
    private final TextArea textArea;
    private final TabPane viewsPane;
    private final Tab feedbackView, fileView;
    private final StudentFileViewer studentFileViewer;

    public FeedbackTab (Feedback feedback) {
        this.feedback = feedback;
        textArea = new TextArea();
        textArea.setText(feedback.getContent());
        textArea.textProperty().addListener(event -> updateFeedback());

        viewsPane = new TabPane();

        feedbackView = new Tab("Student Feedback");
        feedbackView.setContent(textArea);
        feedbackView.setClosable(false);

        studentFileViewer = new StudentFileViewer(this, feedback);
        fileView = new Tab("Student Files");
        fileView.setContent(studentFileViewer);
        fileView.setClosable(false);

        viewsPane.getTabs().addAll(feedbackView, fileView);

        setContent(viewsPane);
        setClosable(true);
        updateTitle();
    }


    private void updateFeedback () {
        feedback.setContent(textArea.getText()); //TODO Too many calls?
    }

    public Feedback getFeedback () {
        feedback.setContent(textArea.getText());
        return feedback;
    }

    public void addFile (String fileName, String content) {
        studentFileViewer.addFile(fileName, content);
    }

    public String toString () {
        return getText();
    }


    public void setTitle (String title) {
        if (title == null || title.equals("")) {
            title = "<Unknown group>";
        }
        if (title.length() < MIN_TITLE_LENGTH) {
            StringBuilder sb = new StringBuilder(title);
            while (sb.length() <= MIN_TITLE_LENGTH) {
                sb.append(" ");
            }
            title = sb.toString();
        }
        setText(title);
    }

    public void updateTitle () {
        setTitle(feedback.getGroup() + (feedback.isDone() ? " \u2713" : ""));
    }

    @Override
    public void feedbackAt (String file, int caretLine, int caretColumn, int caretPosition) {
        int pos = feedback.getFilePosition(file);

        String caretInfo = caretString(caretLine, caretColumn);
        if (pos < 0) {
            textArea.appendText("\nIn \"" + file + "\" at " + caretInfo + ":  \n");
            textArea.positionCaret(Integer.MAX_VALUE);
        } else {
            String text = "\nAt " + caretInfo + ":  \n";
            textArea.insertText(pos, text);
            textArea.positionCaret(pos + text.length());
        }

        viewsPane.getSelectionModel().select(feedbackView);
    }

    private static String caretString (int caretLine, int caretColumn) {
        StringBuilder stringBuilder = new StringBuilder();

        if (caretLine != -1) {
            stringBuilder.append("L" + caretLine);
            if (caretColumn != -1)
                stringBuilder.append(", ");
        }
        if (caretColumn != -1)
            stringBuilder.append("C" + caretColumn);

        return stringBuilder.toString();
    }
}
