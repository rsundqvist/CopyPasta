package gui.newfeedback;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

public class GroupViewController {
  @FXML Tab feedbackTab, filesTab;

  public GroupViewController() {}

  public void setFilesTabText (String newCount) {
      filesTab.setText(newCount);
  }
}
