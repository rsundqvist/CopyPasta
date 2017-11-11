package gui.feedback;

import gui.feedback.JavaCodeArea;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional;

/** Created by Richard Sundqvist on 12/04/2017. */
public class FileTab extends Tab {

  private final CodeArea codeArea;
  private final int firstNumber; // Set to 1 to set the first line to 1.

  public FileTab (String fileName, String content) {
    this(fileName, content, false);
  }

  /**
   * @param fileName Name of the file.
   * @param content Content of the file
   * @param startFromZero If {@code true}, row count begins at zero.
   */
  public FileTab (String fileName, String content, boolean startFromZero) {
    firstNumber = startFromZero ? 0 : 1;

    setText(fileName);
    setClosable(false);

    codeArea = new JavaCodeArea(content);
    codeArea.setEditable(false);
    setContent(new VirtualizedScrollPane<>(codeArea));
  }

  public void setContent(String content) {
    codeArea.replaceText(content);
  }

  public String getFileName() {
    return getText();
  }

  public int getCaretLine() {
    int offset = codeArea.getCaretPosition();
    TwoDimensional.Position pos = codeArea.offsetToPosition(offset, TwoDimensional.Bias.Forward);
    return pos.getMajor() + firstNumber;
  }

  public String getCodeAreaContent() {
    return codeArea.getText();
  }

  public int getCaretColumn() {
    int offset = codeArea.getCaretPosition();
    TwoDimensional.Position pos = codeArea.offsetToPosition(offset, TwoDimensional.Bias.Forward);
    return pos.getMinor() + firstNumber;
  }

  public int getCaretPosition() {
    return codeArea.getCaretPosition();
  }

  public void setEditable(boolean value) {
    codeArea.setEditable(value);
  }
}
