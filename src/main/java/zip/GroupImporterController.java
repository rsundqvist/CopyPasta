package zip;

import gui.feedback.JavaCodeArea;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import model.IO;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.File;
import java.io.RandomAccessFile;

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
    Label filePreviewLabel;

    boolean openArchives = true;

    private final JavaCodeArea codeArea;
    private File currentFile;

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

    public void initialize () {
        previewContainer.setCenter(codeArea);

        TreeItem<File> root = new TreeItem<>(new File("foo"));

        TreeItem<File> sub = new TreeItem<>(new File("bar"));
        TreeItem<File> x = new TreeItem<>(new File("x"));

        root.getChildren().addAll(sub, x);
        treeView.setRoot(root);
    }

    public void onImport () {
        filePreviewLabel.setText(currentFile == null ? "NULL" : currentFile.toString());
    }

    public void onChangeRootDirectory () {
        File dir = IO.showDirectoryChooser(null);
        if (dir != null) {
            try {
                crawl(dir);
            } catch (Exception e) {
                IO.showExceptionAlert(e);
                e.printStackTrace();
            }
        }
    }

    private void crawl (File root) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(root, "r"); //Read only
        IInArchive inArchive = SevenZip.openInArchive(null, // autodetect archive type
                new RandomAccessFileInStream(randomAccessFile));
    }

    public void onToggleOpenArchives (Event event) {
        openArchives = ((ToggleButton) event.getSource()).isSelected();
    }
}
