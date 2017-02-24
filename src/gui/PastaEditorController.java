package gui;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
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
    private ListView currentPastaTagListView, availablePastaTagListView;
    @FXML
    private Label assignmentLabel;
    @FXML
    private CheckBox autoToggleCheckBox;
    @FXML
    private ToggleButton assignmentToggleButton;
    private String assignment;
    private Pasta selectedPastaActual = null;
    private Pasta selectedPastaClone = null;
    private boolean autoTitle = true;

    @FXML
    private PastaViewController pastaViewController = null;

    public void initialize (List<Pasta> pastaList, String assignment) {
        pastaViewController.importPasta(pastaList);
        pastaViewController.setListener(this);
        System.out.println(assignment);
        assignment = FeedbackManager.parseAssignmentString(assignment);
        System.out.println(assignment);
        assignmentLabel.setText(assignment);
        this.assignment = assignment;
        update();
    }

    private void update () {
        clear();
        availablePastaTagListView.getItems().addAll(pastaViewController.getPastaManager().getTagList());
    }

    private void clear () {
        availablePastaTagListView.getItems().clear();
        currentPastaTagListView.getItems().clear();
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

    public void addPastaTag () {
        String selectedItem = (String) availablePastaTagListView.getSelectionModel().getSelectedItem();

        if (selectedPastaClone != null && selectedItem != null)
            if (selectedPastaClone.getContentTags().add(selectedItem)) {
                currentPastaTagListView.getItems().add(selectedItem);
            }
    }

    public void removePastaTag () {
        String selectedItem = (String) currentPastaTagListView.getSelectionModel().getSelectedItem();
        if (selectedPastaClone != null && selectedItem != null) {
            selectedPastaClone.getContentTags().remove(selectedItem);
            currentPastaTagListView.getItems().remove(selectedItem);
        }
    }

    public void createNewTag () {
        TextInputDialog dialog = new TextInputDialog("New tag");
        dialog.setTitle("Create new tag.");
        dialog.setHeaderText("Create new tag.");
        dialog.setContentText("Enter new tag: ");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get() != null) {
            String newTag = result.get();
            newTag = newTag.toLowerCase().trim();
            if (!availablePastaTagListView.getItems().contains(newTag)) {
                availablePastaTagListView.getItems().add(0, newTag);
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "The tag \"" + newTag + "\" already exists.", ButtonType.OK);
                alert.setHeaderText("Tag already exists.");
                alert.setTitle("Duplicate found");
                alert.show();
            }
        }
    }

    public void createNewPasta () {
        Pasta newPasta = new Pasta();
        newPasta.setContent("New");
        select(newPasta);

        List<Pasta> pastaList = new UniqueArrayList<>();
        pastaList.add(newPasta);
        pastaViewController.importPasta(pastaList);
    }

    public void exportAllPasta () {
        pastaViewController.exportAllPasta();
    }

    public void clearAllPasta () {
        pastaViewController.clearAllPasta();
        update();
    }

    public void savePastaChanges () {
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

        pastaViewController.getPastaManager().updateFilters();
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

        assignmentToggleButton.setSelected(pasta.getAssignmentTags().contains(assignment));
        autoToggleCheckBox.setSelected(pasta.isAutomaticTitle());

        pastaEditingTextArea.setText(selectedPastaClone.getContent());
        currentPastaTagListView.getItems().clear();
        currentPastaTagListView.getItems().addAll(selectedPastaClone.getContentTags());
    }

    public void toggleAssignmentTag () {
        if (assignmentToggleButton.isSelected())
            selectedPastaClone.getAssignmentTags().add(assignment);
        else
            selectedPastaClone.getAssignmentTags().remove(assignment);
    }

    @Override
    public String getAssignment () {
        return assignment;
    }

    public void importPasta () {
        pastaViewController.importPasta();
    }

    public UniqueArrayList<Pasta> getPastaList () {
        return pastaViewController.getPastaList();
    }
}
