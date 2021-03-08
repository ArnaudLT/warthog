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

    private final ControlPane controlPane;

    private final ExplorerPane explorerPane;

    private final TransformPane transformPane;

    private final OutputPane outputPane;

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

        Node controlNode = this.controlPane.buildControlPane(stage);
        Node explorerNode = this.explorerPane.buildExplorerPane(stage);
        Node transformNode = this.transformPane.buildTransformPane(stage);
        Node outputNode = this.outputPane.buildOutputPane(stage);

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
