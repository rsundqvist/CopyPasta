package gui.feedback;

import gui.Tools;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import model.Feedback;
import model.FeedbackListener;
import model.FeedbackManager;
import model.IO;
import model.Pasta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Created by Richard Sundqvist on 19/02/2017. */
public class WorkspaceViewController implements FeedbackListener {
  private final FeedbackManager feedbackManager = new FeedbackManager();
  private final List<GroupView> groupViewList = new ArrayList<>();
  private final FeedbackListView feedbackListView =
      new FeedbackListView(feedbackManager.getFeedbackList(), feedbackManager, this);
  // region Field
  // ================================================================================= //
  // Field
  // ================================================================================= //
  @FXML private Tab groupViewsTab, setupTab, statisticsTab;
  @FXML private TextField studentGroupField, assignmentField;
  @FXML private TabPane groupViewsTabPane, rootTabPane;
  @FXML private Label visibleCountLabel;
  @FXML
  private TextArea signatureInput,
      possibleGradesInput,
      tempateBodyInput,
      templateHeaderInput,
      templateFooterInput;
  @FXML private StatisticsViewController statisticsViewController;
  @FXML private VBox feedbackListViewContainer;
  // endregion
  private boolean hideDoneItems = true;

  public void createFeedbackItems(List<String> groups) {
    // Groups exist already? Modified?

    updateTemplate(); // make sure template is up-to-date for isContentModified
    List<Feedback> existing = feedbackManager.getByGroup(groups);
    List<Feedback> existingModified = feedbackManager.isContentModified(existing);
    List<Feedback> existingUnmodified = new ArrayList<>(existing);
    existingUnmodified.removeAll(existingModified);

    int numClash = existing.size();
    if (numClash > 0) {
      String contentText =
          "Groups with modified content: \n\t"
              + FeedbackManager.getGroups(existingModified)
              + "\n"
              + "Groups without modified content (unchanged): \n\t"
              + FeedbackManager.getGroups(existingUnmodified);

      ButtonType replaceAll = new ButtonType("Overwrite All (" + numClash + ")");
      ButtonType replaceUnchanged =
          new ButtonType("Overwrite Unchanged (" + existingUnmodified.size() + ")");

      Alert alert =
          new Alert(
              Alert.AlertType.CONFIRMATION,
              contentText,
              replaceAll,
              replaceUnchanged,
              ButtonType.CANCEL);
      alert.setHeaderText("Overwrite existing groups?");

      Optional<ButtonType> result = alert.showAndWait();
      if (!result.isPresent() || result.get() == ButtonType.CANCEL) return;

      if (result.get() == replaceAll) {
        feedbackManager.deleteFeedback(existing);
      } else if (result.get() == replaceUnchanged) {
        feedbackManager.deleteFeedback(existingUnmodified);
      } else {
        throw new IllegalStateException("Unhandled case: " + result.get());
      }
    }
    List<Feedback> newFeedbackList = feedbackManager.generateFeedback(groups);

    if (newFeedbackList != null) {
      for (Feedback feedback : newFeedbackList) createFeedbackView(feedback);

      if (newFeedbackList.size() > 1) rootTabPane.getSelectionModel().select(groupViewsTab);
    }

    update();
  }

  /** FXML onAction for "Create Feedback" button. */
  public void createFeedbackItems() {
    String str = studentGroupField.getText();
    List<String> groups = Tools.extractTokens(str);
    createFeedbackItems(groups);
  }

  private List<GroupView> getFeedbackViews(List<Feedback> feedbackList) {
    List<GroupView> selectedGroups = new ArrayList<>();

    for (GroupView groupView : groupViewList)
      if (feedbackList.contains(groupView.getFeedback())) selectedGroups.add(groupView);
    return selectedGroups;
  }

  private void updateTemplate() {
    updateTemplateFromInput(feedbackManager.getTemplate());
  }

  private void updateTemplateFromInput(Feedback template) {
    template.setContent(tempateBodyInput.getText());
    template.setHeader(templateHeaderInput.getText());
    template.setFooter(templateFooterInput.getText());
    template.setSignature(signatureInput.getText());
    template.setAssignment(getAssignment());
    template.getPossibleGrades().clear();
    template.getPossibleGrades().addAll(Tools.extractTokens(possibleGradesInput.getText()));
  }

  private void createFeedbackView(Feedback feedback) {
    GroupView tab = new GroupView(feedback, this);
    tab.setContextMenu(createFeedbackTabContextMenu(tab));
    tab.updatePossibleGrades(feedbackManager);
    groupViewList.add(tab);
    update();
  }

  private ContextMenu createFeedbackTabContextMenu(GroupView tab) {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem changeGroup = new MenuItem("Change group");
    changeGroup.setOnAction(event -> changeGroup(Arrays.asList(tab.getFeedback())));

    MenuItem toggleDone = new MenuItem("Toggle done");
    toggleDone.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
    toggleDone.setOnAction(event -> toggleDone(Arrays.asList(tab.getFeedback())));

    MenuItem preview = new MenuItem("Preview");
    preview.setOnAction(event -> preview(tab));

    MenuItem exportTxt = new MenuItem("Export .txt");
    exportTxt.setOnAction(event -> exportFeedbackAsTxt(tab.getFeedback()));

    MenuItem exportJson = new MenuItem("Export .json");
    exportJson.setOnAction(event -> exportFeedbackAsJson(tab.getFeedback()));

    MenuItem delete = new MenuItem("Delete");
    delete.setOnAction(event -> deleteFeedback(tab));

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

  private void deleteFeedback(GroupView groupView) {
    FeedbackManager.deleteFeedbackSafe(Arrays.asList(groupView.getFeedback()), feedbackManager);
    update();
  }

  public void exportTemplate() {
    updateTemplate();
    IO.exportSingleFeedbackAsJson(null, feedbackManager.getTemplate());
  }

  public void importTemplate() {
    File file = IO.showJSONOpenDialog();
    Feedback template = IO.importFeedbackSingle(file);
    setFeedbackTemplate(template);
  }

  public void exportAllFeedback() {
    exportFeedback(feedbackManager.getFeedbackList(), true, true);
  }

  public void exportFeedbackAsTxt(Feedback feedback) {
    ArrayList<Feedback> list = new ArrayList(1);
    list.add(feedback);
    exportFeedback(list, true, false);
  }

  public void exportFeedbackAsJson(Feedback feedback) {
    ArrayList<Feedback> list = new ArrayList(1);
    list.add(feedback);
    exportFeedback(list, false, true);
  }

  public boolean exportFeedback(List<Feedback> feedbackList, boolean asTxt, boolean asJson) {
    if (feedbackList == null || feedbackList.isEmpty() || !(asTxt || asJson)) return false;
    updateTemplate();

    if (Feedback.checkManualTags(feedbackList)) {
      showIncompleteFeedback();
      return false;
    }

    return IO.exportFeedback(feedbackList, asTxt, asJson);
  }

  public void showIncompleteFeedback() {
    // Mark items with manual tag as incomplete
    List<Feedback> incompleteItems = feedbackManager.checkManual();
    incompleteItems.forEach(feedback -> feedback.setDone(false));

    // Get incomplete feedback
    feedbackManager.updateDoneUndoneLists();
    incompleteItems = feedbackManager.getNotDoneFeedback();
    if (incompleteItems.isEmpty()) return;

    // Add and select
    List<GroupView> incompleteGroups = getFeedbackViews(incompleteItems);
    incompleteGroups.forEach(groupView -> groupView.update());
    groupViewsTabPane.getTabs().setAll(incompleteGroups);
    rootTabPane.getSelectionModel().select(groupViewsTab);
  }

  public void importFeedback() {
    List<Feedback> feedbackList = feedbackManager.importFeedback();

    if (feedbackList != null) updateAfterFeedbackImport(feedbackList);
  }

  /**
   * Import feedback.
   *
   * @param feedbackList The feedback to import.
   * @param replaceAll if {@code true}, old feedback is cleared.
   * @param setTemplateContent if {@code true}, content will be set by template.
   */
  public void importFeedback(
      List<Feedback> feedbackList, boolean replaceAll, boolean setTemplateContent) {
    updateTemplate();
    if (replaceAll) clearFeedback();

    List<Feedback> newFeedbackList =
        feedbackManager.importFeedback(feedbackList, setTemplateContent);
    updateAfterFeedbackImport(newFeedbackList);
  }

  private void updateAfterFeedbackImport(List<Feedback> feedbackList) {
    for (Feedback feedback : feedbackList) createFeedbackView(feedback);

    rootTabPane.getSelectionModel().select(groupViewsTab);
  }

  public void initialize() {
    Feedback template = feedbackManager.importSavedTemplate();
    if (template == null) template = new Feedback();
    setFeedbackTemplate(template);

    // Feedback
    List<Feedback> feedbackList = feedbackManager.importSavedFeedback();
    if (feedbackList != null) for (Feedback feedback : feedbackList) createFeedbackView(feedback);

    if (feedbackManager.getFeedbackList().isEmpty())
      rootTabPane.getSelectionModel().select(setupTab);
    statisticsViewController.initialize(feedbackManager, this);
    groupViewsTabPane.getTabs().addListener((InvalidationListener) event -> updateLockStatus());

    feedbackListViewContainer.getChildren().add(feedbackListView);
    feedbackListViewContainer = null;

    update();
    updateLockStatus();

    feedbackListView
        .getSelectionModel()
        .getSelectedItems()
        .addListener((ListChangeListener<? super Feedback>) event -> addSelectedTabs());
  }

  private void addSelectedTabs() {
    List<Feedback> selectedItems = feedbackListView.getSelectionModel().getSelectedItems();
    List<GroupView> views = getFeedbackViews(selectedItems);
    views.removeAll(groupViewsTabPane.getTabs());
    groupViewsTabPane.getTabs().addAll(views);
  }

  public void setFeedbackTemplate(Feedback template) {
    if (template == null) return;
    updateTemplate(template);
  }

  private void updateTemplate(Feedback template) {
    feedbackManager.setTemplate(template);
    signatureInput.setText((template.getSignature()));
    assignmentField.setText(template.getAssignment());
    tempateBodyInput.setText(template.getContent());
    templateHeaderInput.setText(template.getHeader());
    templateFooterInput.setText(template.getFooter());
    String possibleGradesString = template.getPossibleGrades().toString();
    possibleGradesString = possibleGradesString.substring(1, possibleGradesString.length() - 1);
    possibleGradesInput.setText(possibleGradesString);
  }

  public void clear() {
    if (Tools.confirmDelete(feedbackManager.getFeedbackList().size())) clearFeedback();
  }

  public void clearFeedback() {
    feedbackManager.clear();
    groupViewList.clear();
    update();
  }

  public void save() {
    Tools.exportSavedFeedback(feedbackManager.getFeedbackList());
    Feedback template = feedbackManager.getTemplate();
    updateTemplateFromInput(template);
    Tools.exportSavedTemplate(template);
  }

  private void preview(GroupView tab) {
    if (tab == null) return;
    preview(Arrays.asList(tab.getFeedback()));
  }

  public void quickInsert(Pasta pasta) {
    GroupView groupView = (GroupView) groupViewsTabPane.getSelectionModel().getSelectedItem();
    if (groupView == null) return;
    groupView.quickInsert(pasta);
  }

  // region Status
  // ================================================================================= //
  // Status
  // ================================================================================= //

  /** Called from main controller. */
  public void toggleDoneTab() {
    GroupView groupView = (GroupView) groupViewsTabPane.getSelectionModel().getSelectedItem();
    toggleDone(Arrays.asList(groupView.getFeedback()));
  }

  private void checkFeedbackDone() {
    if (!feedbackManager.isAllFeedbackDone()) return;

    String contentText = "All feedback is done! Export to .txt-files?";
    Alert alert =
        new Alert(Alert.AlertType.INFORMATION, contentText, ButtonType.YES, ButtonType.NO);
    alert.setHeaderText("All feedback done!");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.YES) exportAllFeedback();
  }

  public void toggleHideDoneItems(Event event) {
    CheckBox cb = (CheckBox) event.getSource();
    hideDoneItems = cb.isSelected();
    update(false);
  }

  public void feedbackKeyTyped(KeyEvent event) {
    if (!groupViewsTabPane.isFocused()) return;

    GroupView groupView = (GroupView) groupViewsTabPane.getSelectionModel().getSelectedItem();
    if (groupView != null && event.isControlDown() && event.getCode() == KeyCode.D) {
      toggleDone(Arrays.asList(groupView.getFeedback()));
      event.consume();
    }
  }

  /** Called when one of the three major tabs are selected. */
  public void onSelectionChanged(Event e) {
    Tab source = (Tab) e.getSource();
    if (!source.isSelected()) return;

    if (source == groupViewsTab) {
      groupViewsTabPane
          .getTabs()
          .forEach(groupView -> ((GroupView) groupView).update()); // student view
    } else if (source == statisticsTab) {
      updateTemplate();
      statisticsViewController.update();
    } else if (source == setupTab) {
      update(false);
    }
  }

  public void updateLockStatus() {
    boolean empty = groupViewsTabPane.getTabs().isEmpty();
    groupViewsTab.setDisable(empty);
    statisticsTab.setDisable(feedbackManager.getFeedbackList().isEmpty());
    if (empty) rootTabPane.getSelectionModel().select(setupTab);
  }

  public String getAssignment() {
    String assignment = assignmentField.getText();
    if (assignment != null) assignment = assignment.replaceAll("\\s+", "");
    return assignment;
  }

  public void selectView(int i) {
    rootTabPane.getSelectionModel().select(i);
  }

  public void update() {
    update(true);
  }

  public void update(boolean updateTabs) {
    List<Feedback> visibleFeedback;
    if (hideDoneItems) visibleFeedback = feedbackManager.getNotDoneFeedback();
    else visibleFeedback = feedbackManager.getFeedbackList();

    feedbackListView.update(visibleFeedback);

    visibleCountLabel.setText(
        "Showing "
            + visibleFeedback.size()
            + "/"
            + feedbackManager.getFeedbackList().size()
            + " items.");

    if (updateTabs) groupViewsTabPane.getTabs().setAll(getFeedbackViews(visibleFeedback));
    groupViewList.forEach(groupView -> groupView.update());
  }

  @Override
  public void listChanged() {
    update();
    updateLockStatus();
  }

  @Override
  public void changeGroup(List<Feedback> feedbackList) {
    feedbackList.forEach(
        feedback -> {
          feedbackManager.changeFeedbackGroup(feedback);
        });
    feedbackListView.refresh();
    update(false);
  }

  @Override
  public void toggleDone(List<Feedback> feedbackList) {
    if (feedbackList == null) return;

    for (Feedback feedback : feedbackList)
      feedbackManager.setDoneStatus(feedback, !feedback.isDone());
    checkFeedbackDone();
    update(false);
    if (hideDoneItems) groupViewsTabPane.getTabs().removeAll(getFeedbackViews(feedbackList));
  }

  @Override
  public void preview(List<Feedback> feedbackList) {
    updateTemplate();
    feedbackList.forEach(FeedbackManager::preview);
  }

  // endregion
}
