package zip;

import gui.JavaCodeArea;
import gui.Tools;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Feedback;
import model.FeedbackManager;
import model.IO;
import model.ManagerListener;
import org.fxmisc.richtext.ViewActions;

import java.io.File;
import java.util.List;

/** Created by Richard Sundqvist on 17/04/2017. */
public class GroupImporterController {
  private final JavaCodeArea codeArea;
  private FeedbackManager tmpManager = new FeedbackManager(), realManager;
  @FXML private TextField rootDirectoryField, currentGroupsTextField;
  @FXML private ListView groupListView, filesListView;
  @FXML private TreeView treeView;
  @FXML private BorderPane previewContainer;
  @FXML private TextArea filePatternsTextArea;
  @FXML private Label filePreviewLabel;
  @FXML private Button replaceAllButton, importButton;
  @FXML private CheckBox erpaCheckBox; // Enable "Replace All" checkbox
  @FXML private VBox hintVBox;
  @FXML private GridPane rootGrid;
  private List<String> fileEndingList;
  private boolean openArchives = false;
  private ManagerListener listener;

  public GroupImporterController() {
    codeArea = new JavaCodeArea();
    codeArea.setEditable(false);
    codeArea.setShowCaret(ViewActions.CaretVisibility.AUTO);
  }

  public static void removeItem(FeedbackTreeItem treeItem, Feedback feedback) {
    File file = treeItem.getValue();

    if (file.isDirectory())
      treeItem
          .getChildren()
          .forEach(childItem -> removeItem((FeedbackTreeItem) childItem, feedback));
    else feedback.removeFile(file.getName());
  }

  public static void addItem(FeedbackTreeItem treeItem, Feedback feedback) {
    File file = treeItem.getValue();

    if (file.isDirectory())
      treeItem.getChildren().forEach(childItem -> addItem((FeedbackTreeItem) childItem, feedback));
    else {
      String content = IO.getFileAsString(file);
      feedback.addFile(file.getName(), content);
    }
  }

  private static void setNodeIcon(TreeItem node, ImageView iw) {
    iw.setFitWidth(18);
    iw.setFitHeight(18);
    iw.setOpacity(0.7);
    node.setGraphic(iw);
  }

  public void onReplaceAll() {
    realManager.clear(); // Replace all content.
    realManager.importFeedback(tmpManager.getFeedbackList(), true);
    close(true);
  }

  public void onCancel() {
    close(false);
  }

  public void onImport() {
    tmpManager.removeFeedbackByGroup(realManager.getGroups()); // Don't overwrite existing content.
    realManager.importFeedback(tmpManager.getFeedbackList(), true);
    close(true);
  }

  public void onGroupSelectionChanged() {
    String group = (String) groupListView.getSelectionModel().getSelectedItem();
    Feedback feedback = tmpManager.getByGroup(group);
    if (feedback != null) filesListView.getItems().setAll(feedback.getFiles().keySet());
  }

  public void onFileSelectionChanged() {
    String group = (String) groupListView.getSelectionModel().getSelectedItem();
    Feedback feedback = tmpManager.getByGroup(group);
    if (feedback == null) return;

    String fileName = (String) filesListView.getSelectionModel().getSelectedItem();
    if (fileName == null) return;

    String content = feedback.getFiles().get(fileName);
    codeArea.setText(content);
    filePreviewLabel.setText(fileName + " - \"" + group + "\"");
  }

  public void initialize() {
    previewContainer.setCenter(codeArea);
    groupListView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(event -> onGroupSelectionChanged());
    filesListView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(event -> onFileSelectionChanged());

    groupListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    filesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    String s = IO.getFileAsString(Tools.GROUP_IMPORT_FILE_PATTERNS);
    if (s != null && s.length() > 0) {
      filePatternsTextArea.setText(s);
    }

    replaceAllButton.disableProperty().bind(erpaCheckBox.selectedProperty().not());
  }

  public void onChangeRootDirectory() {
    File dir = IO.showDirectoryChooser(null);

    if (dir != null) {
      hintVBox.setMouseTransparent(true);
      hintVBox.setVisible(false);
      rootGrid.getChildren().remove(hintVBox);
      importButton.setDefaultButton(true);

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

  public void saveFilePatterns() {
    updateFileEndingList();
    StringBuilder sb = new StringBuilder();
    for (String string : fileEndingList) sb.append(string + "\n");
    IO.printStringToFile(sb.toString(), Tools.GROUP_IMPORT_FILE_PATTERNS);
  }

  private ContextMenu createTreeViewContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem add = new MenuItem("Add");
    add.setOnAction(event -> addItem(false));
    // MenuItem addTo = new MenuItem("Add to group"); // TODO: Allow user to pick group.
    // addTo.setOnAction(event -> addItem(true));
    MenuItem remove = new MenuItem("Remove");
    remove.setOnAction(event -> removeItem());

    contextMenu.getItems().addAll(add, new SeparatorMenuItem(), remove);

    return contextMenu;
  }

  public void removeItem() {
    FeedbackTreeItem selectedItem =
        (FeedbackTreeItem) treeView.getSelectionModel().getSelectedItem();
    Feedback feedback = selectedItem.getFeedback();

    if (feedback != null) removeItem(selectedItem, feedback);

    update();
  }

  public void addItem(boolean pickGroup) {
    FeedbackTreeItem selectedItem =
        (FeedbackTreeItem) treeView.getSelectionModel().getSelectedItem();
    Feedback feedback = selectedItem.getFeedback();

    if (feedback == null) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("No Associated Feedback");
      alert.setHeaderText("No Associated Feedback");
      alert.setContentText(
          "Items must be located in a child folder to add. I might fix this in the future. Maybe.");

      alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
      alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
      alert.showAndWait();
      return;
    }

    addItem(selectedItem, feedback);
    update();
  }

  public void update() {
    crawlNodeStatus((FeedbackTreeItem) treeView.getRoot(), 0, null);
    onGroupSelectionChanged();
  }

  public void updateFileEndingList() {
    String text = filePatternsTextArea.getText();
    fileEndingList = Tools.extractTokens(text);
  }

  public FeedbackTreeItem crawl(File file, int level, Feedback feedback) throws Exception {
    if (level == 1 && file.isDirectory()) {
      String group = file.getName(); // .replaceAll("[^0-9]", "");
      feedback = new Feedback();
      feedback.setGroup(group);
      groupListView.getItems().add(feedback.getGroup());
      tmpManager.importFeedback(feedback);
    }
    FeedbackTreeItem root = new FeedbackTreeItem(file, feedback);

    for (File dirFile : file.listFiles()) {
      if (dirFile.isDirectory()) {
        FeedbackTreeItem childDir = crawl(dirFile, level + 1, feedback);
        root.getChildren().add(childDir);

      } else {
        FeedbackTreeItem child = new FeedbackTreeItem(dirFile, feedback);
        root.getChildren().add(child);

        String[] s = dirFile.getName().split("\\.");
        if (feedback != null && s.length > 1 && fileEndingList.contains(s[s.length - 1])) {
          String content = IO.getFileAsString(dirFile);
          feedback.addFile(dirFile.getName(), content);
        }
      }
    }
    return root;
  }

  private NodeColor crawlNodeStatus(FeedbackTreeItem root, int level, Feedback feedback) {
    NodeColor nodeColor;
    if (level == 1) // level 1 => group root
    feedback = root.getFeedback();

    if (root.isLeaf()) { // leaf => file or empty folder
      if (feedback != null && feedback.getFiles().keySet().contains(root.getValue().getName()))
        nodeColor = NodeColor.GREEN;
      else nodeColor = NodeColor.RED;
    } else {
      List<TreeItem<File>> children = root.getChildren();
      NodeColor[] childNodeColors = new NodeColor[children.size()];

      for (int i = 0; i < children.size(); i++) {
        FeedbackTreeItem treeItem = (FeedbackTreeItem) children.get(i);
        childNodeColors[i] = crawlNodeStatus(treeItem, level + 1, feedback);
      }
      nodeColor = NodeColor.getCombinedStatus(childNodeColors);
      setNodeIcon(root, nodeColor.getImageView());
    }
    setNodeIcon(root, nodeColor.getImageView());
    if (nodeColor != NodeColor.RED) root.setExpanded(true);
    return nodeColor;
  }

  public void onToggleOpenArchives(Event event) {
    openArchives = ((ToggleButton) event.getSource()).isSelected();
  }

  public void initialize(FeedbackManager feedbackManager) {
    realManager = feedbackManager;
    List<String> existingGroups = realManager.getGroups();
    existingGroups.sort(String::compareToIgnoreCase);
    String groups = existingGroups.toString();
    currentGroupsTextField.setText(
        groups.substring(1, groups.length() - 1)
            + "    -    ("
            + existingGroups.size()
            + " total)");
  }

  public void setListener(ManagerListener listener) {
    this.listener = listener;
  }

  private void close(boolean managerChanged) {
    saveFilePatterns();
    managerChanged = managerChanged && !tmpManager.getFeedbackList().isEmpty();
    listener.close(managerChanged);
  }

  public enum NodeColor {
    RED,
    GREEN,
    BLUE;

    public static NodeColor getCombinedStatus(NodeColor... status) {
      NodeColor status0 = status[0];

      if (status0 == RED || status0 == GREEN)
        for (int i = 1; i < status.length; i++) if (status[i] != status0) return BLUE;

      return status0;
    }

    public ImageView getImageView() {
      String s = null;

      switch (this) {
        case RED:
          s = "/img/red_circle.png";
          break;
        case GREEN:
          s = "/img/green_circle.png";
          break;
        case BLUE:
          s = "/img/blue_circle.png";
          break;
      }
      return new ImageView(new Image(getClass().getResourceAsStream(s)));
    }
  }

  public static class FeedbackTreeItem extends TreeItem<File> {
    private final Feedback feedback;

    public FeedbackTreeItem(File file, Feedback feedback) {
      super(new FileWithGetNameAsToString(file.getPath()));
      this.feedback = feedback;
    }

    public Feedback getFeedback() {
      return feedback;
    }

    public static class FileWithGetNameAsToString extends File {
      public FileWithGetNameAsToString(String pathname) {
        super(pathname);
      }

      public String toString() {
        return super.getName();
      }
    }
  }
}
