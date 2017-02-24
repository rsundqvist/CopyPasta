package gui;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import model.IO;
import model.Pasta;
import model.PastaManager;
import model.UniqueArrayList;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PastaViewController {
    //region Field
    // ================================================================================= //
    // Field
    // ================================================================================= //
    private PastaControllerListener listener;
    @FXML
    private ListView listView = null;
    @FXML
    private TextArea previewTextArea = null;
    @FXML
    private FlowPane filterFlowPane;
    @FXML
    private TextField searchField;

    private PastaManager pastaManager = new PastaManager();
    //endregion

    public PastaViewController () {
        //Called before FXML injection - not used
    }

    public void setPastaManager (PastaManager pastaManager) {
        this.pastaManager = pastaManager;
        pastaManager.updateFilters();
        pastaManager.updateFilteredList();
    }

    public void initialize () {
        //Called after FXML injection
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        searchField.textProperty().addListener(event -> searchPasta());
    }

    private void searchPasta () {
        String searchString = searchField.getText();
        List<String> searchTerms = Arrays.asList(searchString.split(","));
        pastaManager.search(searchTerms);
        updateFilteredList();
    }

    public void initialize (PastaControllerListener listener) {
        setListener(listener);

        List<Pasta> importedPastaList;
        importedPastaList = pastaManager.importSavedPasta();
        if (importedPastaList == null)
            importedPastaList = pastaManager.importDefaultPasta();

        if (importedPastaList != null) {
            listView.getItems().addAll(importedPastaList);
            updateFilters();
        }
    }

    public void setListener (PastaControllerListener listener) {
        this.listener = listener;
    }

    protected void setPastaList (UniqueArrayList<Pasta> pastaList) {
        pastaManager.clear();
        filterFlowPane.getChildren().clear();
        listView.getItems().clear();

        pastaManager.setPastaList(pastaList);
        listView.getItems().clear();
        listView.getItems().addAll(pastaList);
        updateFilters();
    }

    public void onMouseClicked () {
        Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
        if (pasta != null) {
            previewTextArea.setText(pasta.getContent());
            if (listener != null)
                listener.select(pasta);
        }
    }

    public void copyItem () {
        Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
        PastaManager.copyPastaContentsToClipboard(pasta);
    }

    public void exportAllPasta () {
        pastaManager.exportPasta();
    }

    public void exportPastaJSON () {
        List<Pasta> pastaList = listView.getSelectionModel().getSelectedItems();
        PastaManager.exportPasta(pastaList);
    }

    public void exportPastaTXT () {
        List<Pasta> pastaList = listView.getSelectionModel().getSelectedItems();
        exportPastaTXT(pastaList);
    }

    private static void exportPastaTXT (List<Pasta> pastaList) {
        String gatheredContent = PastaManager.gatherContent(pastaList);
        File file = IO.showTXTSaveDialog("pasta");
        IO.printStringToFile(gatheredContent, file);
    }

    public void importPasta () {
        List<Pasta> importedPastaList = IO.importPasta();
        importPasta(importedPastaList);
    }

    public void importPasta (List<Pasta> importedPastaList) {
        importedPastaList = pastaManager.importPasta(importedPastaList);
        if (importedPastaList != null) {
            updateFilters();
            listView.getItems().addAll(importedPastaList); //TODO
        }
    }

    public void shutdown () {
        pastaManager.exportSavedPasta();
    }

    public UniqueArrayList<Pasta> getPastaList () {
        return pastaManager.getPastaList();
    }

    public void clearAllPasta () {
        int numItems = pastaManager.getPastaList().size();

        if (Tools.confirmDelete(numItems)) {
            pastaManager.clear();
            filterFlowPane.getChildren().clear();
            listView.getItems().clear();
        }
    }

    public void clearTagFilters () {
        pastaManager.clearTagFilters();
        updateFilters();
        updateFilteredList();
    }

    public void updateFilters () {
        filterFlowPane.getChildren().clear();

        for (String tag : pastaManager.getTagList()) {
            CheckBox cb = new CheckBox(tag);
            cb.setPadding(new Insets(0, 0, 0, 2));
            cb.setOnAction(event -> onTagChanged(cb));
            filterFlowPane.getChildren().add(cb);
        }
    }

    public void matchAnyTag () {
        pastaManager.setAnyTag(true);
        updateFilteredList();
    }

    public void matchAllTag () {
        pastaManager.setAnyTag(false);
        updateFilteredList();
    }

    private void onTagChanged (CheckBox cb) {

        boolean changed;
        if (cb.isSelected())
            changed = pastaManager.addFilterTag(cb.getText());
        else
            changed = pastaManager.removeFilterTag(cb.getText());

        if (changed)
            updateFilteredList();
    }

    public void refreshListView () {
        listView.refresh();
    }

    private void updateFilteredList () {
        //pastaManager.updateFilteredList(); //TODO probably not needed?

        listView.getItems().clear();
        listView.getItems().addAll(pastaManager.getFilteredPastaList());
    }

    public void delete () {
        List<Pasta> selectedItems = listView.getSelectionModel().getSelectedItems();
        int numberOfItems = selectedItems.size();
        if (numberOfItems > 1) {
            String contentText = "Really delete all selected Pasta? There are currently " +
                    numberOfItems + " items selected.";
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);
            alert.setHeaderText("Really delete all selected Pasta?");

            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK)
                return;
        }

        pastaManager.removePasta(selectedItems);
        listView.getItems().removeAll(selectedItems);
    }

    public void preview () {
        Pasta pasta = (Pasta) listView.getSelectionModel().getSelectedItem();
        if (pasta != null)
            PastaManager.preview(pasta);
    }

    public void toggleNotTag (Event event) {
        CheckBox cb = (CheckBox) event.getSource();
        pastaManager.setNegate(cb.isSelected());
        updateFilteredList();
    }

    public PastaManager getPastaManager () {
        return pastaManager;
    }

    public void toggleCurrentAssignmentOnly (Event event) {
        if (listener == null)
            return;

        ToggleButton toggleButton = (ToggleButton) event.getSource();

        if (toggleButton.isSelected()) {
            String assignment = listener.getAssignment();
            pastaManager.setAssignment(assignment);
        } else {
            pastaManager.setAssignment(null);
        }
        updateFilteredList();
    }

    // ====================================================
    // Interface declaration
    // ====================================================

    /**
     * Listener interface for the controller.
     */
    public interface PastaControllerListener {
        /**
         * Called when an item is selected.
         *
         * @param pasta The selected item.
         */
        void select (Pasta pasta);

        /**
         * Called when {@link #toggleCurrentAssignmentOnly} is called by the GUI.
         *
         * @return The assignment to filter for.
         */
        String getAssignment ();
    }
}
