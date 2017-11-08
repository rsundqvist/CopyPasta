package model;

import gui.Tools;
import gui.feedback.JavaCodeArea;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Richard Sundqvist
 */
public class PastaManager {
    //region Field
    // ================================================================================= //
    // Field
    // ================================================================================= //
    private UniqueArrayList<Pasta> pastaList = new UniqueArrayList<>();
    private UniqueArrayList<Pasta> filteredPastaList = new UniqueArrayList<>();
    private UniqueArrayList<String> tagList = new UniqueArrayList<>();
    private UniqueArrayList<String> activeTagList = new UniqueArrayList<>();
    private UniqueArrayList<String> assignmentTagList = new UniqueArrayList<>();
    private String currentAssignment = null;

    private boolean anyTag = true; //Determines if UNION or INTERSECT filtering is used.
    private boolean negate = false; //Exclude all items which match (negation/complement).
    //endregion

    //region Control
    // ================================================================================= //
    // Control
    // ================================================================================= //

    /**
     * Gather all the contents of the list as a single string.
     *
     * @param pastaList The list to gather from.
     * @return A single string of content.
     */
    public static String gatherContent (List<Pasta> pastaList) {
        StringBuilder sb = new StringBuilder();

        for (Pasta pasta : pastaList)
            sb.append(pasta.getContent().trim() + "\n\n");

        return sb.toString();
    }

    /**
     * Produce a preview of a Pasta item, displayed in an Alert dialog.
     *
     * @param pasta The Pasta to preview.
     */
    public static void preview (Pasta pasta) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(ButtonType.CLOSE);
        alert.setTitle("Pasta Preview");
        alert.setHeaderText("Pasta preview");
        alert.setContentText("Output when exporting as a .txt-file:");

        JavaCodeArea jca = new JavaCodeArea(pasta.getContent());
        jca.setEditable(false);
        jca.setWrapText(true);

        jca.setPrefSize(600, 300);
        jca.setMaxWidth(Double.MAX_VALUE);
        jca.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(jca, Priority.ALWAYS);
        GridPane.setHgrow(jca, Priority.ALWAYS);

        //Content tags
        Label contentLabel = new Label("Content tags:         ");
        contentLabel.setMaxHeight(Double.MAX_VALUE);

        String pastaContentTags;
        if (pasta.getContentTags().isEmpty()) {
            pastaContentTags = "<no tags>";
        } else {
            pastaContentTags = pasta.getContentTags().toString();
            pastaContentTags = pastaContentTags.substring(1, pastaContentTags.length() - 1);
        }
        TextField contentTagsTextField = new TextField(pastaContentTags);
        contentTagsTextField.setEditable(false);
        contentTagsTextField.setMaxWidth(Double.MAX_VALUE);

        HBox contentTagsHBox = new HBox();
        contentTagsHBox.getChildren().addAll(contentLabel, contentTagsTextField);
        HBox.setHgrow(contentTagsTextField, Priority.ALWAYS);

        //Assignment tags
        Label assignmentLabel = new Label("Assignment tags:   ");
        assignmentLabel.setMaxHeight(Double.MAX_VALUE);

        String pastaAssignmentTags;
        if (pasta.getAssignmentTags().isEmpty()) {
            pastaAssignmentTags = "<no tags>";
        } else {
            pastaAssignmentTags = pasta.getAssignmentTags().toString();
            pastaAssignmentTags = pastaAssignmentTags.substring(1, pastaAssignmentTags.length() - 1);
        }
        TextField assignmentTagsTextField = new TextField(pastaAssignmentTags);
        assignmentTagsTextField.setEditable(false);
        assignmentTagsTextField.setMaxWidth(Double.MAX_VALUE);

        HBox assignmentTagsHBox = new HBox();
        assignmentTagsHBox.getChildren().addAll(assignmentLabel, assignmentTagsTextField);
        HBox.setHgrow(assignmentTagsTextField, Priority.ALWAYS);

        //Add children
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new VirtualizedScrollPane<>(jca), 0, 0);
        expContent.add(contentTagsHBox, 0, 1);
        expContent.add(assignmentTagsHBox, 0, 2);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }

    /**
     * Copy the contents of a Pasta object to the system clipboard.
     *
     * @param pasta The pasta to copy.
     * @return {@code true} if content was copied to clipboard.
     */
    public static boolean copyPastaContentsToClipboard (Pasta pasta) {
        if (pasta == null) return false;

        String content = pasta.getContent();
        if (content == null || content.isEmpty())
            return false;

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        clipboard.setContent(clipboardContent);

        return true;
    }

    /**
     * Displays a dialog and attempts to export the pasta contained in the list.
     *
     * @param exportPastaList The list to export.
     */
    public static void exportPasta (List<Pasta> exportPastaList) {
        IO.exportPastaJSON(Tools.SAVE_FOLDER, exportPastaList);
    }

    /**
     * Shows a file choice dialog and exports a single Pasta item, wrapped in a list.
     *
     * @param pasta The pasta item to export.
     */
    public static void exportPasta (Pasta pasta) {
        IO.exportPastaJSON(Tools.SAVE_FOLDER, pasta);
    }

    /**
     * Clear the manager.
     */
    public void clear () {
        pastaList.clear();
        filteredPastaList.clear();
        tagList.clear();
        assignmentTagList.clear();
        activeTagList.clear();
    }

    /**
     * Clear tag filters.
     */
    public void clearTagFilters () {
        activeTagList.clear();
        filteredPastaList.addAll(pastaList);
    }

    /**
     * Gather all the contents of the manager as a single string.
     *
     * @return A single string of content.
     */
    public String gatherContent () {
        return gatherContent(pastaList);
    }

    /**
     * Adds new filters from the list to the manager.
     *
     * @param pastaList A list of Pasta.
     * @return {@code true} if filters were added.
     */
    public boolean addContentTags (List<Pasta> pastaList) {
        boolean changed = false;

        for (Pasta pasta : pastaList)
            if (tagList.addAll(pasta.getContentTags()))
                changed = true;

        return changed;
    }

    /**
     * Adds new filters from the list to the manager.
     *
     * @param pastaList A list of Pasta.
     * @return {@code true} if filters were added.
     */
    public boolean addAssignmentTags (List<Pasta> pastaList) {
        boolean changed = false;

        for (Pasta pasta : pastaList)
            if (assignmentTagList.addAll(pasta.getAssignmentTags()))
                changed = true;

        return changed;
    }

    /**
     * Force updating of filters of items contained in {@link #pastaList}
     */
    public void updateTags () {
        tagList.clear();
        assignmentTagList.clear();
        filteredPastaList.clear();

        addContentTags(pastaList);
        addAssignmentTags(pastaList);
    }

    /**
     * Update the filtered list. Will search {@link Pasta#content}, {@link Pasta#title}, {@link Pasta#contentTags} and
     * {@link Pasta#assignmentTags}, ignoring tag settings.
     */
    public void search (List<String> searchTerms) {
        filteredPastaList.clear();

        if (searchTerms == null || searchTerms.isEmpty() ||
                (searchTerms.size() == 1 && searchTerms.get(0).isEmpty())) {
            filteredPastaList.addAll(pastaList);
        } else {
            List<Pasta> filteredList = PastaFilter.search(pastaList, searchTerms);
            filteredPastaList.addAll(filteredList);
        }
    }

    /**
     * Update the filtered lists. If {@link #currentAssignment} is set, all items which do not match the current currentAssignment
     * will be excluded from the filtered list as well.
     */
    public void updateFilteredList () {
        filteredPastaList.clear();

        if (activeTagList.isEmpty() && currentAssignment == null) {
            filteredPastaList.addAll(pastaList);
            return;
        }

        List<Pasta> filteredPastaList;

        if (!activeTagList.isEmpty()) {
            filteredPastaList = new ArrayList<>();
            List<Pasta> list = PastaFilter.filter(pastaList, activeTagList, anyTag, negate);
            filteredPastaList.addAll(list);
        } else {
            filteredPastaList = new ArrayList<>(pastaList);
        }

        if (currentAssignment != null) {
            List<Pasta> list = PastaFilter.filter(pastaList, currentAssignment, true);
            filteredPastaList.removeAll(list);
        }

        this.filteredPastaList.addAll(filteredPastaList);
    }
    //endregion

    //region Import/export
    // ================================================================================= //
    // Import/export
    // ================================================================================= //

    /**
     * Remove all selected pasta from the manager.
     *
     * @param pastaList The pasta to remove.
     * @return {@code} true if pasta was removed, false otherwise.
     */
    public boolean removePasta (List<Pasta> pastaList) {
        boolean changed = this.pastaList.removeAll(pastaList);

        if (changed) {
            removeContentTags(pastaList);
            removeAssignmentTags(pastaList);
        }

        return changed;
    }

    private void removeContentTags (List<Pasta> removedPastaList) {
        UniqueArrayList<String> rejectedItems = new UniqueArrayList<>();

        for (Pasta pasta : removedPastaList)
            rejectedItems.addAll(pasta.getContentTags());

        for (Pasta pasta : pastaList)
            rejectedItems.removeAll(pasta.getContentTags());

        tagList.removeAll(rejectedItems);
    }

    private void removeAssignmentTags (List<Pasta> removedPastaList) {
        UniqueArrayList<String> rejectedItems = new UniqueArrayList<>();

        for (Pasta pasta : removedPastaList)
            rejectedItems.addAll(pasta.getAssignmentTags());

        for (Pasta pasta : pastaList)
            rejectedItems.removeAll(pasta.getAssignmentTags());

        tagList.removeAll(rejectedItems);
    }

    /**
     * Attempt to import Pasta from the default location, defined by {@link Tools#AUTO_SAVE_PASTA_FILE}.
     *
     * @return The imported pasta, or {@code null} if nothing was imported.
     */
    public List<Pasta> importSavedPasta () {
        List<Pasta> importedPastaList = Tools.importSavedPasta();
        return importPasta(importedPastaList);
    }

    /**
     * Attempt to export Pasta to the default location, defined by {@link Tools#AUTO_SAVE_PASTA_FILE}.
     */
    public void exportSavedPasta () {
        Tools.exportSavedPasta(pastaList);
    }

    /**
     * Attempt to import Pasta from a list.
     *
     * @param importedPastaList The list of Pasta to import.
     * @return The imported pasta, or {@code null} if nothing was imported.
     */
    public List<Pasta> importPasta (List<Pasta> importedPastaList) {
        if (importedPastaList == null || importedPastaList.isEmpty())
            return null;

        importedPastaList = new ArrayList<>(importedPastaList);
        importedPastaList.removeAll(pastaList);
        pastaList.addAll(importedPastaList);
        addContentTags(importedPastaList);
        addAssignmentTags(importedPastaList);
        updateFilteredList();
        Collections.sort(pastaList);
        return importedPastaList;
    }

    /**
     * Shows a file choice dialog and imports all Pasta items from file.
     *
     * @return The imported pasta, or {@code null} if nothing was imported.
     */
    public List<Pasta> importPasta () {
        List<Pasta> importedPastaList = IO.importPasta();
        return importPasta(importedPastaList);
    }

    /**
     * Displays a dialog and attempts to export the pasta maintained by the manager.
     */
    public void exportPasta () {
        exportPasta(pastaList);
    }
    //endregion


    //region Getters and setters
    // ================================================================================= //
    // Getters and setters
    // ================================================================================= //
    public UniqueArrayList<String> getActiveTagList () {
        return activeTagList;
    }

    /**
     * Adds a tag from the list of active filter tags.
     *
     * @param tag The tag to add.
     * @return {@code true} if the list of filters changed.
     */
    public boolean addFilterTag (String tag) {
        boolean changed = activeTagList.add(tag);

        if (changed)
            updateFilteredList();

        return changed;
    }

    /**
     * Create a new Pasta item.
     *
     * @return A new Pasta item.
     */
    public Pasta createNew () {
        Pasta newPasta = new Pasta();

        List<Pasta> newList = new ArrayList<>(1);
        newList.add(newPasta);

        pastaList.remove(newPasta); //Ensure that the newly created item is the contained in pastaList
        importPasta(newList);

        return newPasta;
    }

    /**
     * Remove a tag from the list of active filter tags.
     *
     * @param tag The tag to add.
     * @return {@code true} if the list of filters changed.
     */
    public boolean removeFilterTag (String tag) {
        boolean changed = activeTagList.remove(tag);

        if (changed)
            updateFilteredList();

        return changed;
    }

    public UniqueArrayList<Pasta> getPastaList () {
        return pastaList;
    }

    public void setPastaList (UniqueArrayList<Pasta> pastaList) {
        clear();
        this.pastaList = pastaList;
        Collections.sort(pastaList);
        addContentTags(pastaList);
        addAssignmentTags(pastaList);
    }

    /**
     * Returns the list of content tags for the manager.
     *
     * @return A list of content tags.
     */
    public List<String> getTagList () {
        return Collections.unmodifiableList(tagList);
    }

    /**
     * Returns the list of assignment tags for the manager.
     *
     * @return A list of assignment tags.
     */
    public List<String> getAssignmentTagList () {
        return Collections.unmodifiableList(assignmentTagList);
    }

    /**
     * Returns the list of filtered items, updated whenever {@link #updateFilteredList} is called.
     *
     * @return The filtered list.
     */
    public List<Pasta> getFilteredPastaList () {
        return Collections.unmodifiableList(filteredPastaList);
    }

    /**
     * Returns the filtering mode for the manager (UNION or INTERSECT).
     *
     * @return {@code true} if using union filtering, {@code false} otherwise.
     */
    public boolean isAnyTag () {
        return anyTag;
    }

    /**
     * Update the filtering mode for the manager. Will call {@link #updateFilteredList()} if the mode changed.
     *
     * @param anyTag The new filtering mode. A {@code true} value implies union, {@code false} implies intersect.
     */
    public void setAnyTag (boolean anyTag) {
        if (this.anyTag != anyTag) {
            this.anyTag = anyTag;
            updateFilteredList();
        }
    }

    /**
     * Returns the negation setting used when this PastaManager applies a tag filter.
     *
     * @return {@code true} if the search criteria are negated.
     */
    public boolean isNegate () {
        return negate;
    }

    /**
     * Update the filtering mode for the manager. Will call {@link #updateFilteredList()} if the mode changed.
     *
     * @param negate The new filtering negation mode.
     */
    public void setNegate (boolean negate) {
        if (this.negate != negate) {
            this.negate = negate;
            updateFilteredList();
        }
    }

    /**
     * Returns the currentAssignment.
     *
     * @return The currentAssignment.
     */
    public String getAssignment () {
        return currentAssignment;
    }

    /**
     * Set the associated currentAssignment and calls {@link #updateFilteredList()}.
     *
     * @param assignment The new currentAssignment.
     */
    public void setAssignment (String assignment) {
        if (assignment == null || assignment.isEmpty())
            assignment = null;
        this.currentAssignment = assignment;
        updateFilteredList();
    }
    //endregion
}
