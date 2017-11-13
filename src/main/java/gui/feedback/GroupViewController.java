package gui.feedback;

import javafx.beans.InvalidationListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.util.Pair;
import model.Feedback;
import model.FeedbackListener;
import model.FeedbackManager;
import model.Pasta;

import java.util.ArrayList;
import java.util.Arrays;

public class GroupViewController implements FileViewController.FileFeedbackListener {
  public static final boolean SWITCH_TO_FEEDBACK_ON_QUICKINSERT = false;

  @FXML
  FileViewController fileViewController; // Controller of an included file = fx:id + "Controller"

  @FXML private Tab feedbackTab, filesTab;
  @FXML private TabPane viewsPane;
  @FXML private ChoiceBox<String> gradeChoiceBox;
  @FXML private TextArea notesArea;
  @FXML private ToggleButton toggleDoneButton;
  private Feedback feedback;
  private FeedbackText feedbackText;
  private FeedbackListener listener;

  public GroupViewController() {
    feedbackText = new FeedbackText();
  }

  public void updateFilesTabTitle() {
    filesTab.setText("Files (" + feedback.getFiles().size() + ")");
  }

  public void initialize() {
    feedbackTab.setContent(feedbackText);
    fileViewController.setListener(this);
    gradeChoiceBox.getItems().add(Feedback.GRADE_NOT_SET_OPTION);
    gradeChoiceBox.getSelectionModel().select(Feedback.GRADE_NOT_SET_OPTION);
    fileViewController
        .getSourceTabs()
        .addListener((InvalidationListener) event -> updateFilesTabTitle());
  }

  private void gradeChanged() {
    String newGrade = gradeChoiceBox.getSelectionModel().getSelectedItem();
    if (feedback == null || newGrade == null || newGrade.isEmpty()) return;
    feedback.setGrade(Feedback.destylizeGrade(newGrade));
  }

  public void onChangeGroup() {
    listener.changeGroup(Arrays.asList(feedback));
  }

  public void onToggleDone() {
    listener.toggleDone(Arrays.asList(feedback));
    update();
  }

  public void onExportTxt() {
    ArrayList<Feedback> feedbackList = new ArrayList<>(1);
    feedbackList.add(feedback);
    listener.exportFeedback(feedbackList, true, false);
  }

  public void onExportJson() {
    ArrayList<Feedback> feedbackList = new ArrayList<>(1);
    feedbackList.add(feedback);
    listener.exportFeedback(feedbackList, false, true);
  }

  public void updatePossibleGrades(FeedbackManager feedbackManager) {
    gradeChoiceBox.getItems().clear();
    gradeChoiceBox.getItems().add(Feedback.GRADE_NOT_SET_OPTION);
    gradeChoiceBox.getItems().addAll(feedbackManager.getTemplate().getPossibleGrades());
    updateGradeChoiceBox();
  }

  public void initialize(Feedback feedback, FeedbackListener listener) {
    this.listener = listener;
    this.feedback = feedback;
    feedbackText.setFeedback(feedback);
    fileViewController.setFeedback(feedback);
    updateGradeChoiceBox();
    updateFilesTabTitle();
    gradeChoiceBox.getSelectionModel().selectedItemProperty().addListener(event -> gradeChanged());
    notesArea.setText(feedback.getNotes());
  }

  public void updateNotes() {
    feedback.setNotes(notesArea.getText());
  }

  public void toggleTextWrap(Event e) {
    notesArea.setWrapText(((CheckBox) e.getSource()).isSelected());
  }

  public void updateGradeChoiceBox() {
    String grade = feedback.getGrade();
    gradeChoiceBox
        .getSelectionModel()
        .select((grade == null || grade.isEmpty()) ? Feedback.GRADE_NOT_SET_OPTION : grade);
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

  public void onPreview() {
    FeedbackManager.preview(feedback);
  }

  public void update() {
    feedbackText.updateColor();
    toggleDoneButton.setSelected(feedback.isDone());
  }

  public void updateFeedbackContent() {
    feedback.setContent(feedbackText.getText());
  }
}
