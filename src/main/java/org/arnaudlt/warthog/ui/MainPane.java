package org.arnaudlt.warthog.ui;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import org.arnaudlt.warthog.ui.pane.control.ControlPane;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainPane {

    private ControlPane controlPane;

    private ExplorerPane explorerPane;

    private TransformPane transformPane;

    private OutputPane outputPane;

    private Stage stage;


    @Autowired
    public MainPane(ControlPane controlPane, ExplorerPane explorerPane, TransformPane transformPane,
                    OutputPane outputPane) {

        this.controlPane = controlPane;
        this.explorerPane = explorerPane;
        this.transformPane = transformPane;
        this.outputPane = outputPane;

        this.controlPane.setExplorerPane(this.explorerPane);
        this.controlPane.setTransformPane(this.transformPane);
        this.controlPane.setOutputPane(this.outputPane);
        this.explorerPane.setTransformPane(this.transformPane);
        this.explorerPane.setControlPane(this.controlPane);
    }


    public Parent build() {

        this.controlPane.setStage(stage);
        this.explorerPane.setStage(stage);
        this.transformPane.setStage(stage);
        this.outputPane.setStage(stage);

        Node controlNode = this.controlPane.buildControlPane();
        Node explorerNode = this.explorerPane.buildExplorerPane();
        Node transformNode = this.transformPane.buildTransformPane();
        Node outputNode = this.outputPane.buildOutputPane();

        SplitPane middleSplitPane = new SplitPane();
        middleSplitPane.setOrientation(Orientation.HORIZONTAL);
        middleSplitPane.getItems().addAll(explorerNode, transformNode);
        middleSplitPane.setDividerPositions(0.226);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(controlNode, middleSplitPane, outputNode);
        splitPane.setDividerPositions(0, 0.58, 0.42);

        return splitPane;
    }

    public void setStage(Stage stage) {

        this.stage = stage;
    }
}
