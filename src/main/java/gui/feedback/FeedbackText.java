package gui.feedback;

import gui.ContentText;
import gui.Tools;
import model.Feedback;

public class FeedbackText extends ContentText implements FileViewController.FileViewListener {
  protected static final String DONE_STYLE =
      "-fx-font-family: monospaced regular; -fx-font-size: 11pt; -fx-background-color: #aaffaa;";
  private Feedback feedback;

  private static String caretString(int caretLine, int caretColumn) {
    StringBuilder stringBuilder = new StringBuilder();

    if (caretLine != -1) {
      stringBuilder.append("L" + caretLine);
      if (caretColumn != -1) stringBuilder.append(", ");
    }
    if (caretColumn != -1) stringBuilder.append("C" + caretColumn);

    return stringBuilder.toString();
  }

  private static String getFileTagHeadline(String s) {
    return Tools.getDecoratedHeadline(s) + "\n" + Feedback.getFileTag(s);
  }

  public void updateColor() {
    setStyle(feedback.isDone() ? DONE_STYLE : PLAIN_STYLE);
  }

  @Override
  public void feedbackAt(String file, int caretLine, int caretColumn) {
    insertFeedback(file, "", caretLine, caretColumn);
  }

  public void feedbackAt(String file, String content, int caretLine, int caretColumn) {
    insertFeedback(file, content, caretLine, caretColumn);
  }

  private void insertFeedback(String file, String text, int caretLine, int caretColumn) {
    System.out.println("FeedbackText.insertFeedback");
    String posText = "\nAt " + caretString(caretLine, caretColumn) + ":\n";
    String fileTag = "";

    boolean usingFooter = false;
    int pos = feedback.getFileTagPosition(file);
    if (pos < 0) { // File doesn't exist - look for footer
      pos = feedback.getContent().indexOf(Feedback.FOOTER) - 1;
      usingFooter = pos >= 0;
      if (!usingFooter) pos = feedback.getContent().length(); // Footer doesn't exist - place at end
      fileTag = getFileTagHeadline(file);
    }
    if (usingFooter) text += "\n";
    insertText(pos, fileTag + posText + text);
    if (usingFooter) moveTo(getCaretPosition() - 1);

    requestFollowCaret();
  }

  public void insertTextAtCaret(String s) {
    insertText(getCaretPosition(), s);
  }

  public void setFeedback(Feedback feedback) {
    this.feedback = feedback;
    setContent(feedback);
  }
}
