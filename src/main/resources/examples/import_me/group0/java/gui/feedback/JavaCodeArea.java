package gui.feedback;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.ViewActions;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by Richard Sundqvist on 17/04/2017. */
public class JavaCodeArea extends CodeArea {

  // region strings
  private static final String[] KEYWORDS =
      new String[] {
        "abstract",
        "assert",
        "boolean",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        "class",
        "const",
        "continue",
        "default",
        "do",
        "double",
        "else",
        "enum",
        "extends",
        "final",
        "finally",
        "float",
        "for",
        "goto",
        "if",
        "implements",
        "import",
        "instanceof",
        "int",
        "interface",
        "long",
        "native",
        "new",
        "package",
        "private",
        "protected",
        "public",
        "return",
        "short",
        "static",
        "strictfp",
        "super",
        "switch",
        "synchronized",
        "this",
        "throw",
        "throws",
        "transient",
        "try",
        "void",
        "volatile",
        "while",
        "null",
        "->"
      };

  private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
  private static final String PAREN_PATTERN = "\\(|\\)";
  private static final String BRACE_PATTERN = "\\{|\\}";
  private static final String BRACKET_PATTERN = "\\[|\\]";
  private static final String SEMICOLON_PATTERN = "\\;";
  private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
  private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

  private static final Pattern PATTERN =
      Pattern.compile(
          "(?<KEYWORD>"
              + KEYWORD_PATTERN
              + ")"
              + "|(?<PAREN>"
              + PAREN_PATTERN
              + ")"
              + "|(?<BRACE>"
              + BRACE_PATTERN
              + ")"
              + "|(?<BRACKET>"
              + BRACKET_PATTERN
              + ")"
              + "|(?<SEMICOLON>"
              + SEMICOLON_PATTERN
              + ")"
              + "|(?<STRING>"
              + STRING_PATTERN
              + ")"
              + "|(?<COMMENT>"
              + COMMENT_PATTERN
              + ")");
  // endregion

  public JavaCodeArea() {
    this(null);
  }

  public JavaCodeArea(String initialContent) {
    setStyle("-fx-font-family: consolas; -fx-font-size: 11pt;-fx-background-color: #dddddd;");
    setShowCaret(ViewActions.CaretVisibility.ON);
    setParagraphGraphicFactory(LineNumberFactory.get(this));

    richChanges()
        .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
        .subscribe(
            change -> {
              setStyleSpans(0, computeHighlighting(getText()));
            });
    if (initialContent != null) replaceText(0, 0, initialContent);
  }

  // @formatter:off
  private static StyleSpans<Collection<String>> computeHighlighting(String text) {
    Matcher matcher = PATTERN.matcher(text);
    int lastKwEnd = 0;
    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
    while (matcher.find()) {
      String styleClass =
          matcher.group("KEYWORD") != null
              ? "keyword"
              : matcher.group("PAREN") != null
                  ? "paren"
                  : matcher.group("BRACE") != null
                      ? "brace"
                      : matcher.group("BRACKET") != null
                          ? "bracket"
                          : matcher.group("SEMICOLON") != null
                              ? "semicolon"
                              : matcher.group("STRING") != null
                                  ? "string"
                                  : matcher.group("COMMENT") != null
                                      ? "comment"
                                      : null; /* never happens */
      assert styleClass != null;
      spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
      spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
      lastKwEnd = matcher.end();
    }
    spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
    return spansBuilder.create();
  }

  public void setText(String s) {
    if (getText() != null && s != null && s.length() > 0) replaceText(0, getText().length(), s);
  }
  // @formatter:on
}
