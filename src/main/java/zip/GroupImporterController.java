package zip;

import gui.feedback.JavaCodeArea;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import model.Feedback;
import model.FeedbackManager;
import model.IO;

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
    private final FeedbackManager feedbackManager = new FeedbackManager();

    public GroupImporterController () {
        codeArea = new JavaCodeArea();
        codeArea.setEditable(false);
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
        if (fileName == null) return;

        String content = feedback.getFiles().get(fileName);
        codeArea.setText(content);
        filePreviewLabel.setText(fileName + " (group " + group + ")");
    }

    public void initialize () {
        previewContainer.setCenter(codeArea);
        groupListView.getSelectionModel().selectedIndexProperty().addListener(event -> onGroupSelectionChanged());
        filesListView.getSelectionModel().selectedIndexProperty().addListener(event -> onFileSelectionChanged());

        groupListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        filesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void onChangeRootDirectory () {
        File dir = IO.showDirectoryChooser(null);

        if (dir != null) {
            try {
                groupListView.getItems().clear();
                filesListView.getItems().clear();
                updateFileEndingList();

                rootDirectoryField.setText(dir.getCanonicalPath());
                FeedbackTreeItem root = crawl(dir, 0, null);
                crawlNodeStatus(root, 0, null);
                treeView.setRoot(root);

                treeView.setContextMenu(createTreeViewContextMenu());

            } catch (Exception e) {
                IO.showExceptionAlert(e);
                e.printStackTrace();
            }
        } else {
            rootDirectoryField.setText(null);
        }
    }

    private ContextMenu createTreeViewContextMenu () {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem add = new MenuItem("Add");
        add.setOnAction(event -> addItem(false));
        MenuItem addTo = new MenuItem("Add to group"); //TODO: Allow user to pick group.
        addTo.setOnAction(event -> addItem(true));
        MenuItem remove = new MenuItem("Remove");
        remove.setOnAction(event -> removeItem());

        contextMenu.getItems().addAll(add, new SeparatorMenuItem(), remove);

        return contextMenu;
    }

    private void removeItem () {
        FeedbackTreeItem selectedItem = (FeedbackTreeItem) treeView.getSelectionModel().getSelectedItem();
        Feedback feedback = selectedItem.getFeedback();
        feedback.removeFile(selectedItem.getValue().getName());

        update();
    }

    private void addItem (boolean pickGroup) {
        FeedbackTreeItem selectedItem = (FeedbackTreeItem) treeView.getSelectionModel().getSelectedItem();
        Feedback feedback = selectedItem.getFeedback();

        if (feedback == null) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Associated Feedback");
            alert.setContentText("Items must be located in a child folder to add. I might fix this in the future. Maybe.");
            alert.show();

            //TODO: Handle adding of files in root folder.
            return;
        }

        feedback.addFile(selectedItem.getValue().getName(), null);
        String content = IO.extractContent(selectedItem.getValue());
        feedback.addFile(selectedItem.getValue().getName(), content);
        update();
    }

    private void update () {
        crawlNodeStatus((FeedbackTreeItem) treeView.getRoot(), 0, null);
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

    private FeedbackTreeItem crawl (File file, int level, Feedback feedback) throws Exception {
        if (level == 1 && file.isDirectory()) {
            String group = file.getName().replaceAll("[^0-9]", "");
            feedback = new Feedback();
            feedback.setGroup(group);
            groupListView.getItems().add(feedback.getGroup());
            feedbackManager.importFeedback(feedback);
        }
        FeedbackTreeItem root = new FeedbackTreeItem(file, feedback);

        for (File dirFile : file.listFiles()) {
            if (dirFile.isDirectory()) {
                FeedbackTreeItem childDir = crawl(dirFile, level + 1, feedback);
                root.getChildren().add(childDir);

            } else {
                FeedbackTreeItem child = new FeedbackTreeItem(dirFile, feedback);
                root.getChildren().add(child);

                int len = dirFile.getName().length();
                String[] s = dirFile.getName().split("\\.");
                if (feedback != null && s.length > 1 && fileEndingList.contains(s[s.length - 1])) {
                    String content = IO.extractContent(dirFile);
                    feedback.addFile(dirFile.getName(), content);
                }
            }
        }
        return root;
    }

    private NodeStatus crawlNodeStatus (FeedbackTreeItem root, int level, Feedback feedback) {
        NodeStatus nodeStatus;
        if (level == 1) // level 1 => group root
            feedback = root.getFeedback();

        if (root.isLeaf()) { // leaf => file or empty folder
            if (feedback != null && feedback.getFiles().keySet().contains(root.getValue().getName()))
                nodeStatus = NodeStatus.GREEN;
            else
                nodeStatus = NodeStatus.RED;
        } else {

            List<TreeItem<File>> children = root.getChildren();
            NodeStatus[] childNodeStatus = new NodeStatus[children.size()];


            for (int i = 0; i < children.size(); i++) {
                FeedbackTreeItem treeItem = (FeedbackTreeItem) children.get(i);
                childNodeStatus[i] = crawlNodeStatus(treeItem, level + 1, feedback);
            }
            nodeStatus = NodeStatus.getCombinedStatus(childNodeStatus);
            setNodeIcon(root, nodeStatus.getImageView());
        }
        setNodeIcon(root, nodeStatus.getImageView());
        if (nodeStatus != NodeStatus.RED)
            root.setExpanded(true);
        return nodeStatus;
    }

    private void setNodeIcon (TreeItem node, ImageView iw) {
        iw.setFitWidth(15);
        iw.setFitHeight(15);
        iw.setOpacity(0.6);
        node.setGraphic(iw);
    }

    public void onToggleOpenArchives (Event event) {
        openArchives = ((ToggleButton) event.getSource()).isSelected();
    }

    public List<Feedback> getFeedback () {
        return feedbackManager.getFeedbackList();
    }


    private enum NodeStatus {
        RED, GREEN, BLUE;

        public static NodeStatus getCombinedStatus (NodeStatus... status) {
            NodeStatus status0 = status[0];

            if (status0 == RED || status0 == GREEN)
                for (int i = 1; i < status.length; i++)
                    if (status[i] != status0) return BLUE;

            return status0;
        }

        public ImageView getImageView () {
            String s = null;

            switch (this) {
                case RED:
                    s = "/red_circle.png";
                    break;
                case GREEN:
                    s = "/green_circle.png";
                    break;
                case BLUE:
                    s = "/bblue_circle.png";
                    break;
            }
            return new ImageView(new Image(getClass().getResourceAsStream(s)));
        }
    }

    public static class FeedbackTreeItem extends TreeItem<File> {
        private final Feedback feedback;

        public FeedbackTreeItem (File file, Feedback feedback) {
            super(new FileWithGetNameAsToString(file.getPath()));
            this.feedback = feedback;
        }

        public Feedback getFeedback () {
            return feedback;
        }

        public static class FileWithGetNameAsToString extends File {
            public FileWithGetNameAsToString (String pathname) {
                super(pathname);
            }

            public String toString () {
                return super.getName();
            }
        }
    }
}
