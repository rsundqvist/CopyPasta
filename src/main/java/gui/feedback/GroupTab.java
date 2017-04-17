package gui.feedback;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Pair;
import model.Feedback;
import model.Pasta;

/**
 * Created by Richard Sundqvist on 19/02/2017.
 */
public class GroupTab extends Tab implements StudentFileViewerController.FileFeedbackListener {
    public static final int MIN_TITLE_LENGTH = 4;

    private final Feedback feedback;
    private final FeedbackText feedbackText;
    private final TabPane viewsPane;
    private final Tab feedbackView, fileView;
    private final StudentFileViewer studentFileViewer;

    public GroupTab (Feedback feedback) {
        this.feedback = feedback;
        feedbackText = new FeedbackText(feedback);

        viewsPane = new TabPane();

        feedbackView = new Tab("Student Feedback");
        feedbackView.setContent(feedbackText);
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
        feedback.setContent(feedbackText.getText()); //TODO Too many calls?
    }

    public Feedback getFeedback () {
        feedback.setContent(feedbackText.getText());
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
        feedbackText.updateColor();
        setTitle(feedback.getGroup() + (feedback.isDone() ? " \u2713" : ""));
    }

    @Override
    public void feedbackAt (String file, int caretLine, int caretColumn, int caretPosition) {
        feedbackText.feedbackAt(file, caretLine, caretColumn, caretPosition);
        viewsPane.getSelectionModel().select(feedbackView);
    }

    public void feedbackAt (String file, String content, int caretLine, int caretColumn, int caretPosition) {
        feedbackText.feedbackAt(file, content, caretLine, caretColumn, caretPosition);
        viewsPane.getSelectionModel().select(feedbackView);
    }

    public void quickInsert (Pasta pasta) {
        Tab tab = viewsPane.getSelectionModel().getSelectedItem();
        if (tab == null || pasta == null) return;

        if (tab == feedbackView)
            feedbackText.insertText(pasta.getContent());
        else if (tab == fileView) {
            Pair<String, Integer> fileAndCaretPos = studentFileViewer.getController().getCurrentFileAndCaretPos();
            StudentFileViewerController ctrl = studentFileViewer.getController();

            feedbackText.feedbackAt(fileAndCaretPos.getKey(), pasta.getContent(),
                    ctrl.getCaretLine(), ctrl.getCaretColumn(), -1);
            viewsPane.getSelectionModel().select(feedbackView);
        }
    }
}
