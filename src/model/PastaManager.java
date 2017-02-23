package model;

import gui.Tools;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.util.ArrayList;
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

    private boolean anyTag = true; //Determines if UNION or INTERSECT filtering is used.
    private boolean negate = false; //Exclude all items which match (negation/complement).
    //endregion

    //region Control
    // ================================================================================= //
    // Control
    // ================================================================================= //

    /**
     * Clear the manager.
     */
    public void clear () {
        pastaList.clear();
        filteredPastaList.clear();
        tagList.clear();
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
     * Adds new filters from the list to the manager.
     *
     * @param pastaList A list of Pasta.
     * @return {@code true} if filters were added.
     */
    public boolean addFilters (List<Pasta> pastaList) {
        boolean changed = false;

        for (Pasta pasta : pastaList)
            if (tagList.addAll(pasta.getContentTags()))
                changed = true;

        return changed;
    }

    /**
     * Force updating of filters of items contained in {@link #pastaList}
     */
    public void updateFilters () {
        tagList.clear();
        filteredPastaList.clear();

        addFilters(pastaList);
    }

    public void updateFilteredList () {
        filteredPastaList.clear();

        if (!activeTagList.isEmpty()) {
            List<Pasta> filteredPastaList = PastaFilter.filter(pastaList, activeTagList, anyTag, negate);
            this.filteredPastaList.addAll(filteredPastaList);
        } else {
            filteredPastaList.addAll(pastaList);
        }
    }

    /**
     * Removes Pasta from the manager
     *
     * @return {@code true} if Pasta was removed.
     */
    public boolean removePasta (Pasta pasta) {
        boolean changed = pastaList.remove(pasta);

        if (changed) {
            List<Pasta> pastaList = new ArrayList<>(1);
            pastaList.add(pasta);
            removeFilters(pastaList);
        }

        return changed;
    }

    /**
     * Remove all selected pasta from the manager.
     *
     * @param pastaList The pasta to remove.
     * @return {@code} true if pasta was removed, false otherwise.
     */
    public boolean removePasta (List<Pasta> pastaList) {
        boolean changed = this.pastaList.removeAll(pastaList);

        if (changed)
            removeFilters(pastaList);

        return changed;
    }

    private void removeFilters (List<Pasta> removedPastaList) {
        UniqueArrayList<String> unwantedFilterList = new UniqueArrayList<>();

        for (Pasta pasta : removedPastaList)
            unwantedFilterList.addAll(pasta.getContentTags());

        for (Pasta pasta : pastaList)
            unwantedFilterList.removeAll(pasta.getContentTags());

        tagList.removeAll(unwantedFilterList);
    }

    /**
     * Produce a preview of a Pasta item, displayed in an Alert dialog.
     *
     * @param pasta The Pasta to preview.
     */
    public static void preview (Pasta pasta) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feedback Preview");
        alert.setHeaderText("Feedback preview");
        alert.setContentText("Output when exporting as a .txt-file:");

        TextArea textArea = new TextArea(pasta.getContent());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        Label label = new Label("Tags: ");
        label.setMaxHeight(Double.MAX_VALUE);

        String pastaContentTags;
        if (pasta.getContentTags().isEmpty()) {
            pastaContentTags = "<no tags>";
        } else {
            pastaContentTags = pasta.getContentTags().toString();
            pastaContentTags = pastaContentTags.substring(1, pastaContentTags.length() - 1);
        }
        TextField textField = new TextField(pastaContentTags);
        textField.setEditable(false);
        textField.setMaxWidth(Double.MAX_VALUE);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(label, textField);
        HBox.setHgrow(textField, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        expContent.add(hBox, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }
    //endregion

    //region Import/export
    // ================================================================================= //
    // Import/export
    // ================================================================================= //

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
     * Attempt to import Pasta from the default location, defined by {@link Tools#SAVED_PASTA_URI}.
     *
     * @return The imported pasta, or {@code null} if nothing was imported.
     */
    public List<Pasta> importSavedPasta () {
        List<Pasta> importedPastaList = Tools.importSavedPasta();
        return importPasta(importedPastaList);
    }

    /**
     * Attempt to import Pasta from the default location, defined by {@link Tools#DEFAULT_PASTA_URI}.
     *
     * @return The imported pasta, or {@code null} if nothing was imported.
     */
    public List<Pasta> importDefaultPasta () {
        List<Pasta> importedPastaList = Tools.importDefaultPasta();
        return importPasta(importedPastaList);
    }

    /**
     * Attempt to export Pasta to the default location, defined by {@link Tools#SAVED_PASTA_URI}.
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
        addFilters(importedPastaList);
        updateFilteredList();
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
     * Displays a dialog and attempts to export the pasta contained in the list.
     *
     * @param exportPastaList The list to export.
     */
    public static void exportPasta (List<Pasta> exportPastaList) {
        IO.exportPastaJSON(exportPastaList);
    }

    /**
     * Shows a file choice dialog and exports a single Pasta item, wrapped in a list.
     *
     * @param pasta The pasta item to export.
     */
    public static void exportPasta (Pasta pasta) {
        IO.exportPastaJSON(pasta);
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
        filteredPastaList.clear();
        tagList.clear();
        activeTagList.clear();

        this.pastaList = pastaList;
        addFilters(pastaList);
    }

    public UniqueArrayList<String> getTagList () {
        return tagList;
    }

    public UniqueArrayList<Pasta> getFilteredPastaList () {
        return filteredPastaList;
    }

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
    //endregion
}
