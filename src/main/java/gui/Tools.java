package gui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import model.Feedback;
import model.IO;
import model.Pasta;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Richard Sundqvist on 19/02/2017.
 */
public abstract class Tools {

    //public static final File IMPORT_TEMP_FOLDER = create("import-temp", null);
    public static final File SAVE_FOLDER = create("save", null);
    public static final File AUTO_SAVE_PASTA_FILE = create("save/auto", "pasta.json");
    public static final File AUTO_SAVE_TEMPLATE_FILE = create("save/auto", "template.json");
    public static final File AUTO_SAVE_FEEDBACK_FILE = create("save/auto", "feedback.json");
    public static final File GROUP_IMPORT_FILE_PATTERNS = create("save/auto", "group_file_patterns.txt");

    public static final String VERSION = "PRERELEASE Rev6";

    private Tools () {
    }

    private static File create (String dir, String file) {
        File userDir = new File(System.getProperty("user.dir"));
        File d = new File(userDir, dir);
        if (!d.exists())
            d.mkdirs();

        if (file != null) {
            File f = new File(d, file);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    IO.showExceptionAlert(e);
                    e.printStackTrace();
                }
            }
            return f;
        } else {
            return d;
        }
    }
    //Files and directories


    //region Pasta
    // ================================================================================= //
    // Pasta
    // ================================================================================= //
    public static void exportSavedPasta (List<Pasta> pastaList) {
        IO.exportPastaJSON(pastaList, AUTO_SAVE_PASTA_FILE);
    }

    public static List<Pasta> importSavedPasta () {
        return IO.importPasta(AUTO_SAVE_PASTA_FILE);
    }
    //endregion


    //region Feedback
    // ================================================================================= //
    // Feedback
    // ================================================================================= //
    public static void exportSavedFeedback (List<Feedback> feedbackList) {
        IO.exportFeedbackAsJson(feedbackList, AUTO_SAVE_FEEDBACK_FILE);
    }

    public static List<Feedback> importSavedFeedback () {
        return IO.importFeedback(AUTO_SAVE_FEEDBACK_FILE);
    }

    public static void exportSavedTemplate (Feedback template) {
        IO.exportSingleFeedbackAsJson(template, AUTO_SAVE_TEMPLATE_FILE);
    }

    public static Feedback importSavedTemplate () {
        return IO.importFeedbackSingle(AUTO_SAVE_TEMPLATE_FILE);
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

    public static void flashNode (Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(2000), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(2000), node);
        st.setFromX(1.5);
        st.setToX(0.5);
        st.setFromY(1.5);
        st.setToY(0.5);

        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.play();
    }
    //endregion
}
