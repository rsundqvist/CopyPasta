package gui.feedback;

import gui.settings.Settings;
import javafx.scene.layout.BorderPane;
import model.Feedback;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.ViewActions;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by Richard Sundqvist on 12/04/2017. */
public class FeedbackText extends BorderPane implements FileViewController.FileFeedbackListener {

  // region strings
  private static final String TAG_PATTERN = "\\%(.*?)\\%";
  private static final Pattern PATTERN = Pattern.compile("(?<TAG>" + TAG_PATTERN + ")");
  // endregion

  private final CodeArea codeArea;
  private Feedback feedback;

  public FeedbackText() {
    codeArea = new CodeArea();
    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    codeArea.setShowCaret(ViewActions.CaretVisibility.ON);
    codeArea
        .textProperty()
        .addListener(
            event -> {
              feedback.setContent(codeArea.getText());
            });

    codeArea
        .richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(
            change -> {
              String text = codeArea.getText();
              if (text != null && !text.isEmpty()) // Prevent exception
              codeArea.setStyleSpans(0, computeHighlighting(text));
            });
    setCenter(new VirtualizedScrollPane<>(codeArea));
  }

  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass = matcher.group("TAG") != null ? "tag" : null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  private static String caretString(int caretLine, int caretColumn) {
    StringBuilder stringBuilder = new StringBuilder();

    if (caretLine != -1) {
      stringBuilder.append("L" + caretLine);
      if (caretColumn != -1) stringBuilder.append(", ");
    }
    if (caretColumn != -1) stringBuilder.append("C" + caretColumn);

    return stringBuilder.toString();
  }

  private static String fileTagString(String file) {
    file = " " + file + " ";
    if (file.length() % 2 != 0) file = file + " ";

    int width = Settings.FILE_DECORATION_WIDTH;
    int sz = (width - file.length()) / 2; // space per side
    int numRepeats = sz / 2;
    String extra = sz % 2 == 0 ? "" : "<>";

    String around = new String(new char[numRepeats]).replace("\0", "<>");
    String border = new String(new char[width]).replace("\0", "=");
    return "\n\n"
        + border
        + "\n"
        + around
        + file
        + around
        + extra
        + "\n"
        + border
        + "\n"
        + Feedback.getFileTag(file);
  }

  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
    updateColor();
    setText(feedback.getContent());
  }

  public String getText() {
    return codeArea.getText();
  }

  public void setText(String text) {
    codeArea.replaceText(0, 0, text);
  }

  public void updateColor() {
    if (feedback.isDone())
      codeArea.setStyle(
          "-fx-font-family: monospaced regular; -fx-font-size: 11pt; -fx-background-color: #55e055;");
    else
      codeArea.setStyle(
          "-fx-font-family: monospaced regular; -fx-font-size: 11pt; -fx-background-color: #dddddd;");
  }

  @Override
  public void feedbackAt(String file, int caretLine, int caretColumn, int caretPosition) {
    int pos = feedback.getFileTagPosition(file);

    String caretInfo = caretString(caretLine, caretColumn);
    String text = "\nAt " + caretInfo + ":  \n";
    if (pos < 0) { // No FILE-tag
      pos = feedback.getContent().indexOf(Feedback.FOOTER) - 1; // Place above footer, if it exists.
      if (pos < 0) // No footer - place at end of file.
      pos = feedback.getContent().length();

      text = fileTagString(file) + text;
    }
    insertText(pos, text);
  }

  public void feedbackAt(
      String file, String content, int caretLine, int caretColumn, int caretPosition) {
    String text;
    if (caretLine < 0 && caretColumn < 0) text = "\n";
    else text = "\nAt " + caretString(caretLine, caretColumn) + ":\n";

    int pos = feedback.getFileTagPosition(file);
    if (pos < 0) { // No FILE-tag or footer present.
      pos = feedback.getContent().indexOf(Feedback.FOOTER) - 1; // Place above footer, if it exists.
      if (pos < 0) // No footer - place at end of file.
      pos = feedback.getContent().length();

      text = fileTagString(file) + text;
    }
    insertText(pos, text + content);
  }

  public void insertText(int pos, String s) {
    codeArea.insertText(pos, s);
  }

  public void insertTextAtCaret(String s) {
    insertText(codeArea.getCaretPosition(), s);
  }
}
