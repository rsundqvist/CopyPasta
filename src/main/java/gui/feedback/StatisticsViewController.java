package gui.feedback;

import gui.Tools;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Feedback;
import model.FeedbackListListener;
import model.FeedbackManager;
import model.IO;

import java.util.HashMap;
import java.util.List;

public class StatisticsViewController implements FeedbackListListener {
  @FXML VBox vBox1, vBox2;
  @FXML private ProgressBar progressBar;
  @FXML private Label progressLabel, numFeedback, numDone, numNotDone;
  @FXML private Text groupText, gradeText;

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
    updateGroupTable();
    updateGradeTable();
  }

  private void updateGradeTable() {
    List<String> possibleGrades = feedbackManager.getTemplate().getPossibleGrades();
    HashMap<String, Counter> map = new HashMap<>(possibleGrades.size());
    for (String grade : possibleGrades) map.put(grade, new Counter());
    map.put(Feedback.GRADE_NOT_SET_OPTION, new Counter());

    for (Feedback feedback : feedbackManager.getFeedbackList())
      map.get(Feedback.stylizeGrade(feedback.getGrade())).count++;

    String hdline = String.format(" %-10s | %-10s | %-8s\n", "Grade", "#Groups", " Percent");
    String format = " %-11s| %-11d| %8.2f\n";

    StringBuilder sb = new StringBuilder(hdline);
    sb.append(new String(new char[hdline.length()]).replace("\0", "-") + "\n");

    for (String key : possibleGrades) {
      int c = map.get(Feedback.stylizeGrade(key)).count;
      double f = (100 * ((double) c) / feedbackManager.getFeedbackList().size());
      sb.append(String.format(format, key, c, f));
    }
    int c = map.get(Feedback.stylizeGrade(Feedback.GRADE_NOT_SET_OPTION)).count;
    double f = (100 * ((double) c) / feedbackManager.getFeedbackList().size());
    sb.append(String.format(format, Feedback.GRADE_NOT_SET_OPTION, c, f));

    gradeText.setText(sb.toString());
  }

  private void updateGroupTable() {
    String hdline = String.format(" %-25s| %-8s| %-7s| %-4s\n", "Group", "Grade", "#Files", "Done");
    String format = " %-25s| %-8s| %-7d| %4s\n";

    StringBuilder sb = new StringBuilder(hdline);
    sb.append(new String(new char[hdline.length()]).replace("\0", "-") + "\n");

    for (Feedback f : feedbackManager.getFeedbackList()) {
      sb.append(
          String.format(
              format,
              f.getGroup(),
              Feedback.stylizeGrade(f.getGrade()),
              f.getFiles().size(),
              (f.isDone() ? " \u2713  " : "")));
    }

    groupText.setText(sb.toString());
  }

  @Override
  public void listChanged(List<Feedback> feedbackList) {
    update();
  }

  @Override
  public void feedbackAboutToExport(List<Feedback> feedbackList) {
    // Do nothing
  }

  private static class Counter {
    private int count = 0;

    public String toString() {
      return "" + count;
    }
  }

  private class GradeRow {}

  private class GroupRow {}
}
