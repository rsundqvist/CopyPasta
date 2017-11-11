package gui.feedback;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import model.Feedback;
import model.IO;
import model.Pasta;

public class GroupView extends Tab {
  public static final int MIN_TITLE_LENGTH = 6;
  private final Feedback feedback;

  private final GroupViewController controller;

  public GroupView(Feedback feedback) {
    this.feedback = feedback;
    updateTabText();

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/groupView.fxml"));
    try {
      setContent(fxmlLoader.load());
    } catch (Exception e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
    }
    controller = fxmlLoader.getController();
    controller.setFeedback(feedback);
  }

  public void setTitle(String title) {
    if (title == null || title.isEmpty()) title = "<Unknown group>";
    else if (title.length() < MIN_TITLE_LENGTH)
      title += new String(new char[MIN_TITLE_LENGTH - title.length()]).replace("\0", "0");

    setText(title);
  }

  public String toString() {
    return getText();
  }

  public void updateTabText() {
    setText(feedback.getGroup());
  }

  public void updateTitle() {
    controller.updateFeedbackTextColor();
    setTitle(feedback.getGroup() + (feedback.isDone() ? " \u2713" : ""));
  }

  public Feedback getFeedback() {
    controller.updateFeedbackContent();
    return feedback;
  }

  public void quickInsert(Pasta pasta) {
    controller.quickInsert(pasta);
  }
}
