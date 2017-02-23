package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Feedback;
import model.IO;
import model.Pasta;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Richard Sundqvist on 19/02/2017.
 */
public abstract class Tools {

    public static final String DEFAULT_PASTA_URI = "/res/pasta/default.json";
    public static final String SAVED_PASTA_URI = "/res/pasta/saved.json";

    public static final String DEFAULT_TEMPLATE_URI = "/res/feedback/defaultTemplate.json";
    public static final String SAVED_TEMPLATE_URI = "/res/feedback/savedTemplate.json";
    public static final String SAVED_FEEDBACK_URI = "/res/feedback/savedFeedback.json";

    public static final String VERSION = "PRERELEASE";

    private Tools () {
    }

    //region Pasta
    // ================================================================================= //
    // Pasta
    // ================================================================================= //
    public static void exportSavedPasta (List<Pasta> pastaList) {
        File file = IO.getFileByURI(SAVED_PASTA_URI);
        IO.exportPastaJSON(pastaList, file);
    }

    public static List<Pasta> importSavedPasta () {
        File file = IO.getFileByURI(SAVED_PASTA_URI);
        return IO.importPasta(file);
    }

    public static List<Pasta> importDefaultPasta () {
        File file = IO.getFileByURI(DEFAULT_PASTA_URI);
        return IO.importPasta(file);
    }
    //endregion


    //region Template
    // ================================================================================= //
    // Template
    // ================================================================================= //
    public static void exportSavedFeedback (List<Feedback> feedbackList) {
        File file = IO.getFileByURI(SAVED_FEEDBACK_URI);
        IO.exportFeedbackAsJson(feedbackList, file);
    }

    public static List<Feedback> importSavedFeedback () {
        File file = IO.getFileByURI(SAVED_FEEDBACK_URI);
        return IO.importFeedback(file);
    }

    public static void exportSavedFeedbackTemplate (Feedback template) {
        File file = IO.getFileByURI(SAVED_TEMPLATE_URI);
        IO.exportSingleFeedbackAsJson(template, file);
    }

    public static Feedback importSavedFeedbackTemplate () {
        File file = IO.getFileByURI(SAVED_TEMPLATE_URI);
        return IO.importFeedbackSingle(file);
    }

    public static Feedback importDefaultFeedbackTemplate () {
        File file = IO.getFileByURI(DEFAULT_TEMPLATE_URI);
        return IO.importFeedbackSingle(file);
    }
    //endregion

    //region Miscellaneous
    // ================================================================================= //
    // Miscellaneous
    // ================================================================================= //
    /**
     * Returns the computer name. Used for runonce.
     *
     * @return The computer name, or {@code null} if an exception occurred.
     */
    public static String getComputerName () {
        String computerName = null;
        try {
            computerName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return computerName;
    }

    /**
     * Show a confirmation dialog before deleting items.
     *
     * @param numberOfItems The number of items being deleted.
     * @return {@code true} if user wants to delete items, {@code false} otherwise.
     */
    public static boolean confirmDelete (int numberOfItems) {
        String contentText = "Really delete all selected elements? There are currently " +
                numberOfItems + " items selected.";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, contentText, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("Really delete all selected items?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    //endregion
}
