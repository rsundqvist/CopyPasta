package gui.feedback;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.util.Pair;
import model.Feedback;
import model.Pasta;

public class GroupViewController implements FileViewController.FileFeedbackListener {
  public static final boolean SWITCH_TO_FEEDBACK_ON_QUICKINSERT = false;

  @FXML Tab feedbackTab, filesTab;
  @FXML TabPane viewsPane;

  @FXML
  FileViewController fileViewController; // Controller of an included file = fx:id + "Controller"

  private Feedback feedback;
  private FeedbackText feedbackText;

  public GroupViewController() {
    feedbackText = new FeedbackText();
  }

  public void updateFilesTabTitle() {
    filesTab.setText("Files (" + feedback.getFiles().size() + ")");
  }

  public void initialize() {
    feedbackTab.setContent(feedbackText);
    fileViewController.setListener(this);
  }

  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
    feedbackText.setFeedback(feedback);
    fileViewController.setFeedback(feedback);
    updateFilesTabTitle();
  }

  @Override
  public void feedbackAt(String file, int caretLine, int caretColumn, int caretPosition) {
    feedbackText.feedbackAt(file, caretLine, caretColumn, caretPosition);
    viewsPane.getSelectionModel().select(feedbackTab);
  }

  public void feedbackAt(
      String file, String content, int caretLine, int caretColumn, int caretPosition) {
    feedbackText.feedbackAt(file, content, caretLine, caretColumn, caretPosition);
    viewsPane.getSelectionModel().select(feedbackTab);
  }

  public void quickInsert(Pasta pasta) {
    Tab tab = viewsPane.getSelectionModel().getSelectedItem();
    if (tab == null || pasta == null) return;

    if (tab == feedbackTab) feedbackText.insertTextAtCaret(pasta.getContent());
    else if (tab == filesTab) {
      Pair<String, Integer> fileAndCaretPos = fileViewController.getCurrentFileAndCaretPos();

      feedbackText.feedbackAt(
          fileAndCaretPos.getKey(),
          pasta.getContent(),
          fileViewController.getCaretLine(),
          fileViewController.getCaretColumn(),
          -1);

      fileViewController.flashCopiedlabel();
      if (SWITCH_TO_FEEDBACK_ON_QUICKINSERT) viewsPane.getSelectionModel().select(feedbackTab);
    }
  }

  public void updateFeedbackTextColor() {
    feedbackText.updateColor();
  }

  public void updateFeedbackContent() {
    feedback.setContent(feedbackText.getText());
  }
}
