package gui.feedback;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import model.Feedback;
import model.FeedbackListListener;
import model.FeedbackManager;
import model.IO;

import java.util.List;

public class FeedbackListView extends ListView<Feedback> {
  private final List<Feedback> feedbackList;
  private final FeedbackManager feedbackManager;
  private final FeedbackListListener listener;

  public FeedbackListView(
      List<Feedback> feedbackList,
      FeedbackManager feedbackManager,
      FeedbackListListener listener) {
    this.feedbackList = feedbackList;
    this.feedbackManager = feedbackManager;
    this.listener = listener;
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    setContextMenu(createContextMenu());
  }

  private ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem changeGroup = new MenuItem("Change group");
    changeGroup.setOnAction(event -> changeFeedbackGroup());

    MenuItem toggleDone = new MenuItem("Toggle done");
    toggleDone.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
    toggleDone.setOnAction(event -> toggleDone());

    MenuItem preview = new MenuItem("Preview");
    preview.setOnAction(event -> preview());

    MenuItem exportTxt = new MenuItem("Export .txt");
    exportTxt.setOnAction(event -> exportFeedbackAsTxt());

    MenuItem exportJson = new MenuItem("Export .json");
    exportJson.setOnAction(event -> exportFeedbackAsJson());

    MenuItem delete = new MenuItem("Delete");
    delete.setOnAction(event -> deleteFeedback());

    contextMenu
        .getItems()
        .addAll(
            changeGroup,
            toggleDone,
            preview,
            new SeparatorMenuItem(),
            exportTxt,
            exportJson,
            new SeparatorMenuItem(),
            delete);

    return contextMenu;
  }

  private void exportFeedbackAsJson() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    listener.feedbackAboutToExport();
    IO.exportFeedback(feedbackList, false, true);
  }

  private void exportFeedbackAsTxt() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    listener.feedbackAboutToExport();
    IO.exportFeedback(feedbackList, true, false);
  }

  private void deleteFeedback() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    FeedbackManager.deleteFeedback(feedbackList, feedbackManager);
    listener.listChanged();
    update();
  }

  private void preview() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    FeedbackManager.preview(selectedItems.get(0));
  }

  public void changeFeedbackGroup() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (Feedback feedback : selectedItems) Feedback.changeFeedbackGroup(feedback);

    listener.listChanged();
    update();
  }

  private void toggleDone() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (Feedback feedback : selectedItems) feedback.setDone(!feedback.isDone());

    listener.listChanged();
    update();
  }

  private List<Feedback> getSelectedItems() {
    List<Feedback> selectedItems = getSelectionModel().getSelectedItems();
    return selectedItems.isEmpty() ? null : selectedItems;
  }

  public void update() {
    getItems().setAll(feedbackList);
  }
}
