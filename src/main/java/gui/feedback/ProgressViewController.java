package gui.feedback;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.Feedback;
import model.FeedbackListListener;
import model.FeedbackManager;

import java.util.List;

public class ProgressViewController {
  @FXML private ProgressBar progressBar;
  @FXML private Label progressLabel, numFeedback, numDone, numNotDone;
  @FXML VBox vBox1, vBox2;

  public void initialize() {}

  public void initialize(
      List<Feedback> doneList,
      List<Feedback> notDoneList,
      FeedbackManager feedbackManager,
      FeedbackListListener listener) {

    vBox1.getChildren().add(1, new FeedbackListView(doneList, feedbackManager, listener)); // Done
    vBox2
        .getChildren()
        .add(1, new FeedbackListView(notDoneList, feedbackManager, listener)); // Not done
  }
}
