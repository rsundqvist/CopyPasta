package gui.feedback;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
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
import model.UniqueArrayList;

import java.util.List;

public class FeedbackListView extends ListView<Feedback> {
  private final List<Feedback> feedbackList;
  private final FeedbackManager feedbackManager;
  private final List<FeedbackListListener> listeners = new UniqueArrayList<>();

  public FeedbackListView(
      List<Feedback> feedbackList, FeedbackManager feedbackManager, FeedbackListListener listener) {
    this.feedbackList = feedbackList;
    this.feedbackManager = feedbackManager;
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    setContextMenu(createContextMenu());

    setCellFactory(
        param ->
            new ListCell<Feedback>() {
              protected void updateItem(Feedback item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == null) {
                  setText(null);
                } else {
                  setText(item.getStylizedGroup());
                }
              }
            });
    if (listener != null) listeners.add(listener);
  }

  public void addListener(FeedbackListListener listener) {
    listeners.add(listener);
  }

  public void removeListner(FeedbackListListener listener) {
    listeners.remove(listener);
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
    for (FeedbackListListener fl : listeners) fl.feedbackAboutToExport(selectedItems);
    IO.exportFeedback(selectedItems, false, true);
  }

  private void exportFeedbackAsTxt() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (FeedbackListListener fl : listeners) fl.feedbackAboutToExport(selectedItems);
    IO.exportFeedback(selectedItems, true, false);
  }

  private void deleteFeedback() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    FeedbackManager.deleteFeedback(selectedItems, feedbackManager);
    for (FeedbackListListener fl : listeners) fl.listChanged(selectedItems);
    update();
  }

  private void preview() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (FeedbackListListener fl : listeners) fl.feedbackAboutToExport(selectedItems);
    FeedbackManager.preview(selectedItems.get(0));
  }

  public void changeFeedbackGroup() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (Feedback feedback : selectedItems) Feedback.changeFeedbackGroup(feedback);

    for (FeedbackListListener fl : listeners) fl.listChanged(selectedItems);
    update();
  }

  private void toggleDone() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (Feedback feedback : selectedItems) feedback.setDone(!feedback.isDone());
    feedbackManager.updateDoneUndoneLists();

    for (FeedbackListListener fl : listeners) fl.listChanged(selectedItems);
    update();
  }

  private List<Feedback> getSelectedItems() {
    List<Feedback> selectedItems = getSelectionModel().getSelectedItems();
    return selectedItems.isEmpty() ? null : selectedItems;
  }

  public void update() {
    update(feedbackList);
  }

  public void update(List<Feedback> feedbackList) {
    getItems().setAll(feedbackList);
  }
}
