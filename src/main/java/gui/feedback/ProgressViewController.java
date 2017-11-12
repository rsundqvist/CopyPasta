package gui.feedback;

import gui.Tools;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import model.Feedback;
import model.FeedbackListListener;
import model.FeedbackManager;
import model.IO;

import java.util.List;

public class ProgressViewController implements FeedbackListListener {
  @FXML VBox vBox1, vBox2;
  @FXML private ProgressBar progressBar;
  @FXML private Label progressLabel, numFeedback, numDone, numNotDone;

  private FeedbackManager feedbackManager;
  private FeedbackListView doneListView, notDoneListView;
  private FeedbackListListener listener;

  public void initialize(FeedbackManager feedbackManager, FeedbackListListener listener) {

    this.listener = listener;
    this.feedbackManager = feedbackManager;

    doneListView =
        new FeedbackListView(feedbackManager.getDoneFeedback(), feedbackManager, listener);
    vBox1.getChildren().add(1, doneListView);
    doneListView.addListener(this);
    doneListView.addListener(listener);

    notDoneListView =
        new FeedbackListView(feedbackManager.getNotDoneFeedback(), feedbackManager, listener);
    vBox2.getChildren().add(1, notDoneListView);
    notDoneListView.addListener(this);
    notDoneListView.addListener(listener);
  }

  public void exportAllDone() {
    List<Feedback> feedbackList = feedbackManager.getDoneFeedback();
    listener.feedbackAboutToExport(feedbackList);
    IO.exportFeedback(feedbackList, true, true);
    update();
  }

  public void exportAllNotDone() {
    List<Feedback> feedbackList = feedbackManager.getNotDoneFeedback();
    listener.feedbackAboutToExport(feedbackList);
    IO.exportFeedback(feedbackList, true, true);
    update();
  }

  public void clearAllDone() {
    List<Feedback> feedbackList = feedbackManager.getDoneFeedback();

    if (Tools.confirmDelete(feedbackList.size())) {
      feedbackManager.deleteFeedback(feedbackList);
      listener.listChanged(feedbackList);
      update();
    }
  }

  public void clearAllNotDone() {
    List<Feedback> feedbackList = feedbackManager.getNotDoneFeedback();
    if (Tools.confirmDelete(feedbackList.size())) {
      feedbackManager.deleteFeedback(feedbackList);
      listener.listChanged(feedbackList);
      update();
    }
  }

  private void updateStatusLists() {
    notDoneListView.update();
    doneListView.update();
  }

  public void update() {
    int tot = feedbackManager.getFeedbackList().size();
    int done = feedbackManager.getDoneFeedback().size();

    numFeedback.setText(tot + "");
    numDone.setText(done + "");
    numNotDone.setText((tot - done) + "");

    if (tot == 0) {
      progressBar.setProgress(-1);
      progressLabel.setText("-");
    } else {
      double pDone = (double) done / tot;
      progressLabel.setText((int) (pDone * 100 + 0.5) + " %");
      progressBar.setProgress(pDone);
    }

    updateStatusLists();
  }

  @Override
  public void listChanged(List<Feedback> feedbackList) {
    System.out.println("feedbackList = " + feedbackList.size());
    System.out.println(
        "feedbackManager.getDoneFeedback() = " + feedbackManager.getDoneFeedback().size());
    update();
  }

  @Override
  public void feedbackAboutToExport(List<Feedback> feedbackList) {
    // Do nothing
  }
}
