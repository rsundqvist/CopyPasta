package gui;

import gui.feedback.FeedbackViewController;
import gui.pasta.PastaEditor;
import gui.pasta.PastaViewController;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.util.Duration;
import model.Feedback;
import model.IO;
import model.Pasta;
import model.UniqueArrayList;
import zip.GroupImporter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

public class Controller implements PastaViewController.PastaControllerListener {

    @FXML
    private PastaViewController pastaViewController = null;
    @FXML
    private FeedbackViewController feedbackViewController = null;
    @FXML
    private Label savedLabel, lastSaveTimestampLabel, versionLabel;

    private Timeline autosaveTimeline = null;

    public void exit () {
        System.exit(0);
    }

    public void initialize () {
        pastaViewController.initialize(this);
        savedLabel.setOpacity(0);
        initTimeline(false);
    }

    public void initTimeline (boolean saveNow) {
        if (saveNow)
            save();

        autosaveTimeline = new Timeline(new KeyFrame(
                Duration.minutes(5),
                ae -> save()));
        autosaveTimeline.setCycleCount(Timeline.INDEFINITE);
        autosaveTimeline.play();
    }

    public void parseFIREFolder () {
        File file = IO.showDirectoryChooser(null);
        if (file == null) return;

        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        for (int i = 0; i < directories.length; i++)
            directories[i] = directories[i].replaceAll("[^0-9]", "");

        List<String> fireGroups = Arrays.asList(directories);
        feedbackViewController.createFeedbackItems(fireGroups);
    }

    @Override
    public void select (Pasta pasta) {
    }

    public void selectFeedback () {
        feedbackViewController.selectView(0);
    }

    public void selectSetup () {
        feedbackViewController.selectView(1);
    }

    public void selectProgress () {
        feedbackViewController.selectView(2);
    }

    public void toggleAutoSave (Event event) {
        CheckMenuItem checkMenuItem = (CheckMenuItem) event.getSource();

        if (checkMenuItem.isSelected())
            initTimeline(true);
        else if (autosaveTimeline != null)
            autosaveTimeline.stop();

    }

    public void save () {
        pastaViewController.save();
        feedbackViewController.save();
        Tools.flashNode(savedLabel);

        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        lastSaveTimestampLabel.setText("Saved at " + dateFormat.format(Calendar.getInstance().getTime()));
        FadeTransition ft = new FadeTransition(Duration.seconds(6), lastSaveTimestampLabel);
        ft.setFromValue(0);
        ft.setToValue(1.0);
        ft.play();
    }

    @Override
    public void quickInsert (Pasta pasta) {
        feedbackViewController.quickInsert(pasta);
    }

    public String getCurrentAssignment () {
        return feedbackViewController.getAssignment();
    }

    public void settings () {
        new SettingsEditor().showAndWait();
    }

    public void shutdown () {
        pastaViewController.save();
        feedbackViewController.save();
        Settings.storeStoreSettingsFile();
        Settings.setRunningFile(false);
    }

    public void openPastaEditor () {
        UniqueArrayList<Pasta> pastaList = pastaViewController.getPastaList();

        List<Pasta> copy = Pasta.copy(pastaList);
        PastaEditor pastaEditor = new PastaEditor(copy, feedbackViewController.getAssignment());
        UniqueArrayList<Pasta> editorPastaList = pastaEditor.showAndWait();

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

    public void openGroupImporter () {
        GroupImporter groupImporter = new GroupImporter();
        List<Feedback> feedbackList = groupImporter.showAndWait();

        if (!feedbackList.isEmpty()) {
            ButtonType bt1 = new ButtonType("Nothing");
            ButtonType bt2 = new ButtonType("Replace ALL groups");
            ButtonType bt3 = new ButtonType("Import new groups");

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "What do you want to do?",
                    bt1, bt2, bt3);
            alert.setHeaderText("Finish Import");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent())
                if (result.get() == bt2)
                    feedbackViewController.importFeedbackAddTemplateContent(feedbackList, true); //Replace all
                else if (result.get() == bt3)
                    feedbackViewController.importFeedbackAddTemplateContent(feedbackList, false); // Add new only
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
        about_fxml();
        /*
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
                "Author:       Richard Sundqvist\n" +
                "E-mail:        richard.sundqvist@live.se\n" +
                "Git repo:     https://github.com/whisp91/CopyPasta\n" +
                "Version:      " + Tools.VERSION + "\n" +
                "";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Program");
        alert.setHeaderText("About Copy Pasta");
        alert.setContentText(content);
        alert.showAndWait();
        */
    }

    public void toggleFeedbackDone () {
        feedbackViewController.toggleDoneTab();
    }

    public void about_fxml () {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Program");
        alert.setHeaderText("About Copy Pasta");

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/about.fxml"));
        fxmlLoader.setController(this);
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
            return;
        }
        versionLabel.setText(Tools.VERSION);
        alert.getDialogPane().setExpandableContent(root);
        alert.getDialogPane().setExpanded(true);
        alert.initModality(Modality.NONE);
        alert.showAndWait();
    }

    public void onMail () {
        Desktop desktop;
        if (Desktop.isDesktopSupported()
                && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
            try {
                desktop.mail(new URI("mailto:richard.sundqvist@live.se?subject=About%20CopyPasta"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onRepo () {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/whisp91/CopyPasta").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUpdate () {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Check the Git repository for the latest version.",
                ButtonType.OK);
        alert.setHeaderText("Not Implemented");
        alert.showAndWait();
    }
}
