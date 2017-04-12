package gui;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import model.FeedbackManager;
import model.Pasta;
import model.UniqueArrayList;

import java.util.List;
import java.util.Optional;

/**
 * Created by Richard Sundqvist on 20/02/2017.
 */
public class PastaEditorController implements PastaViewController.PastaControllerListener {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea pastaEditingTextArea;
    @FXML
    private ListView currentContentTagView, allContentTagView, currentAssignTagView, allAssignTagView;
    @FXML
    private CheckBox autoTitleCheckBox;
    @FXML
    private Button saveChangesButton;
    @FXML
    private Label assignmentLabel;
    private String currentAssignment;
    private Pasta selectedPastaActual = null;
    private Pasta selectedPastaClone = null;
    private boolean autoTitle = true;
    private boolean autoSave = true;

    @FXML
    private PastaViewController pastaViewController = null;

    public void initialize (List<Pasta> pastaList, String currentAssignment) {
        pastaViewController.importPasta(pastaList);
        pastaViewController.setListener(this);
        System.out.println(currentAssignment);
        currentAssignment = FeedbackManager.parseAssignmentString(currentAssignment);
        this.currentAssignment = currentAssignment;
        currentAssignment = currentAssignment.length() == 0 ? "<None>" : currentAssignment;
        assignmentLabel.setText(currentAssignment);
        titleField.textProperty().addListener(event -> titleChanged());
        pastaEditingTextArea.textProperty().addListener(event -> contentChanged());
        update();
    }

    private void titleChanged () {
        if (autoSave)
            savePastaChanges();
    }

    private void contentChanged () {
        if (autoSave)
            savePastaChanges();
    }

    private void update () {
        clear();
        allContentTagView.getItems().addAll(pastaViewController.getPastaManager().getTagList());
        allAssignTagView.getItems().addAll(pastaViewController.getPastaManager().getAssignmentTagList());
        //Make sure the current assignment tag is available.
        if (currentAssignment != null && !currentAssignment.isEmpty()
                && !allAssignTagView.getItems().contains(currentAssignment))
            allAssignTagView.getItems().add(currentAssignment);
    }

    private void clear () {
        allContentTagView.getItems().clear();
        currentContentTagView.getItems().clear();
        allAssignTagView.getItems().clear();
        currentAssignTagView.getItems().clear();
        titleField.clear();
        pastaEditingTextArea.clear();
        selectedPastaClone = null;
        selectedPastaActual = null;
    }

    public void toggleAutoTitle (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        autoTitle = cb.isSelected();

        if (autoTitle) {
            titleField.setText(null);
            titleField.setDisable(true);
        } else {
            titleField.setDisable(false);
        }
    }

    public void toggleAutoSave (Event e) {
        CheckBox cb = (CheckBox) e.getSource();
        autoSave = cb.isSelected();
        saveChangesButton.setDisable(autoSave);
    }

    public void addContentTag () {
        String selectedItem = (String) allContentTagView.getSelectionModel().getSelectedItem();

        if (selectedPastaClone != null && selectedItem != null)
            if (selectedPastaClone.getContentTags().add(selectedItem)) {
                currentContentTagView.getItems().add(selectedItem);
                if (autoSave)
                    savePastaChanges();
            }
    }

    public void removeContentTag () {
        String selectedItem = (String) currentContentTagView.getSelectionModel().getSelectedItem();
        if (selectedPastaClone != null && selectedItem != null) {
            selectedPastaClone.getContentTags().remove(selectedItem);
            currentContentTagView.getItems().remove(selectedItem);
            if (autoSave)
                savePastaChanges();
        }
    }

    public void addAssignTag () {
        String selectedItem = (String) allAssignTagView.getSelectionModel().getSelectedItem();

        if (selectedPastaClone != null && selectedItem != null)
            if (selectedPastaClone.getAssignmentTags().add(selectedItem)) {
                currentAssignTagView.getItems().add(selectedItem);
                if (autoSave)
                    savePastaChanges();
            }
    }

    public void removeAssignTag () {
        String selectedItem = (String) currentAssignTagView.getSelectionModel().getSelectedItem();
        if (selectedPastaClone != null && selectedItem != null) {
            selectedPastaClone.getAssignmentTags().remove(selectedItem);
            currentAssignTagView.getItems().remove(selectedItem);
            if (autoSave)
                savePastaChanges();
        }
    }

    public void newContentTag () {
        TextInputDialog dialog = new TextInputDialog("New content tag");
        dialog.setTitle("New Tag");
        dialog.setHeaderText("Create new content tag.");
        dialog.setContentText("Enter new tag: ");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null) {
            String newTag = result.get();
            newTag = newTag.toLowerCase().trim();
            if (!allContentTagView.getItems().contains(newTag)) {
                allContentTagView.getItems().add(0, newTag);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The tag \"" + newTag + "\" already exists.", ButtonType.OK);
                alert.setHeaderText("Tag already exists.");
                alert.setTitle("Duplicate found");
                alert.show();
            }
        }
    }

    public void newAssignTag () {
        TextInputDialog dialog = new TextInputDialog("New assignment tag");
        dialog.setTitle("New Tag");
        dialog.setHeaderText("Create new assignment tag.");
        dialog.setContentText("Enter new tag: ");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null) {
            String newTag = result.get();
            newTag = newTag.toLowerCase().trim();
            if (!allAssignTagView.getItems().contains(newTag)) {
                allAssignTagView.getItems().add(0, newTag);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The tag \"" + newTag + "\" already exists.", ButtonType.OK);
                alert.setHeaderText("Tag already exists.");
                alert.setTitle("Duplicate found");
                alert.show();
            }
        }
    }

    public void createNewPasta () {
        Pasta newPasta = pastaViewController.createNew();
        select(newPasta);
    }

    public void exportAllPasta () {
        pastaViewController.exportAllPasta();
    }

    public void clearAllPasta () {
        pastaViewController.clearAllPasta();
        update();
    }

    public void savePastaChanges () {
        if (selectedPastaActual == null) return;

        selectedPastaActual.setContent(pastaEditingTextArea.getText());
        selectedPastaActual.setLastModificationDate();
        selectedPastaActual.getContentTags().clear();
        selectedPastaActual.getContentTags().addAll(selectedPastaClone.getContentTags());
        selectedPastaActual.getAssignmentTags().clear();
        selectedPastaActual.getAssignmentTags().addAll(selectedPastaClone.getAssignmentTags());

        if (autoTitle)
            selectedPastaActual.setTitle(null);
        else
            selectedPastaActual.setTitle(titleField.getText());

        pastaViewController.getPastaManager().updateTags();
        pastaViewController.updateFilters();
        pastaViewController.refreshListView();
    }

    @Override
    public void select (Pasta pasta) {
        if (pasta == selectedPastaActual) return;

        selectedPastaActual = pasta;
        selectedPastaClone = new Pasta(pasta);

        autoTitle = pasta.isAutomaticTitle();
        titleField.setDisable(autoTitle);
        if (autoTitle)
            titleField.setText(null);
        else
            titleField.setText(selectedPastaClone.getTitle());

        autoTitleCheckBox.setSelected(pasta.isAutomaticTitle());

        pastaEditingTextArea.setText(selectedPastaClone.getContent());
        currentContentTagView.getItems().clear();
        currentAssignTagView.getItems().clear();
        currentContentTagView.getItems().addAll(selectedPastaClone.getContentTags());
        currentAssignTagView.getItems().addAll(selectedPastaClone.getAssignmentTags());
    }

    public String getCurrentAssignment () {
        return currentAssignment;
    }

    public void importPasta () {
        pastaViewController.importPasta();
    }

    public UniqueArrayList<Pasta> getPastaList () {
        return pastaViewController.getPastaList();
    }
}
