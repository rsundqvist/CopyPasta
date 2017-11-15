package gui.feedback;

import gui.JavaCodeArea;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.model.TwoDimensional;

/** Created by Richard Sundqvist on 12/04/2017. */
public class FileTab extends Tab {

  private final JavaCodeArea jca;
  private final int firstNumber; // Set to 1 to set the first line to 1.

  public FileTab(String fileName, String content) {
    this(fileName, content, false);
  }

  /**
   * @param fileName Name of the file.
   * @param content Content of the file
   * @param startFromZero If {@code true}, row count begins at zero.
   */
  public FileTab(String fileName, String content, boolean startFromZero) {
    firstNumber = startFromZero ? 0 : 1;

    setText(fileName);
    setClosable(false);

    jca = new JavaCodeArea(content);
    jca.setEditable(false);
    setContent(new VirtualizedScrollPane<>(jca));
    jca.moveTo(0);
    jca.requestFollowCaret();
  }

  public void setContentText(String text) {
    jca.replaceText(text);
  }

  public String getFileName() {
    return getText();
  }

  public int getCaretLine() {
    int offset = jca.getCaretPosition();
    TwoDimensional.Position pos = jca.offsetToPosition(offset, TwoDimensional.Bias.Forward);
    return pos.getMajor() + firstNumber;
  }

  public String getCodeAreaContent() {
    return jca.getText();
  }

  public int getCaretColumn() {
    int offset = jca.getCaretPosition();
    TwoDimensional.Position pos = jca.offsetToPosition(offset, TwoDimensional.Bias.Forward);
    return pos.getMinor() + firstNumber;
  }

  public int getCaretPosition() {
    return jca.getCaretPosition();
  }

  public void setEditable(boolean value) {
    jca.setEditable(value);
  }

  public boolean isTextFocused() {
    return jca.isFocused();
  }
}
