package model;

import com.google.gson.Gson;
import gui.Tools;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class IO {
    private static final Gson gson = new Gson();

    private IO () {
    }

    /**
     * Shows a file choice dialog and exports a list of Pasta.
     *
     * @param pastaList The list of Pasta to export.
     */
    public static void exportPastaJSON (List<Pasta> pastaList) {
        if (pastaList == null || pastaList.isEmpty()) return;

        File file = showJSSONSaveDialog("pasta");
        exportPastaJSON(pastaList, file);
    }

    /**
     * Export a list of Pasta to a file.
     *
     * @param pastaList The list of Pasta to export.
     * @param file The file to export to.
     */
    public static void exportPastaJSON (List<Pasta> pastaList, File file) {
        if (file == null || pastaList == null || pastaList.isEmpty()) return;

        ArrayList<Object> tempList = new ArrayList<>();
        tempList.addAll(pastaList);
        String json = gson.toJson(tempList);
        printStringToFile(json, file);
    }

    /**
     * Shows a file choice dialog and exports a single Pasta item, wrapped in a list.
     *
     * @param pasta The pasta item to export.
     */
    public static void exportPastaJSON (Pasta pasta) {
        if (pasta == null) return;

        ArrayList<Pasta> tempList = new ArrayList<>();
        tempList.add(pasta);
        exportPastaJSON(tempList);
    }

    /**
     * Shows a file choice dialog and imports all Pasta items from file.
     *
     * @return A list of Pasta, or {@code null} if import failed.
     */
    public static List<Pasta> importPasta () {
        File file = showJSONOpenDialog();
        return importPasta(file);
    }

    /**
     * Import a list of Pasta from a file.
     *
     * @param file The source file.
     * @return A list of Pasta, or {@code null} if import failed.
     */
    public static List<Pasta> importPasta (File file) {
        if (file != null) {
            try {
                Pasta pastaArray[] = gson.fromJson(new FileReader(file), Pasta[].class);
                return Arrays.asList(pastaArray);
            } catch (FileNotFoundException e) {
                showExceptionAlert(e);
            }
        }

        return null;
    }

    /**
     * Import a single Feedback item from file. Will fail if the item is wrapped in a list.
     *
     * @param file The file to import from.
     * @return A single Feedback item, or {@code null} if import failed.
     */
    public static Feedback importFeedbackSingle (File file) {
        if (file != null) {
            try {
                Feedback feedback = gson.fromJson(new FileReader(file), Feedback.class);
                return feedback;
            } catch (FileNotFoundException e) {
                showExceptionAlert(e);
            }
        }

        return null;
    }

    /**
     * Shows a file choice dialog and exports all Feedback items to a single .json-file
     *
     * @param feedbackList The list of Feedback to export.
     */
    public static void exportFeedbackAsJson (List<Feedback> feedbackList) {
        File file = showJSSONSaveDialog("feedback");
        exportFeedbackAsJson(feedbackList, file);
    }

    /**
     * Export a list of Feedback to the given file.
     *
     * @param feedbackList The list of Feedback to export.
     * @param file The file to export to.
     */
    public static void exportFeedbackAsJson (List<Feedback> feedbackList, File file) {
        if (file == null) return;

        ArrayList<Feedback> tempList = new ArrayList<>();
        tempList.addAll(feedbackList);
        String json = gson.toJson(tempList);
        printStringToFile(json, file);
    }

    /**
     * Shows a file choice dialog and exports a single Feedback item,  <b>without wrapping in a list.</b>
     *
     * @param template The Feedback to export.
     */
    public static void exportSingleFeedbackAsJson (Feedback template) {
        File file = showJSSONSaveDialog("template");
        exportSingleFeedbackAsJson(template, file);
    }

    /**
     * Exports a single Feedback item,  <b>without wrapping in a list.</b>
     *
     * @param template The Feedback to export.
     * @param file The file to export to.
     */
    public static void exportSingleFeedbackAsJson (Feedback template, File file) {
        if (file == null) return;

        String json = gson.toJson(template);
        printStringToFile(json, file);
    }

    /**
     * Export a List of feedback as individual .txt files, and as a single .json for later import.
     *
     * @param feedbackList A list of Feedback to export as individual .txt and a single .json
     */
    public static void exportFeedbackAsTxtAndJson (List<Feedback> feedbackList) {
        //Should strip out any weird stuff from derived list types/concurrency issues.
        ArrayList<Feedback> tempList = new ArrayList<>();
        tempList.addAll(feedbackList);

        File file = showDirectoryChooser();
        if (file == null) return;
        String json = gson.toJson(tempList);

        try {
            File jsonFile;
            jsonFile = new File(new URI(file.toURI().toString() + "/test.json"));
            printStringToFile(json, jsonFile);

            exportFeedbackAsTxt(tempList, file);
        } catch (Exception e) {
            showExceptionAlert(e);
        }
    }

    /**
     * Print a collection of feedback to a given directory. The feedback group numbers will be used as name. A .txt
     * extension will be given automatically.
     *
     * @param c The collection of feedback.
     * @param directory The target directory.
     */
    public static void exportFeedbackAsTxt (Collection<Feedback> c, File directory) {
        for (Feedback feedback : c) {
            String filename = feedback.getGroup() + ".txt";
            File txtFile;
            try {
                txtFile = getFileInDirectory(directory, filename);
            } catch (URISyntaxException e) {
                showExceptionAlert(e);
                e.printStackTrace();
                return;
            }
            printStringToFile(feedback.getStylizedContent(), txtFile);
        }
    }

    /**
     * Show directory chooser and print a collection of feedback. The feedback group numbers will be used as name. A .txt
     * extension will be given automatically.
     *
     * @param c The collection of feedback.
     */
    public static void exportFeedbackAsTxt (Collection<Feedback> c) {
        File directory = showDirectoryChooser();
        if (directory == null) return;

        for (Feedback feedback : c) {
            String filename = feedback.getGroup() + ".txt";
            File txtFile;
            try {
                txtFile = getFileInDirectory(directory, filename);
            } catch (URISyntaxException e) {
                showExceptionAlert(e);
                e.printStackTrace();
                return;
            }
            printStringToFile(feedback.getStylizedContent(), txtFile);
        }
    }

    private static File getFileInDirectory (File directory, String filename) throws URISyntaxException {
        File txtFile;
        txtFile = new File(new URI((directory.toURI().toString() + "/" + filename)));
        return txtFile;
    }

    private static void ensureAccess (File file) throws IOException {
        if (!file.canWrite())
            file.setWritable(true);
        if (!file.exists())
            file.createNewFile();

    }

    /**
     * Shows a directory chooser dialog.
     *
     * @return The selected file, or {@code null} if the user cancelled.
     */
    public static File showDirectoryChooser () {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        return directoryChooser.showDialog(null);
    }

    /**
     * Open a save dialog for a single file.
     *
     * @param initialFileName Initial file name.
     * @param fileExtension File extension.
     * @return The user selected file, or {@code null} if user cancelled.
     */
    public static File showSaveDialog (String initialFileName, String fileExtension) {
        File file = null;
        switch (fileExtension) {
            case "json":
                file = showJSSONSaveDialog(initialFileName);
                break;
            case "txt":
                file = showTXTSaveDialog(initialFileName);
                break;
            default:
                throw new IllegalArgumentException("Unknown file extension: " + fileExtension);
        }
        return file;
    }

    /**
     * Shows a file chooser dialog for .json-files.
     *
     * @param initialFileName The initial file name.
     * @return The selected file, or {@code null} if the user cancelled.
     */
    public static File showJSSONSaveDialog (String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(initialFileName);
        FileChooser.ExtensionFilter extFilter;
        extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        extFilter = new FileChooser.ExtensionFilter("All (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showSaveDialog(null);
    }

    /**
     * Shows a file chooser dialog for .txt-files.
     *
     * @param initialFileName The initial file name.
     * @return The selected file, or {@code null} if the user cancelled.
     */
    public static File showTXTSaveDialog (String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(initialFileName);
        FileChooser.ExtensionFilter extFilter;
        extFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        extFilter = new FileChooser.ExtensionFilter("All (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showSaveDialog(null);
    }

    /**
     * Shows a file chooser dialog for .json-files.
     *
     * @return The selected file, or {@code null} if the user cancelled.
     */
    public static File showJSONOpenDialog () {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter;
        extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        extFilter = new FileChooser.ExtensionFilter("All (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showOpenDialog(null);
    }

    /**
     * Print a String to file.
     *
     * @param content The content to print.
     * @param file The file to print to.
     */
    public static void printStringToFile (String content, File file) {
        if (file == null || content == null) return;

        try {
            ensureAccess(file);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            showExceptionAlert(ex);
        }
    }

    /**
     * Opens a file chooser dialog and returns a list of Feedback. Will return {@code null} if the import failed.
     *
     * @return A list of Feedback, or {@code null} if nothing was imported.
     */
    public static List<Feedback> importFeedback () {
        File file = showJSONOpenDialog();
        return importFeedback(file);
    }

    /**
     * Returns a list of Feedback. Will return {@code null} if the import failed.
     *
     * @param file The file to import from.
     * @return A list of Feedback.
     */
    public static List<Feedback> importFeedback (File file) {
        if (file != null) {
            try {
                Feedback feedbackArray[] = gson.fromJson(new FileReader(file), Feedback[].class);
                return Arrays.asList(feedbackArray);
            } catch (FileNotFoundException e) {
                showExceptionAlert(e);
            }
        }
        return null;
    }

    /**
     * Attempts to fetch a file by URI. Returns {@code null} if it fails for any reason.
     *
     * @param uriString The URI of the desired file.
     * @return A file, or {@code null} if unsuccessful.
     */
    public static File getFileByURI (String uriString) {
        URI uri;
        try {
            URL url = Tools.class.getResource(uriString);
            if (url != null)
                uri = url.toURI();
            else {
                System.err.println("Failed to fetch resource with URI: \"" + uriString + "\"");
                return null;
            }
        } catch (URISyntaxException e) {
            showExceptionAlert(e);
            return null;
        }
        return new File(uri);
    }

    /**
     * http://code.makery.ch/blog/javafx-dialogs-official/
     *
     * @param ex The exception to show.
     */
    public static void showExceptionAlert (Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Operation failure.");
        alert.setContentText("ERROR: " + ex.toString());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Exception stack trace:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
}
