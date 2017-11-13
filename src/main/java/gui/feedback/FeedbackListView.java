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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Feedback;
import model.FeedbackListener;
import model.FeedbackManager;
import model.UniqueArrayList;

import java.util.List;

public class FeedbackListView extends ListView<Feedback> {
  private final List<Feedback> feedbackList;
  private final FeedbackManager feedbackManager;
  private final List<FeedbackListener> listeners = new UniqueArrayList<>();

  public FeedbackListView(
      List<Feedback> feedbackList, FeedbackManager feedbackManager, FeedbackListener listener) {
    this.feedbackList = feedbackList;
    this.feedbackManager = feedbackManager;

    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    setContextMenu(createContextMenu());
    setMaxHeight(Double.MAX_VALUE);
    VBox.setVgrow(this, Priority.ALWAYS); // lazy way to do it, but it works

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

    setOnMouseClicked(
        mouseEvent -> {
          if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            if (mouseEvent.getClickCount() == 2) {
              preview();
            }
          }
        });

    if (listener != null) listeners.add(listener);
  }

  public void addListener(FeedbackListener listener) {
    listeners.add(listener);
  }

  public void removeListener(FeedbackListener listener) {
    listeners.remove(listener);
  }

  private ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem changeGroup = new MenuItem("Change group");
    changeGroup.setOnAction(event -> changeGroup());

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
    for (FeedbackListener fl : listeners) fl.exportFeedback(selectedItems, false, true);
  }

  private void exportFeedbackAsTxt() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (FeedbackListener fl : listeners) fl.exportFeedback(selectedItems, true, false);
  }

  private void deleteFeedback() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    FeedbackManager.deleteFeedbackSafe(selectedItems, feedbackManager);
    for (FeedbackListener fl : listeners) fl.listChanged();
  }

  private void preview() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;

    for (FeedbackListener fl : listeners) fl.preview(getSelectedItems());
  }

  public void changeGroup() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;
    for (FeedbackListener fl : listeners) fl.changeGroup(selectedItems);
  }

  private void toggleDone() {
    List<Feedback> selectedItems = getSelectedItems();
    if (selectedItems == null) return;
    for (FeedbackListener fl : listeners) fl.toggleDone(selectedItems);
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
