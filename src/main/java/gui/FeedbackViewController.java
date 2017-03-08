package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import model.Feedback;
import model.FeedbackManager;
import model.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Richard Sundqvist on 19/02/2017.
 */
public class FeedbackViewController {

    private static final int SUPPRESS_CONFIRMATION_DURATION = 90; //in seconds
    //region Field
    // ================================================================================= //
    // Field
    // ================================================================================= //
    @FXML
    private Tab feedbackTab, setupTab, progressTab;
    @FXML
    private TextField studentGroupField, assignmentField, teacherField;
    @FXML
    private TabPane feedbackTabPane, rootTabPane;
    @FXML
    private TextArea templateTextArea, templateHeaderTextArea;
    @FXML
    /**
     * Container for the actual feedback tabs.
     */
    private ListView feedbackTabListView;
    @FXML
    private TitledPane bodyPane;
    private FeedbackManager feedbackManager = new FeedbackManager();
    private List<FeedbackTab> feedbackTabs = new ArrayList<>();
    //endregion
    private boolean hideDoneItems;
    @FXML
    private Label numFeedback, numDone, numNotDone;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ListView doneListView, notDoneListView;
    private boolean suppressClearDoneDialog = false;
    private boolean suppressClearNotDoneDialog = false;

    public void updateFeedbackTabLockStatus () {
        boolean empty = feedbackTabPane.getTabs().isEmpty();
        feedbackTab.setDisable(empty);
        if (empty)
            rootTabPane.getSelectionModel().select(setupTab);
    }

    public void createFeedbackItems (List<String> groups) {
        //Groups exist already? Modified?

        updateTemplate(); //make sure template is up-to-date for isContentModified
        List<Feedback> existing = feedbackManager.getByGroup(groups);
        List<Feedback> existingModified = feedbackManager.isContentModified(existing);
        List<Feedback> existingUnmodified = new ArrayList<>(existing);
        existingUnmodified.removeAll(existingModified);

        int numClash = existing.size();
        if (numClash > 0) {
            String contentText = "Groups with modified content: \n\t" +
                    FeedbackManager.getGroups(existingModified) + "\n" +
                    "Groups without modified content (unchanged): \n\t" +
                    FeedbackManager.getGroups(existingUnmodified);

            ButtonType replaceAll = new ButtonType("Overwrite All (" + numClash + ")");
            ButtonType replaceUnchanged = new ButtonType("Overwrite Unchanged (" + existingUnmodified.size() + ")");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, replaceAll, replaceUnchanged, ButtonType.CANCEL);
            alert.setHeaderText("Overwrite existing groups?");

            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent() || result.get() == ButtonType.CANCEL)
                return;

            if (result.get() == replaceAll) {
                feedbackManager.removeFeedback(existing);
                removeFeedbackTabs(existing);
            } else if (result.get() == replaceUnchanged) {
                feedbackManager.removeFeedback(existingUnmodified);
                removeFeedbackTabs(existingUnmodified);
            } else {
                throw new IllegalStateException("Unhandled case: " + result.get());
            }
        }
        List<Feedback> newFeedbackList = feedbackManager.generateFeedback(groups);

        if (newFeedbackList != null) {
            for (Feedback feedback : newFeedbackList)
                createFeedbackTab(feedback);

            if (newFeedbackList.size() > 1)
                rootTabPane.getSelectionModel().select(feedbackTab);
        }

        updateFeedbackTabLockStatus();
    }

    /**
     * FXML onAction for "Create Feedback" button.
     */
    public void createFeedbackItems () {
        String str = studentGroupField.getText();
        List<String> groups = FeedbackManager.parseGroupString(str);
        createFeedbackItems(groups);
    }

    private void removeFeedbackTabs (List<Feedback> feedbackList) {
        List<FeedbackTab> removedFeedbackTabs = getFeedbackTabs(feedbackList);

        feedbackTabListView.getItems().removeAll(removedFeedbackTabs);
        feedbackTabPane.getTabs().removeAll(removedFeedbackTabs);
        feedbackTabs.removeAll(removedFeedbackTabs);
        updateFeedbackTabLockStatus();
    }

    private List<FeedbackTab> getFeedbackTabs (List<Feedback> feedbackList) {
        List<FeedbackTab> removedFeedbackTabs = new ArrayList<>();

        for (FeedbackTab tab : feedbackTabs)
            if (feedbackList.contains(tab.getFeedback()))
                removedFeedbackTabs.add(tab);
        return removedFeedbackTabs;
    }

    private void updateTemplate () {
        Feedback template = feedbackManager.getTemplate();
        template.setContent(templateTextArea.getText());
        template.setHeader(templateHeaderTextArea.getText());
        template.setTeacher(teacherField.getText());
        template.setAssignment(getAssignment());
        feedbackManager.setTemplate(template);
    }

    private void createFeedbackTab (Feedback feedback) {
        FeedbackTab tab = new FeedbackTab(feedback);
        tab.setContextMenu(createFeedbackTabContextMenu(tab));
        tab.setOnClosed(event -> updateFeedbackTabLockStatus());
        feedbackTabPane.getTabs().add(tab);
        feedbackTabListView.getItems().add(tab);
        feedbackTabs.add(tab);
    }

    private ContextMenu createFeedbackTabContextMenu (FeedbackTab tab) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem changeGroup = new MenuItem("Change group");
        changeGroup.setOnAction(event -> changeFeedbackGroup(tab));

        MenuItem toggleDone = new MenuItem("Toggle done");
        toggleDone.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        toggleDone.setOnAction(event -> toggleDone(tab, true));

        MenuItem preview = new MenuItem("Preview");
        preview.setOnAction(event -> preview(tab));

        MenuItem exportTxt = new MenuItem("Export .txt");
        exportTxt.setOnAction(event -> exportFeedbackAsTxt(tab.getFeedback()));

        MenuItem exportJson = new MenuItem("Export .json");
        exportJson.setOnAction(event -> exportFeedbackAsJson(tab.getFeedback()));

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(event -> deleteFeedback(tab));

        contextMenu.getItems().addAll(
                changeGroup, toggleDone, preview,
                new SeparatorMenuItem(),
                exportTxt, exportJson,
                new SeparatorMenuItem(),
                delete);

        return contextMenu;
    }

    public void exportTemplate () {
        updateTemplate();
        IO.exportSingleFeedbackAsJson(Tools.SAVE_FOLDER, feedbackManager.getTemplate());
    }

    public void importTemplate () {
        File file = IO.showJSONOpenDialog();
        Feedback template = IO.importFeedbackSingle(file);
        bodyPane.setExpanded(true);
        setFeedbackTemplate(template);
    }

    public void exportAllFeedback () {
        exportFeedback(extractFeedback(false), true, true);
    }

    public void exportFeedbackAsTxt (Feedback feedback) {
        List<Feedback> feedbackList = new ArrayList<>(1);
        feedbackList.add(feedback);
        exportFeedback(feedbackList, true, false);
    }

    public void exportFeedbackAsJson (Feedback feedback) {
        List<Feedback> feedbackList = new ArrayList<>(1);
        feedbackList.add(feedback);
        exportFeedback(feedbackList, false, true);
    }

    public void exportFeedbackAsTxt () {
        exportFeedback(extractFeedback(true), true, false);
    }

    public void exportFeedbackAsJson () {
        exportFeedback(extractFeedback(true), false, true);
    }

    private void exportFeedback (List<Feedback> feedbackList, boolean asTxt, boolean asJson) {
        if (feedbackList == null || feedbackList.isEmpty() || !(asTxt || asJson)) return;

        updateTemplate();
        feedbackManager.updateFeedback();

        if (checkManualTags(feedbackList))
            return;

        if (asTxt && asJson) {
            IO.exportFeedbackAsTxtAndJson(null, feedbackList);
        } else if (feedbackList.size() == 1) { //Only one item
            Feedback feedback = feedbackList.get(0);
            String initialFileName = feedback.getGroup();
            File file = IO.showSaveDialog(Tools.SAVE_FOLDER, initialFileName, asTxt ? "txt" : "json");

            if (asTxt)
                IO.printStringToFile(feedback.getStylizedContent(), file);
            else
                IO.exportFeedbackAsJson(feedbackList, file);
        } else {
            if (asTxt) {
                IO.exportFeedbackAsTxt(null, feedbackList);
            } else {
                IO.exportFeedbackAsJson(Tools.SAVE_FOLDER, feedbackList);
            }
        }
    }

    /**
     * Returns true if the user wishes to abort.
     */
    public boolean checkManualTags (List<Feedback> feedbackList) {
        List<Feedback> badFeedbackList = Feedback.checkManual(feedbackList);

        if (badFeedbackList.isEmpty())
            return false;

        List<String> groups = FeedbackManager.getGroups(badFeedbackList);
        System.out.println(badFeedbackList.size());
        System.out.println(groups);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        alert.setTitle("Incomplete items found");
        alert.setHeaderText("Incomplete items found: " + badFeedbackList.size() + "/" + feedbackList.size());
        alert.setContentText("It looks like you're trying to export items with the " + Feedback.MANUAL + " tag present, " +
                "indicating that some items have content not meant for the student. Rectify before exporting?");

        //Content tags
        Label contentLabel = new Label("Groups: ");
        contentLabel.setMaxHeight(Double.MAX_VALUE);

        String badGroups = groups.toString();
        badGroups = badGroups.substring(1, badGroups.length() - 1);

        TextField badGroupsTextField = new TextField(badGroups);
        badGroupsTextField.setEditable(false);
        badGroupsTextField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(badGroupsTextField, Priority.ALWAYS);

        HBox contentTagsHBox = new HBox();
        contentTagsHBox.getChildren().addAll(contentLabel, badGroupsTextField);
        HBox.setHgrow(badGroupsTextField, Priority.ALWAYS);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(contentTagsHBox);
        alert.getDialogPane().setExpanded(true);

        Optional<ButtonType> result = alert.showAndWait();

        if (!result.isPresent() || result.get() != ButtonType.NO) { //Default to assuming user wants to fix content.
            List<FeedbackTab> badFeedbackTabs = getFeedbackTabs(badFeedbackList);
            for (FeedbackTab feedbackTab : badFeedbackTabs) {
                feedbackTab.getFeedback().setDone(true);
                toggleDone(feedbackTab, false);
            }
            feedbackTabPane.getTabs().removeAll(badFeedbackTabs);
            feedbackTabPane.getTabs().addAll(0, badFeedbackTabs);
            feedbackTabPane.getSelectionModel().select(0);
            rootTabPane.getSelectionModel().select(feedbackTab);
            updateFeedbackTabLockStatus();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extract feedback from {@link #feedbackTabListView}, or copy the list from the manager.
     *
     * @param selectedOnly If {@true}, extract selection only. Otherwise extract everything in them manager.
     * @return A list of feedback.
     */
    private List<Feedback> extractFeedback (boolean selectedOnly) {
        List<Feedback> feedbackList = new ArrayList<>();

        if (selectedOnly) {
            List<FeedbackTab> tabs = feedbackTabListView.getSelectionModel().getSelectedItems();
            for (FeedbackTab tab : tabs)
                feedbackList.add(tab.getFeedback());
        } else {
            feedbackList.addAll(feedbackManager.getFeedbackList());
        }

        return feedbackList;
    }

    public void deleteFeedback () {
        if (!feedbackTabListView.isFocused()) return;

        List<Object> selectedItems = new ArrayList<>(feedbackTabListView.getSelectionModel().getSelectedItems());
        //Must use copy, feedbackTabListView.getItems().remove() calls will cause issues otherwise.

        int numberOfItems = selectedItems.size();
        if (numberOfItems > 1 && !Tools.confirmDelete(numberOfItems)) return;

        if (numberOfItems == 1) {
            FeedbackTab tab = ((FeedbackTab) selectedItems.get(0));
            updateTemplate();
            if (feedbackManager.isContentModified(tab.getFeedback())) {

                String contentText = "The content of this feedback seems to have been modified.";
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);

                alert.setHeaderText("Really delete feedback for group \"" + tab.getFeedback().getGroup() + "\"?");

                Optional<ButtonType> result = alert.showAndWait();
                if (!result.isPresent() || result.get() != ButtonType.OK)
                    return;
            }
        }

        for (Object o : selectedItems)
            deleteFeedback((FeedbackTab) o);

        updateFeedbackTabLockStatus();
    }

    private void deleteFeedback (FeedbackTab tab) {
        if (tab == null) return;

        feedbackTabPane.getTabs().remove(tab);
        feedbackTabListView.getItems().remove(tab);
        feedbackTabs.remove(tab);
        feedbackManager.removeFeedback(tab.getFeedback());
        updateFeedbackTabLockStatus();
    }

    public void importFeedback () {
        List<Feedback> feedbackList = feedbackManager.importFeedback();

        if (feedbackList != null)
            for (Feedback feedback : feedbackList)
                createFeedbackTab(feedback);

        rootTabPane.getSelectionModel().select(feedbackTab);
        updateFeedbackTabLockStatus();
    }

    public void initialize () {
        feedbackTabListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        doneListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        notDoneListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //Feedback
        Feedback template;
        template = feedbackManager.importSavedTemplate();
        if (template == null)
            template = new Feedback();
        setFeedbackTemplate(template);

        //Feedback
        List<Feedback> feedbackList = feedbackManager.importSavedFeedback();
        if (feedbackList != null)
            //feedbackManager.importFeedback(feedbackList); //TODO
            for (Feedback feedback : feedbackList)
                createFeedbackTab(feedback);

        if (feedbackManager.getFeedbackList().isEmpty())
            rootTabPane.getSelectionModel().select(setupTab);

        bodyPane.setExpanded(true);
        updateFeedbackTabLockStatus();
    }

    public void setFeedbackTemplate (Feedback template) {
        if (template == null) return;

        feedbackManager.setTemplate(template);
        teacherField.setText((template.getTeacher()));
        assignmentField.setText(template.getAssignment());
        templateTextArea.setText(template.getContent());
        templateHeaderTextArea.setText(template.getHeader());
    }

    public void onMouseClicked (MouseEvent mouseEvent) {
        FeedbackTab tab = (FeedbackTab) feedbackTabListView.getSelectionModel().getSelectedItem();
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && tab != null) {//mouseEvent.isPrimaryButtonDown()
            if (feedbackTabPane.getTabs().contains(tab))
                feedbackTabPane.getSelectionModel().select(tab);
            else
                feedbackTabPane.getTabs().add(tab);
        }
        updateFeedbackTabLockStatus();
    }

    public void clear () {
        String contentText = "Really delete all feedback? There are currently " +
                feedbackManager.getFeedbackList().size() + " items.";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("Really delete all feedback?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            feedbackTabPane.getTabs().removeAll(feedbackTabs);
            feedbackTabListView.getItems().clear();
            feedbackManager.clear();
            feedbackTabs.clear();
            updateFeedbackTabLockStatus();
        }
    }

    public void changeFeedbackGroup () {
        FeedbackTab tab = (FeedbackTab) feedbackTabListView.getSelectionModel().getSelectedItem();
        if (tab == null) return;

        changeFeedbackGroup(tab);
    }

    private void changeFeedbackGroup (FeedbackTab tab) {
        Feedback feedback = tab.getFeedback();

        TextInputDialog dialog = new TextInputDialog(feedback.getGroup());
        dialog.setTitle("Change group number");
        dialog.setHeaderText("Change group number");
        dialog.setContentText("Enter new group number: ");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null) {
            String newGroup = result.get();
            tab.setTitle(newGroup);
            feedback.setGroup(newGroup);
            feedbackTabListView.refresh();
        }
    }

    public void shutdown () {
        Tools.exportSavedFeedback(feedbackManager.getFeedbackList());

        //Feedback
        Feedback template = new Feedback();
        template.setTeacher(teacherField.getText());
        template.setContent(templateTextArea.getText());
        template.setHeader(templateHeaderTextArea.getText());
        template.setAssignment(getAssignment());
        Tools.exportSavedTemplate(template);
    }

    public void preview () {
        FeedbackTab tab = (FeedbackTab) feedbackTabListView.getSelectionModel().getSelectedItem();
        preview(tab);
    }

    private void preview (FeedbackTab tab) {
        if (tab == null) return;

        Feedback feedback = tab.getFeedback();
        feedback.setTeacher(teacherField.getText());

        FeedbackManager.preview(feedback);
    }

    //region Status
    // ================================================================================= //
    // Status
    // ================================================================================= //

    /**
     * Called from main controller.
     */
    public void toggleDoneTab () {
        Tab tab = feedbackTabPane.getSelectionModel().getSelectedItem();
        toggleDone((FeedbackTab) tab, true);
    }

    /**
     * Toggle done for the list.
     */
    public void toggleDone () {
        if (!feedbackTabListView.isFocused())
            return;
        List<Object> selectedItems = feedbackTabListView.getSelectionModel().getSelectedItems();

        if (!selectedItems.isEmpty()) {
            for (Object o : selectedItems)
                toggleDone((FeedbackTab) o, false);

            if (feedbackManager.isAllFeedbackDone())
                allFeedbackDone();
        }
    }

    public void toggleDone (FeedbackTab tab, boolean checkAllDone) {
        if (tab == null) return;

        Feedback feedback = tab.getFeedback();
        feedbackManager.setDoneStatus(feedback, !feedback.isDone());
        tab.updateTitle();

        if (feedback.isDone())
            feedbackTabPane.getTabs().remove(tab);
        if (hideDoneItems)
            feedbackTabListView.getItems().removeAll(tab);
        else
            feedbackTabListView.refresh();

        if (checkAllDone && feedbackManager.isAllFeedbackDone())
            allFeedbackDone();

        updateFeedbackTabLockStatus();
    }

    private void allFeedbackDone () {
        String contentText = "All feedback is done! Export to .txt-files?";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("All feedback done!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES)
            exportAllFeedback();
    }

    public void toggleHideDoneItems (Event event) {
        CheckBox cb = (CheckBox) event.getSource();

        hideDoneItems = cb.isSelected();

        if (hideDoneItems) {
            List<Feedback> doneFeedbackList = feedbackManager.getDoneFeedbackList();
            List<FeedbackTab> doneFeedbackTabs = getFeedbackTabs(doneFeedbackList);

            feedbackTabPane.getTabs().removeAll(doneFeedbackTabs);
            feedbackTabListView.getItems().removeAll(doneFeedbackTabs);

            if (feedbackManager.isAllFeedbackDone())
                allFeedbackDone();
        } else {
            feedbackTabListView.getItems().clear();
            feedbackTabListView.getItems().addAll(feedbackTabs);
        }

        updateFeedbackTabLockStatus();
    }

    public void feedbackKeyTyped (KeyEvent event) {
        if (!feedbackTabPane.isFocused()) return;

        Tab tab = feedbackTabPane.getSelectionModel().getSelectedItem();
        if (tab != null && event.isControlDown() && event.getCode() == KeyCode.D) {
            toggleDone((FeedbackTab) tab, true);
            event.consume();
        }
    }

    public void onSelectionChanged (Event event) {
        Tab statisticsTab = (Tab) event.getSource();
        if (!statisticsTab.isSelected()) return;
        FeedbackManager fm = feedbackManager;

        int tot = fm.getFeedbackList().size();
        int done = fm.getDoneFeedbackList().size();

        numFeedback.setText(tot + "");
        numDone.setText(done + "");
        numNotDone.setText(fm.getNotDoneFeedbackList().size() + "");
        progressBar.setProgress(tot == 0 ? -1 : (double) done / tot);

        updateStatusLists();
    }

    private void updateStatusLists () {
        doneListView.getItems().clear();
        notDoneListView.getItems().clear();

        List<Feedback> done = feedbackManager.getDoneFeedbackList();
        doneListView.getItems().clear();
        doneListView.getItems().addAll(getFeedbackTabs(done));

        List<Feedback> notDone = feedbackManager.getNotDoneFeedbackList();
        notDoneListView.getItems().clear();
        notDoneListView.getItems().addAll(getFeedbackTabs(notDone));
    }

    public void exportAllDone () {
        List<Feedback> feedbackList = feedbackManager.getDoneFeedbackList();
        exportFeedback(feedbackList, true, true);
        suppressClearDoneDialog = true;

        //Reset after a little while
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(SUPPRESS_CONFIRMATION_DURATION),
                ae -> suppressClearDoneDialog = false));
        timeline.play();
    }

    public void clearDone () {
        List<Feedback> feedbackList = feedbackManager.getDoneFeedbackList();
        if (suppressClearDoneDialog || Tools.confirmDelete(feedbackList.size())) {
            feedbackManager.removeFeedback(feedbackList);
            updateStatusLists();
            removeFeedbackTabs(feedbackList);
        }
    }

    public void exportAllNotDone () {
        List<Feedback> feedbackList = feedbackManager.getNotDoneFeedbackList();
        exportFeedback(feedbackList, true, true);
        suppressClearNotDoneDialog = true;

        //Reset after a little while
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(SUPPRESS_CONFIRMATION_DURATION),
                ae -> suppressClearNotDoneDialog = false));
        timeline.play();
    }

    public void clearAllNotDone () {
        List<Feedback> feedbackList = feedbackManager.getNotDoneFeedbackList();
        if (suppressClearNotDoneDialog || Tools.confirmDelete(feedbackList.size())) {
            feedbackManager.removeFeedback(feedbackList);
            updateStatusLists();
            removeFeedbackTabs(feedbackList);
        }
    }

    public String getAssignment () {
        String assignment = assignmentField.getText();

        if (assignment != null)
            assignment = assignment.replaceAll("\\s+", "");

        return assignment;
    }
    //endregion
}
