package gui.pasta;

import gui.ContentText;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import model.FeedbackManager;
import model.ManagerListener;
import model.Pasta;
import model.PastaManager;
import model.UniqueArrayList;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Created by Richard Sundqvist on 20/02/2017. */
public class PastaEditorController implements PastaViewController.PastaControllerListener {
  private static String manualTitlePromt = "Enter title.",
      autoTitlePromt = "Enter content for automatic title.";

  @FXML private TextField titleField;
  @FXML
  private ListView currentContentTagView, allContentTagView, currentAssignTagView, allAssignTagView;
  @FXML private CheckBox autoTitleCheckBox, enforceNewlineCheckBox, purgeEmptyCheckBox;
  @FXML private Label assignmentLabel;
  @FXML private BorderPane borderPane;

  private ContentText contentText;
  private String assignment;
  private Pasta pasta = null; // Currently selected item
  private boolean autoTitle = true;
  private PastaManager tmpManager, realManager;

  @FXML private PastaViewController pastaViewController = null;
  private ManagerListener listener;

  private static void newTag(String defaultValue, List<String> currentTags) {
    TextInputDialog dialog = new TextInputDialog(defaultValue);
    dialog.setTitle("New Tag");
    dialog.setHeaderText("Create new tag");
    dialog.setContentText("Enter new tag: ");
    Optional<String> result = dialog.showAndWait();

    if (result.isPresent() && result.get() != null) {
      String newTag = result.get();
      newTag = newTag.toLowerCase().trim();
      if (!currentTags.contains(newTag)) {
        currentTags.add(0, newTag);
      } else {
        Alert alert =
            new Alert(
                Alert.AlertType.INFORMATION,
                "The tag \"" + newTag + "\" already exists.",
                ButtonType.OK);
        alert.setHeaderText("Tag already exists.");
        alert.setTitle("Duplicate found");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
      }
    }
  }

  public void initialize() {
    contentText = new ContentText();
    contentText.setDisable(true);
    borderPane.setCenter(new VirtualizedScrollPane(contentText));

    pastaViewController.initialize(this, false);
    tmpManager = pastaViewController.getPastaManager();
    titleField.textProperty().addListener(event -> titleChanged());
    update();
  }

  public void onSave() {
    UniqueArrayList<Pasta> tmpList = tmpManager.getPastaList();
    UniqueArrayList<Pasta> newList = new UniqueArrayList();

    boolean enforceNewline = enforceNewlineCheckBox.isSelected();
    boolean purgeEmpty = purgeEmptyCheckBox.isSelected();

    if (enforceNewline || purgeEmpty) {
      for (Pasta tmpPasta : tmpList) {
        String content = tmpPasta.getContent();

        if (purgeEmpty && content.replaceAll("\\s+", "").isEmpty()) continue;

        if (enforceNewline && !content.endsWith("\n")) {
          content = content + "\n";
          tmpPasta.setContent(content);
        }

        newList.add(tmpPasta);
      }
    } else {
      System.out.println("PastaEditorController.onSave");
      newList = tmpList;
    }

    realManager.setPastaList(newList);
    listener.close(true);
  }

  public void onDiscard() {
    listener.close(false);
  }

  public void initialize(ManagerListener listener, PastaManager pastaManager, String assignment) {

    realManager = pastaManager;
    this.listener = listener;
    this.assignment = assignment;

    List<Pasta> pastaClones = new ArrayList<>(realManager.getPastaList().size());
    realManager.getPastaList().forEach(orig -> pastaClones.add(orig.clone()));
    pastaViewController.importPasta(pastaClones);

    // Available assignment tags
    List<String> aaTags = allAssignTagView.getItems();
    aaTags.addAll(realManager.getAssignmentTagList());
    if (assignment != null && !assignment.isEmpty() && !aaTags.contains(assignment))
      aaTags.add(assignment);

    // Available content tags
    allContentTagView.getItems().setAll(realManager.getTagList());

    // Assignment label
    assignment = FeedbackManager.parseAssignmentString(assignment);
    assignment = assignment.isEmpty() ? "<None>" : assignment;
    assignmentLabel.setText(assignment);
  }

  private void titleChanged() {
    savePastaChanges();
  }

  private void update() {
    clear();
    allContentTagView.getItems().addAll(tmpManager.getTagList());
    List<String> allAssignmentTags = allAssignTagView.getItems();
    allAssignmentTags.addAll(tmpManager.getAssignmentTagList());
  }

  private void clear() {
    allContentTagView.getItems().clear();
    currentContentTagView.getItems().clear();
    allAssignTagView.getItems().clear();
    currentAssignTagView.getItems().clear();
    titleField.clear();
    contentText.clear();
    pasta = null;
  }

  public void toggleAutoTitle(Event e) {
    CheckBox cb = (CheckBox) e.getSource();
    autoTitle = cb.isSelected();
    updateTitleField(pasta);
  }

  private void updateTagsLists() {
    if (pasta == null) {
      currentAssignTagView.getItems().clear();
      currentContentTagView.getItems().clear();
    } else {
      currentAssignTagView.getItems().setAll(pasta.getAssignmentTags());
      currentContentTagView.getItems().setAll(pasta.getContentTags());
    }
  }

  public void newContentTag() {
    newTag("New content tag", allContentTagView.getItems());
  }

  public void newAssignTag() {
    newTag("New assignment tag", allAssignTagView.getItems());
  }

  public void createNewPasta() {
    Pasta newPasta = pastaViewController.createNew();
    select(newPasta);
    contentText.requestFocus();
  }

  public void exportAllPasta() {
    pastaViewController.exportAllPasta();
  }

  public void clearAllPasta() {
    pastaViewController.clearAllPasta();
    update();
    select(null);
  }

  public void savePastaChanges() {
    if (pasta == null) return;

    pasta.setContent(contentText.getText());
    if (autoTitle) pasta.setTitle(null);
    else pasta.setTitle(titleField.getText());

    pasta.setLastModificationDate();

    pastaViewController.getPastaManager().updateTags();
    pastaViewController.updateFilters();
    pastaViewController.refreshListView();
  }

  @Override
  public void select(Pasta pasta) {
    this.pasta = pasta;
    if (pasta == null) {
      contentText.setText("");
      contentText.setDisable(true);
      return;
    }
    contentText.setDisable(false);
    contentText.setContent(pasta);

    autoTitle = pasta.isAutomaticTitle();
    updateTitleField(pasta);

    autoTitleCheckBox.setSelected(pasta.isAutomaticTitle());

    contentText.setText(pasta.getContent() + "");
    updateTagsLists();
  }

  private void updateTitleField(Pasta pasta) {
    titleField.setEditable(!autoTitle);
    titleField.setPromptText(autoTitle ? autoTitlePromt : manualTitlePromt);
    if (autoTitle) {
      if (pasta != null && pasta.hasContent()) titleField.setText(pasta.getTitle());
      else titleField.setText(null);
    } else {
      titleField.setText(pasta == null ? pasta.getTitle() : null);
    }
  }

  @Override
  public void quickInsert(Pasta pasta) {
    Alert alert = new Alert(Alert.AlertType.ERROR, "Not supported in this view.", ButtonType.CLOSE);
    alert.setHeaderText("Not supported");
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
    alert.showAndWait();
  } // Do nothing

  public String getAssignment() {
    return assignment;
  }

  public void importPasta() {
    pastaViewController.importPasta();
  }

  // =================================================================================================================//
  // Remove tags
  // =================================================================================================================//
  public void removeContentTag() {
    String tag = (String) currentContentTagView.getSelectionModel().getSelectedItem();
    if (pasta != null && tag != null) removeTag(tag, pasta.getContentTags());
  }

  public void removeAssignTag() {
    String tag = (String) currentAssignTagView.getSelectionModel().getSelectedItem();
    if (pasta != null && tag != null) removeTag(tag, pasta.getAssignmentTags());
  }

  private void removeTag(String oldTag, List<String> currentTags) {
    if (currentTags.remove(oldTag)) updateTagsLists();
  }

  // =================================================================================================================//
  // Add tags
  // =================================================================================================================//
  public void addContentTag() {
    String tag = (String) allContentTagView.getSelectionModel().getSelectedItem();
    if (pasta != null && tag != null) addTag(tag, pasta.getContentTags());
  }

  public void addAssignTag() {
    String tag = (String) allAssignTagView.getSelectionModel().getSelectedItem();
    if (pasta != null && tag != null) addTag(tag, pasta.getAssignmentTags());
  }

  private void addTag(String newTag, List<String> currentTags) {
    if (currentTags.add(newTag)) updateTagsLists();
  }
}
