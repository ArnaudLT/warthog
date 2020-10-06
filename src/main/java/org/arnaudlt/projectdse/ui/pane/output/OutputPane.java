package org.arnaudlt.projectdse.ui.pane.output;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OutputPane {


    private final Stage stage;

    private TextArea outputText;


    public OutputPane(Stage stage) {
        this.stage = stage;
    }


    public Node buildOutputPane() {

        this.outputText = new TextArea();
        Tab outputTab = new Tab("Overview", outputText);

        Tab logTab = new Tab("Log", CustomLogAppender.getLogArea());

        TabPane tabPane = new TabPane(outputTab, logTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }


    public void fillOutput(List<String> content) {

        if (content == null || content.isEmpty()) {
            this.outputText.setText("No result !");
        } else {
            this.outputText.setText(String.join("\n", content));
        }
    }

}
