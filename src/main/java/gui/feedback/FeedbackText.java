package gui.feedback;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import model.Feedback;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Richard Sundqvist on 12/04/2017.
 */
public class FeedbackText extends BorderPane implements StudentFileViewerController.FileFeedbackListener {

    //region strings
    private static final String TAG_PATTERN = "\\%(.*?)\\%";

    private static final Pattern PATTERN = Pattern.compile("(?<TAG>" + TAG_PATTERN + ")");
    //endregion

    private final CodeArea codeArea;
    private final Feedback feedback;

    public FeedbackText (Feedback feedback) {
        this.feedback = feedback;
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener(event -> {
            feedback.setContent(codeArea.getText());
        });

        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
                });
        setCenter(new VirtualizedScrollPane<>(codeArea));
        codeArea.replaceText(0, 0, feedback.getContent());
        updateColor();
    }

    private static StyleSpans<Collection<String>> computeHighlighting (String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
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

    public String getText () {
        return codeArea.getText();
    }

    public void updateColor () {
        if (feedback.isDone())
            codeArea.setStyle("-fx-font-family: Arial; -fx-font-size: 10pt; -fx-background-color: #55e055;");
        else
            codeArea.setStyle("-fx-font-family: Arial; -fx-font-size: 10pt; -fx-background-color: #ffeaea;");
    }

    private static String caretString (int caretLine, int caretColumn) {
        StringBuilder stringBuilder = new StringBuilder();

        if (caretLine != -1) {
            stringBuilder.append("L" + caretLine);
            if (caretColumn != -1)
                stringBuilder.append(", ");
        }
        if (caretColumn != -1)
            stringBuilder.append("C" + caretColumn);

        return stringBuilder.toString();
    }

    @Override
    public void feedbackAt (String file, int caretLine, int caretColumn, int caretPosition) {
        int pos = feedback.getFilePosition(file);

        String caretInfo = caretString(caretLine, caretColumn);
        if (pos < 0) {
            codeArea.appendText("\nIn \"" + file + "\" at " + caretInfo + ":  \n\n");
        } else {
            String text = "\nAt " + caretInfo + ":  \n";
            codeArea.insertText(pos, text);
        }
    }
}
