package org.arnaudlt.warthog.ui.pane.transform;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.arnaudlt.warthog.PoolService;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SqlCodeArea {

    private final PoolService poolService;

    private final CodeArea codeArea;


    private static final String[] KEYWORDS = new String[] {
            "ALL", "ALTER", "AND", "ARRAY", "AS", "AUTHORIZATION", "BETWEEN", "BIGINT", "BINARY", "BOOLEAN", "BOTH",
            "BY", "CASE", "CAST", "CHAR", "COLUMN", "CONF", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE",
            "CURRENT_TIMESTAMP", "CURSOR", "DATABASE", "DATE", "DECIMAL", "DELETE", "DESCRIBE", "DISTINCT", "DOUBLE",
            "DROP", "ELSE", "END", "EXCHANGE", "EXISTS", "EXTENDED", "EXTERNAL", "FALSE", "FETCH", "FLOAT", "FOLLOWING",
            "FOR", "FROM", "FULL", "FUNCTION", "GRANT", "GROUP", "GROUPING", "HAVING", "IF", "IMPORT", "IN", "INNER",
            "INSERT", "INT", "INTERSECT", "INTERVAL", "INTO", "IS", "JOIN", "LATERAL", "LEFT", "LESS", "LIKE", "LOCAL",
            "MACRO", "MAP", "MORE", "NONE", "NOT", "NULL", "OF", "ON", "OR", "ORDER", "OUT", "OUTER", "OVER",
            "PARTIALSCAN", "PARTITION", "PERCENT", "PRECEDING", "PRESERVE", "PROCEDURE", "RANGE", "READS", "REDUCE",
            "REVOKE", "RIGHT", "ROLLUP", "ROW", "ROWS", "SELECT", "SET", "SMALLINT", "TABLE", "TABLESAMPLE", "THEN",
            "TIMESTAMP", "TO", "TRANSFORM", "TRIGGER", "TRUE", "TRUNCATE", "UNBOUNDED", "UNION", "UNIQUEJOIN", "UPDATE",
            "USER", "USING", "UTC_TMESTAMP", "VALUES", "VARCHAR", "WHEN", "WHERE", "WINDOW", "WITH", "COMMIT", "ONLY",
            "REGEXP", "RLIKE", "ROLLBACK", "START", "CACHE", "CONSTRAINT", "FOREIGN", "PRIMARY", "REFERENCES",
            "DAYOFWEEK", "EXTRACT", "FLOOR", "INTEGER", "PRECISION", "VIEWS", "TIME", "NUMERIC", "SYNC"
    };


    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PARENTHESIS_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\\`([^\"\\\\]|\\\\.)*\\`" + "|" + "\'([^\"\\\\]|\\\\.)*\'";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|\\-\\-(.*)|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PARENTHESIS>" + PARENTHESIS_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
            , Pattern.CASE_INSENSITIVE);

    public SqlCodeArea(PoolService poolService) {

        this.poolService = poolService;
        this.codeArea = new CodeArea(
                "/*\n" +
                "   You can copy (CTRL+C) from the left menu the name of the table/column, and paste it here (CTRL+V)\n" +
                "   Select the query you want to run and press 'CTRL+ENTER'\n" +
                "   https://cwiki.apache.org/confluence/display/Hive/LanguageManual\n" +
                "*/\n"
        );
        this.codeArea.getStyleClass().add("sql-area");
        this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea, i -> "%03d"));

        // Auto indent
        final Pattern whiteSpace = Pattern.compile("^\\s+");
        codeArea.addEventHandler( KeyEvent.KEY_PRESSED, keyEvent -> {

            if ( keyEvent.getCode() == KeyCode.ENTER && codeArea.getCurrentParagraph() > 0) {
                int caretPosition = codeArea.getCaretPosition();
                int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher(codeArea.getParagraph( currentParagraph-1 ).getSegments().get(0));
                if ( m0.find() ) {
                    Platform.runLater(() -> codeArea.insertText(caretPosition, m0.group()));
                }
            }
        });

        this.codeArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(this.codeArea.multiPlainChanges())
                .filterMap(t -> t.isSuccess() ? Optional.of(t.get()) : Optional.empty())
                .subscribe(this::applyHighlighting);
    }


    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {

        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(text);
            }
        };
        poolService.getExecutor().execute(task);
        return task;
    }


    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {

        codeArea.setStyleSpans(0, highlighting);
    }


    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {

            String styleClass = determineStyleClass(matcher);
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }


    private static String determineStyleClass(Matcher matcher) {

        if (matcher.group("KEYWORD") != null) return "keyword";
        if (matcher.group("PARENTHESIS") != null) return "parenthesis";
        if (matcher.group("BRACE") != null) return "brace";
        if (matcher.group("BRACKET") != null) return "bracket";
        if (matcher.group("SEMICOLON") != null) return "semicolon";
        if (matcher.group("STRING") != null) return "string";
        if (matcher.group("COMMENT") != null) return "comment";
        return null;
    }


    public String getActiveQuery() {

        String selectedText = this.codeArea.getSelectedText();

        if (selectedText != null && !selectedText.isBlank()) {
            return selectedText;
        } else {
            return codeArea.getText();
        }
    }


    public Node getWrappedSqlArea() {

        return new VirtualizedScrollPane<>(codeArea);
    }
}
