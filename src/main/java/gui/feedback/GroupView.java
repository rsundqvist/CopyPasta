package gui.feedback;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import model.Feedback;
import model.FeedbackListener;
import model.FeedbackManager;
import model.IO;
import model.Pasta;

public class GroupView extends Tab {
  public static final int MIN_TITLE_LENGTH = 8;
  private final Feedback feedback;

  private final GroupViewController controller;

  public GroupView(Feedback feedback, FeedbackListener listener) {
    this.feedback = feedback;

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/groupView.fxml"));
    try {
      setContent(fxmlLoader.load());
    } catch (Exception e) {
      IO.showExceptionAlert(e);
      e.printStackTrace();
    }
    controller = fxmlLoader.getController();
    controller.initialize(feedback, listener);
    update();
  }

  private static String stylizeTitle(String title) {
    if (title == null || title.isEmpty()) title = "<Unknown group>";
    else if (title.length() < MIN_TITLE_LENGTH)
      title += new String(new char[MIN_TITLE_LENGTH - title.length()]).replace("\0", " ");
    return title;
  }

  public void updatePossibleGrades(FeedbackManager feedbackManager) {
    controller.updatePossibleGrades(feedbackManager);
  }

  public void setTitle(String title) {
    setText(stylizeTitle(title));
  }

  public String toString() {
    return getText(); // Needed to make ListViews work properly
  }

  public void update() {
    String title = feedback.getStylizedGroup();
    setTitle(stylizeTitle(title));
    controller.update();
  }

  public Feedback getFeedback() {
    controller.updateFeedbackContent();
    return feedback;
  }

  public void quickInsert(Pasta pasta) {
    controller.quickInsert(pasta);
  }
}
