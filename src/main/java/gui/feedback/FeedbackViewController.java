package gui.feedback;

import gui.Tools;
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
import model.Pasta;

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
    private Tab groupTab, setupTab, progressTab;
    @FXML
    private TextField studentGroupField, assignmentField, teacherField;
    @FXML
    private Label progressLabel;
    @FXML
    private TabPane feedbackTabPane, rootTabPane;
    @FXML
    private TextArea templateTextArea, templateHeaderTextArea, templateFooterTextArea;
    @FXML
    /**
     * Container for the actual feedback tabs.
     */
    private ListView feedbackTabListView;
    @FXML
    private TitledPane bodyPane;
    private FeedbackManager feedbackManager = new FeedbackManager();
    private List<GroupTab> groupTabs = new ArrayList<>();
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
        groupTab.setDisable(empty);
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
                rootTabPane.getSelectionModel().select(groupTab);
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
        List<GroupTab> removedGroupTabs = getFeedbackTabs(feedbackList);

        feedbackTabListView.getItems().removeAll(removedGroupTabs);
        feedbackTabPane.getTabs().removeAll(removedGroupTabs);
        groupTabs.removeAll(removedGroupTabs);
        updateFeedbackTabLockStatus();
    }

    private List<GroupTab> getFeedbackTabs (List<Feedback> feedbackList) {
        List<GroupTab> removedGroupTabs = new ArrayList<>();

        for (GroupTab tab : groupTabs)
            if (feedbackList.contains(tab.getFeedback()))
                removedGroupTabs.add(tab);
        return removedGroupTabs;
    }

    private void updateTemplate () {
        Feedback template = feedbackManager.getTemplate();
        template.setContent(templateTextArea.getText());
        template.setHeader(templateHeaderTextArea.getText());
        template.setFooter(templateFooterTextArea.getText());
        template.setTeacher(teacherField.getText());
        template.setAssignment(getAssignment());
        feedbackManager.setTemplate(template);
    }

    private void createFeedbackTab (Feedback feedback) {
        GroupTab tab = new GroupTab(feedback);
        tab.setContextMenu(createFeedbackTabContextMenu(tab));
        tab.setOnClosed(event -> updateFeedbackTabLockStatus());
        if (!feedback.isDone()) feedbackTabPane.getTabs().add(tab);
        feedbackTabListView.getItems().add(tab);
        groupTabs.add(tab);
    }

    private ContextMenu createFeedbackTabContextMenu (GroupTab tab) {
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

    private boolean exportFeedback (List<Feedback> feedbackList, boolean asTxt, boolean asJson) {
        if (feedbackList == null || feedbackList.isEmpty() || !(asTxt || asJson)) return false;

        updateTemplate();
        feedbackManager.updateFeedback();

        if (checkManualTags(feedbackList))
            return false;

        boolean exportSuccessful;
        if (asTxt && asJson) {
            exportSuccessful = IO.exportFeedbackAsTxtAndJson(null, feedbackList);
        } else if (feedbackList.size() == 1) { //Only one item
            Feedback feedback = feedbackList.get(0);
            String initialFileName = feedback.getGroup();
            File file = IO.showSaveDialog(Tools.SAVE_FOLDER, initialFileName, asTxt ? "txt" : "json");

            if (asTxt)
                exportSuccessful = IO.printStringToFile(feedback.getStylizedContent(), file);
            else
                exportSuccessful = IO.exportFeedbackAsJson(feedbackList, file);
        } else {
            if (asTxt)
                exportSuccessful = IO.exportFeedbackAsTxt(null, feedbackList);
            else
                exportSuccessful = IO.exportFeedbackAsJson(Tools.SAVE_FOLDER, feedbackList);
        }

        return exportSuccessful;
    }

    /**
     * Returns true if the user wishes to abort.
     */
    public boolean checkManualTags (List<Feedback> feedbackList) {
        List<Feedback> badFeedbackList = Feedback.checkManual(feedbackList);

        if (badFeedbackList.isEmpty())
            return false;

        List<String> groups = FeedbackManager.getGroups(badFeedbackList);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        alert.setTitle("Incomplete items found");
        alert.setHeaderText("Found " + badFeedbackList.size() + " incomplete items (of " + feedbackList.size() + " items total)");
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
            List<GroupTab> badGroupTabs = getFeedbackTabs(badFeedbackList);
            for (GroupTab groupTab : badGroupTabs) {
                groupTab.getFeedback().setDone(true);
                toggleDone(groupTab, false);
            }
            feedbackTabPane.getTabs().removeAll(badGroupTabs);
            feedbackTabPane.getTabs().addAll(0, badGroupTabs);
            feedbackTabPane.getSelectionModel().select(0);
            rootTabPane.getSelectionModel().select(groupTab);
            updateFeedbackTabLockStatus();
            return true;
        }

        return false;
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
            List<GroupTab> tabs = feedbackTabListView.getSelectionModel().getSelectedItems();
            for (GroupTab tab : tabs)
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
            GroupTab tab = ((GroupTab) selectedItems.get(0));
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
            deleteFeedback((GroupTab) o);

        updateFeedbackTabLockStatus();
    }

    private void deleteFeedback (GroupTab tab) {
        if (tab == null) return;

        feedbackTabPane.getTabs().remove(tab);
        feedbackTabListView.getItems().remove(tab);
        groupTabs.remove(tab);
        feedbackManager.removeFeedback(tab.getFeedback());
        updateFeedbackTabLockStatus();
    }

    public void importFeedback () {
        List<Feedback> feedbackList = feedbackManager.importFeedback();

        if (feedbackList != null)
            updateAfterFeedbackImport(feedbackList);
    }

    /**
     * Import feedback.
     *
     * @param feedbackList The feedback to import.
     * @param replaceAll if {@code true}, old feedback is cleared.
     */
    public void importFeedbackAddTemplateContent (List<Feedback> feedbackList, boolean replaceAll) {
        updateTemplate();
        if (replaceAll)
            clearFeedback();

        List<Feedback> newFeedbackList = feedbackManager.importFeedback(feedbackList, true);
        updateAfterFeedbackImport(newFeedbackList);
    }

    private void updateAfterFeedbackImport (List<Feedback> feedbackList) {
        for (Feedback feedback : feedbackList)
            createFeedbackTab(feedback);

        rootTabPane.getSelectionModel().select(groupTab);
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
        templateFooterTextArea.setText(template.getFooter());
    }

    public void onMouseClicked (MouseEvent event) {
        GroupTab tab = (GroupTab) feedbackTabListView.getSelectionModel().getSelectedItem();
        if (event.getButton().equals(MouseButton.PRIMARY) && tab != null) {//mouseEvent.isPrimaryButtonDown()
            if (feedbackTabPane.getTabs().contains(tab))
                feedbackTabPane.getSelectionModel().select(tab);
            else
                feedbackTabPane.getTabs().add(tab);


            if (event.getClickCount() > 1)
                preview();
        }
        updateFeedbackTabLockStatus();
    }

    public void clear () {
        String contentText = "Really delete all feedback? There are currently " +
                feedbackManager.getFeedbackList().size() + " items.";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("Really delete all feedback?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
            clearFeedback();
    }

    private void clearFeedback () {
        feedbackTabPane.getTabs().removeAll(groupTabs);
        feedbackTabListView.getItems().clear();
        feedbackManager.clear();
        groupTabs.clear();
        updateFeedbackTabLockStatus();

    }

    public void changeFeedbackGroup () {
        GroupTab tab = (GroupTab) feedbackTabListView.getSelectionModel().getSelectedItem();
        if (tab == null) return;

        changeFeedbackGroup(tab);
    }

    private void changeFeedbackGroup (GroupTab tab) {
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

    public void save () {
        Tools.exportSavedFeedback(feedbackManager.getFeedbackList());

        //Feedback
        Feedback template = new Feedback();
        template.setTeacher(teacherField.getText());
        template.setContent(templateTextArea.getText());
        template.setHeader(templateHeaderTextArea.getText());
        template.setFooter(templateFooterTextArea.getText());
        template.setAssignment(getAssignment());
        Tools.exportSavedTemplate(template);
    }

    public void preview () {
        GroupTab tab = (GroupTab) feedbackTabListView.getSelectionModel().getSelectedItem();
        preview(tab);
    }

    private void preview (GroupTab tab) {
        if (tab == null) return;

        Feedback feedback = tab.getFeedback();
        updateTemplate();
        feedbackManager.updateFeedback(feedback);
        FeedbackManager.preview(feedback);
    }

    public void quickInsert (Pasta pasta) {
        GroupTab tab = (GroupTab) feedbackTabPane.getSelectionModel().getSelectedItem();
        if (tab == null) return;
        tab.quickInsert(pasta);
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
        toggleDone((GroupTab) tab, true);
    }

    /**
     * Toggle done for the list.
     */
    public void toggleDone () {
        //TODO focus
        //if (!feedbackTabListView.isFocused())
        //    return;

        List<Object> selectedItems = feedbackTabListView.getSelectionModel().getSelectedItems();

        if (!selectedItems.isEmpty()) {
            for (Object o : selectedItems)
                toggleDone((GroupTab) o, false);

            if (feedbackManager.isAllFeedbackDone())
                allFeedbackDone();
        }
    }

    public void toggleDone (GroupTab tab, boolean checkAllDone) {
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
            List<GroupTab> doneGroupTabs = getFeedbackTabs(doneFeedbackList);

            feedbackTabPane.getTabs().removeAll(doneGroupTabs);
            feedbackTabListView.getItems().removeAll(doneGroupTabs);

            if (feedbackManager.isAllFeedbackDone())
                allFeedbackDone();
        } else {
            feedbackTabListView.getItems().clear();
            feedbackTabListView.getItems().addAll(groupTabs);
        }

        updateFeedbackTabLockStatus();
    }

    public void feedbackKeyTyped (KeyEvent event) {
        if (!feedbackTabPane.isFocused()) return;

        Tab tab = feedbackTabPane.getSelectionModel().getSelectedItem();
        if (tab != null && event.isControlDown() && event.getCode() == KeyCode.D) {
            toggleDone((GroupTab) tab, true);
            event.consume();
        }
    }

    public void onSelectionChanged (Event event) {
        Tab statisticsTab = (Tab) event.getSource();
        if (!statisticsTab.isSelected()) return;

        updateStatistics();
    }

    public void updateStatistics () {
        int tot = feedbackManager.getFeedbackList().size();
        int done = feedbackManager.getDoneFeedbackList().size();

        numFeedback.setText(tot + "");
        numDone.setText(done + "");
        numNotDone.setText(feedbackManager.getNotDoneFeedbackList().size() + "");

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
        suppressClearDoneDialog = exportFeedback(feedbackList, true, true);

        if (suppressClearDoneDialog) {
            //Reset after a little while
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(SUPPRESS_CONFIRMATION_DURATION),
                    ae -> suppressClearDoneDialog = false));
            timeline.play();
        }
    }

    public void clearDone () {
        List<Feedback> feedbackList = feedbackManager.getDoneFeedbackList();
        if (suppressClearDoneDialog || Tools.confirmDelete(feedbackList.size())) {
            feedbackManager.removeFeedback(feedbackList);
            updateStatusLists();
            removeFeedbackTabs(feedbackList);
        }
        updateStatistics();
    }

    public void exportAllNotDone () {
        List<Feedback> feedbackList = feedbackManager.getNotDoneFeedbackList();
        suppressClearNotDoneDialog = exportFeedback(feedbackList, true, true);

        //Reset after a little while
        if (suppressClearNotDoneDialog) {
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(SUPPRESS_CONFIRMATION_DURATION),
                    ae -> suppressClearNotDoneDialog = false));
            timeline.play();
        }
    }

    public void clearAllNotDone () {
        List<Feedback> feedbackList = feedbackManager.getNotDoneFeedbackList();
        if (suppressClearNotDoneDialog || Tools.confirmDelete(feedbackList.size())) {
            feedbackManager.removeFeedback(feedbackList);
            updateStatusLists();
            removeFeedbackTabs(feedbackList);
        }
        updateStatistics();
    }

    public String getAssignment () {
        String assignment = assignmentField.getText();

        if (assignment != null)
            assignment = assignment.replaceAll("\\s+", "");

        return assignment;
    }

    public void selectView (int i) {
        rootTabPane.getSelectionModel().select(i);
    }
    //endregion
}
