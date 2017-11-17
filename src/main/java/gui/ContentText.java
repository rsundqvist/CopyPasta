package gui;

import model.Content;
import model.Feedback;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by Richard Sundqvist on 12/04/2017. */
public class ContentText extends CodeArea {
  public static final String DEFAULT_STYLE =
      "-fx-font-family: monospaced regular; -fx-font-size: 11pt; -fx-background-color: #dcdcdc;";

  // region strings
  private static final String TAG_PATTERN = createTagPattern(); // "\\%(.*?)\\%";
  private static final Pattern PATTERN = Pattern.compile("(?<TAG>" + TAG_PATTERN + ")");
  // endregion
  protected Content content;

  public ContentText() {
    setParagraphGraphicFactory(LineNumberFactory.get(this));
    textProperty()
        .addListener(
            event -> {
              if (content != null) content.setContent(getText());
            });

    richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(
            change -> {
              String text = getText();
              if (text != null && !text.isEmpty()) // Prevent exception
              setStyleSpans(0, computeHighlighting(text));
            });

    setStyle(DEFAULT_STYLE);
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

  private static String createTagPattern() {
    String[] s = {
      Feedback.HEADER,
      Feedback.FOOTER,
      Feedback.GROUP,
      Feedback.GRADE,
      Feedback.MANUAL,
      Feedback.SIGNATURE,
      Feedback.FILE_REGEX
    };
    StringBuilder sb = new StringBuilder();
    for (String regex : s) sb.append(regex + "|");
    return sb.toString();
  }

  public void setText(String text) {
    replaceText(text);
  }

  public void setContent(Content content) {
    this.content = content;
    setText(content.getContent());
  }
}
