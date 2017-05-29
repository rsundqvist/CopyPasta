package zip;

import gui.feedback.JavaCodeArea;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import model.Feedback;
import model.FeedbackManager;
import model.IO;
import net.sf.sevenzipjbinding.SevenZip;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richard Sundqvist on 17/04/2017.
 */
public class GroupImporterController {
    @FXML
    private TextField rootDirectoryField;
    @FXML
    private ListView groupListView, filesListView;
    @FXML
    private TreeView treeView;
    @FXML
    private BorderPane previewContainer;
    @FXML
    private TextArea filePatternsTextArea;

    @FXML
    private Label filePreviewLabel;

    private List<String> fileEndingList;
    private boolean openArchives = true;
    private final JavaCodeArea codeArea;
    private File currentFile;
    private final FeedbackManager feedbackManager = new FeedbackManager();

    public GroupImporterController () {
        codeArea = new JavaCodeArea();
        codeArea.setEditable(false);

        try {
            SevenZip.initSevenZipFromPlatformJAR();
            System.out.println("7-Zip-JBinding library was initialized");
        } catch (Exception e) {
            IO.showExceptionAlert(e);
            e.printStackTrace();
        }
    }

    private void onGroupSelectionChanged () {
        String group = (String) groupListView.getSelectionModel().getSelectedItem();
        Feedback feedback = feedbackManager.getByGroup(group);
        if (feedback != null)
            filesListView.getItems().setAll(feedback.getFiles().keySet());
    }

    private void onFileSelectionChanged () {
        String group = (String) groupListView.getSelectionModel().getSelectedItem();
        Feedback feedback = feedbackManager.getByGroup(group);
        if (feedback == null) return;

        String fileName = (String) filesListView.getSelectionModel().getSelectedItem();
        String content = feedback.getFiles().get(fileName);
        codeArea.setText(content);
        filePreviewLabel.setText(fileName + " (group " + group + ")");
    }

    public void initialize () {
        previewContainer.setCenter(codeArea);
        groupListView.getSelectionModel().selectedIndexProperty().addListener(event -> onGroupSelectionChanged());
        filesListView.getSelectionModel().selectedIndexProperty().addListener(event -> onFileSelectionChanged());
    }

    public void onChangeRootDirectory () {
        File dir = IO.showDirectoryChooser(null);

        if (dir != null) {
            try {
                groupListView.getItems().clear();
                filesListView.getItems().clear();
                updateFileEndingList();

                rootDirectoryField.setText(dir.getCanonicalPath());
                TreeItem<String> root = crawl(dir);
                treeView.setRoot(root);

            } catch (Exception e) {
                IO.showExceptionAlert(e);
                e.printStackTrace();
            }
        } else {
            rootDirectoryField.setText(null);
        }
    }

    private void updateFileEndingList () throws IOException {
        String text = filePatternsTextArea.getText();
        fileEndingList = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(text));

        String line;
        while ((line = reader.readLine()) != null) {
            fileEndingList.add(line.toLowerCase());
        }
    }

    private TreeItem<String> crawl (File dir) throws Exception {
        TreeItem<String> root = new TreeItem<>(dir.getName());
        root.setExpanded(true);

        String group = dir.getName().replaceAll("[^0-9]", "");
        Feedback feedback = new Feedback();
        feedback.setGroup(group);

        boolean hasContent = false;
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                TreeItem<String> child = crawl(file);
                if (child != null) {
                    hasContent = true;
                    root.getChildren().add(child);
                }

            } else {
                String name = file.getName();

                for (String fileEnding : fileEndingList) {
                    if (name.endsWith(fileEnding)) {
                        String content = IO.extractContent(file);
                        feedback.addFile(file.getName(), content);

                        TreeItem<String> child = new TreeItem<>(name);
                        root.getChildren().add(child);
                        hasContent = true;
                        break;
                    }
                }

            }
        }
        if (hasContent) {
            groupListView.getItems().add(feedback.getGroup());
            feedbackManager.importFeedback(feedback);
            return root;
        } else {
            return null;
        }
    }

    public void onToggleOpenArchives (Event event) {
        openArchives = ((ToggleButton) event.getSource()).isSelected();
    }

    public List<Feedback> getFeedback () {
        return feedbackManager.getFeedbackList();
    }


}
