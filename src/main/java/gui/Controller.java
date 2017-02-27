package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.IO;
import model.Pasta;
import model.UniqueArrayList;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Controller implements PastaViewController.PastaControllerListener {

    @FXML
    private PastaViewController pastaViewController = null;
    @FXML
    private FeedbackViewController feedbackViewController = null;

    public void exit () {
        System.exit(0);
    }

    public void initialize () {
        pastaViewController.initialize(this);
    }

    public void parseFIREFolder () {
        File file = IO.showDirectoryChooser(null);
        if (file == null) return;

        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        for (int i = 0; i < directories.length; i++)
            directories[i] = directories[i].replaceAll("[^0-9.]", "");

        List<String> fireGroups = Arrays.asList(directories);
        feedbackViewController.createFeedbackItems(fireGroups);
    }

    @Override
    public void select (Pasta pasta) {
        //TODO - do something
    }

    @Override
    public String getAssignment () {
        return feedbackViewController.getAssignment();
    }

    public void shutdown () {
        pastaViewController.shutdown();
        feedbackViewController.shutdown();
    }

    public void openPastaEditor () {
        UniqueArrayList<Pasta> pastaList = pastaViewController.getPastaList();

        List<Pasta> copy = Pasta.copy(pastaList);
        PastaEditor pastaEditor = new PastaEditor(copy, feedbackViewController.getAssignment());
        UniqueArrayList<Pasta> editorPastaList = pastaEditor.showAndWait();

        System.out.println(editorPastaList.equals(pastaList));
        if (!editorPastaList.equals(pastaList)) {
            pastaList = editorPastaList;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Replace current Pasta with editor Pasta?",
                    ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Replace current pasta?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES)
                pastaViewController.setPastaList(pastaList);
        }
    }

    public void importPasta () {
        pastaViewController.importPasta();
    }

    public void exportPasta () {
        pastaViewController.exportAllPasta();
    }

    public void clearPasta () {
        pastaViewController.clearAllPasta();
    }

    public void exportFeedback () {
        feedbackViewController.exportAllFeedback();
    }

    public void importFeedback () {
        feedbackViewController.importFeedback();
    }

    public void exportTemplate () {
        feedbackViewController.exportTemplate();
    }

    public void importTemplate () {
        feedbackViewController.importTemplate();
    }

    public void clearFeedback () {
        feedbackViewController.clear();
    }

    public void about () {
        String content = "" +
                "Copy Pasta is a program developed to aid in grading lab exercises. Common feedback (\"Pasta\") " +
                "can be created and categorized to speed up the process. Wildcards and templates are used to reduce the" +
                " risk of mistakes, and to reduce clutter. The program will automatically load/store the most recent" +
                " data (feedback and Pasta) when starting/exiting. \n" +
                "\n" +
                "Typical workflow: \n" +
                "   1. Setup (JSON-files received from course owner) \n" +
                "       i. Import feedback template \n" +
                "       ii. Import Pasta \n" +
                "   2. Enter group numbers, then click \"Create Feedback\". \n" +
                "       e.g. \"3 5, 6  9, potato\" for groups {3, 5, 6, 9, potato}. \n" +
                "   3. Write feedback. \n" + "" +
                "       i. Save common feedback with the Pasta Editor (Ctrl+G). \n" +
                "       ii. Share common feedback (RMB -> Export) \n" +
                "   4. Export the feedback to .txt (Ctrl+E) \n" +
                "\n" +
                "Creator:      Richard Sundqvist\n" +
                "E-mail:        richard.sundqvist@live.se\n" +
                "Git repo:     https://github.com/whisp91/CopyPasta\n" +
                "Version:      " + Tools.VERSION + "\n" +
                "";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Program");
        alert.setHeaderText("About Copy Pasta");
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void toggleFeedbackDone () {
        feedbackViewController.toggleDoneTab();
    }

    public void about2 () {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Program");
        alert.setHeaderText("About Copy Pasta");

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/about.fxml"));
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
            return;
        }

        alert.getDialogPane().setExpandableContent(root);
        alert.showAndWait();
    }
}