package gui;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import model.Feedback;

/**
 * Created by Richard Sundqvist on 19/02/2017.
 */
public class FeedbackTab extends Tab {
    public static final int MIN_TITLE_LENGTH = 4;

    private final Feedback feedback;
    private final TextArea textArea;

    public FeedbackTab (Feedback feedback) {
        this.feedback = feedback;
        textArea = new TextArea();
        textArea.setText(feedback.getContent());
        textArea.textProperty().addListener(event -> updateFeedback());
        setContent(textArea);
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
}
