package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gui.Tools;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class IO {
    public static final String ENCODING = "UTF-8";
    private static final Gson gson = build();

    private IO () {
    }

    private static Gson build () {
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }

    /**
     * Shows a file choice dialog and exports a list of Pasta.
     *
     * @param pastaList The list of Pasta to export.
     */
    public static void exportPastaJSON (File dir, List<Pasta> pastaList) {
        if (pastaList == null || pastaList.isEmpty()) return;

        File file = showJSSONSaveDialog(dir, "pasta");
        exportPastaJSON(pastaList, file);
    }

    /**
     * Export a list of Pasta to a file.
     *
     * @param pastaList The list of Pasta to export.
     * @param file The file to export to.
     */
    public static void exportPastaJSON (List<Pasta> pastaList, File file) {
        if (file == null || pastaList == null) return;

        ArrayList<Object> tempList = new ArrayList<>();
        tempList.addAll(pastaList);
        String json = gson.toJson(tempList);
        printStringToFile(json, file);
    }

    /**
     * Shows a file choice dialog and exports a single Pasta item, wrapped in a list.
     *
     * @param dir The initial directory.
     * @param pasta The pasta item to export.
     */
    public static void exportPastaJSON (File dir, Pasta pasta) {
        if (pasta == null) return;

        ArrayList<Pasta> tempList = new ArrayList<>();
        tempList.add(pasta);
        exportPastaJSON(dir, tempList);
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
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(file),
                        Charset.forName(ENCODING).newDecoder()
                );
                Pasta pastaArray[] = gson.fromJson(reader, Pasta[].class);
                return pastaArray == null ? null : Arrays.asList(pastaArray);
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
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(file),
                        Charset.forName(ENCODING).newDecoder()
                );
                Feedback feedback = gson.fromJson(reader, Feedback.class);
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
     * @param dir The initial directory.
     * @param feedbackList The list of Feedback to export.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public static boolean exportFeedbackAsJson (File dir, List<Feedback> feedbackList) {
        File file = showJSSONSaveDialog(dir, "feedback");
        return exportFeedbackAsJson(feedbackList, file);
    }

    /**
     * Export a list of Feedback to the given file.
     *
     * @param feedbackList The list of Feedback to export.
     * @param file The file to export to.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public static boolean exportFeedbackAsJson (List<Feedback> feedbackList, File file) {
        if (file == null) return false;

        ArrayList<Feedback> tempList = new ArrayList<>();
        tempList.addAll(feedbackList);
        String json = gson.toJson(tempList);
        return printStringToFile(json, file);
    }

    /**
     * Shows a file choice dialog and exports a single Feedback item,  <b>without wrapping in a list.</b>
     *
     * @param dir The initial directory.
     * @param template The Feedback to export.
     */
    public static void exportSingleFeedbackAsJson (File dir, Feedback template) {
        File file = showJSSONSaveDialog(dir, "template");
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
     * @param dir Initial directory.
     * @param feedbackList A list of Feedback to export as individual .txt and a single .json
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public static boolean exportFeedbackAsTxtAndJson (File dir, List<Feedback> feedbackList) {
        //Should strip out any weird stuff from derived list types/concurrency issues.
        ArrayList<Feedback> tempList = new ArrayList<>();
        tempList.addAll(feedbackList);

        File file = showDirectoryChooser(dir);
        if (file == null) return false;
        String json = gson.toJson(tempList);

        try {
            File jsonFile;
            jsonFile = new File(new URI(file.toURI().toString() + "/feedback.json"));
            boolean exportSuccessful = printStringToFile(json, jsonFile);

            exportSuccessful = exportFeedbackAsTxt(tempList, file) && exportSuccessful;

            return exportSuccessful;
        } catch (Exception e) {
            showExceptionAlert(e);
            return false;
        }
    }

    /**
     * Print a collection of feedback to a given directory. The feedback group numbers will be used as name. A .txt
     * extension will be given automatically.
     *
     * @param c The collection of feedback.
     * @param directory The target directory.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public static boolean exportFeedbackAsTxt (Collection<Feedback> c, File directory) {
        boolean exportSuccessful = true;
        for (Feedback feedback : c) {
            String filename = feedback.getGroup() + ".txt";
            File txtFile;
            try {
                txtFile = getFileInDirectory(directory, filename);
            } catch (URISyntaxException e) {
                showExceptionAlert(e);
                e.printStackTrace();
                return false;
            }
            exportSuccessful = exportSuccessful && printStringToFile(feedback.getStylizedContent(), txtFile);
        }

        return exportSuccessful;
    }

    /**
     * Show directory chooser and print a collection of feedback. The feedback group numbers will be used as name. A .txt
     * extension will be given automatically.
     *
     * @param dir The initial directory.
     * @param c The collection of feedback.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public static boolean exportFeedbackAsTxt (File dir, Collection<Feedback> c) {
        File directory = showDirectoryChooser(dir);
        if (directory == null) return false;

        boolean exportSuccessful = true;
        for (Feedback feedback : c) {
            String filename = feedback.getGroup() + ".txt";
            File txtFile;
            try {
                txtFile = getFileInDirectory(directory, filename);
            } catch (URISyntaxException e) {
                showExceptionAlert(e);
                e.printStackTrace();
                return false;
            }
            exportSuccessful = exportSuccessful && printStringToFile(feedback.getStylizedContent(), txtFile);
        }

        return exportSuccessful;
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
     * @param dir Initial directory.
     * @return The selected file, or {@code null} if the user cancelled.
     */
    public static File showDirectoryChooser (File dir) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(dir);
        return directoryChooser.showDialog(null);
    }

    /**
     * Open a save dialog for a single file.
     *
     * @param initialFileName Initial file name.
     * @param fileExtension File extension.
     * @return The user selected file, or {@code null} if user cancelled.
     */
    public static File showSaveDialog (File dir, String initialFileName, String fileExtension) {
        File file = null;
        switch (fileExtension) {
            case "json":
                file = showJSSONSaveDialog(dir, initialFileName);
                break;
            case "txt":
                file = showTXTSaveDialog(dir, initialFileName);
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
    public static File showJSSONSaveDialog (File dir, String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(dir);
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
    public static File showTXTSaveDialog (File dir, String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(dir);
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
     * Write a String to file.
     *
     * @param content The content to print.
     * @param file The file to print to.
     * @return {@code true} if export was successful, {@code false} otherwise.
     */
    public static boolean printStringToFile (String content, File file) {
        if (file == null || content == null) return false;

        try {
            ensureAccess(file);
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file),
                    Charset.forName(ENCODING).newEncoder()
            );
            writer.write(content);
            writer.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            showExceptionAlert(ex);
            return false;
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
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(file),
                        Charset.forName(ENCODING).newDecoder()
                );
                Feedback feedbackArray[] = gson.fromJson(reader, Feedback[].class);
                return feedbackArray == null ? null : Arrays.asList(feedbackArray);
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
            e.printStackTrace();
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
        ex.printStackTrace();

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

    /**
     * Returns the content of a file as a string.
     *
     * @param file The file to read from.
     * @return The content of the file, or {@code null} if the extraction failed.
     */
    public static String extractContent (File file) {
        String content = null;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line + "\n");
            bufferedReader.close();
            content = stringBuilder.toString();
        } catch (Exception e) {
            showExceptionAlert(e);
            e.printStackTrace();
        }

        return content;
    }

    /**
     * Stolen from NeilMonday @ https://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
     *
     * @param zipFile The file to extract
     * @throws IOException If an IO exception occurrs.
     */
    public static void extractFolder (String zipFile) throws IOException {
        System.out.println(zipFile);
        int BUFFER = 2048;
        File file = new File(zipFile);

        ZipFile zip = new ZipFile(file);
        String newPath = zipFile.substring(0, zipFile.length() - 4);

        new File(newPath).mkdir();
        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            //destFile = new File(newPath, destFile.getName());
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                    dest.write(data, 0, currentByte);

                dest.flush();
                dest.close();
                is.close();
            }

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open
                extractFolder(destFile.getAbsolutePath());
            }
        }
    }

    /**
     * Recursively clear all contents of a directory.
     *
     * @param root The directory to clear.
     * @param deleteRoot If {@code true}, delete root folder as well.
     */
    public static void clearDirectory (File root, boolean deleteRoot) throws IOException {
        clearDirectoryWork(root);

        if (deleteRoot)
            root.delete();
    }

    private static void clearDirectoryWork (File dir) throws IOException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                clearDirectoryWork(file);

            file.delete();
        }
    }
}
