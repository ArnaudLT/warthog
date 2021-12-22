package org.arnaudlt.warthog.ui.pane.transform;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransformPane {


    private Stage stage;

    private final PoolService poolService;

    private TabPane namedDatasetsTabPane;


    @Autowired
    public TransformPane(PoolService poolService) {

        this.poolService = poolService;
    }


    public Node buildTransformPane(Stage stage) {

        this.stage = stage;

        this.namedDatasetsTabPane = new TabPane();
        this.namedDatasetsTabPane.setSide(Side.BOTTOM);
        this.namedDatasetsTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        SqlTab sqlTab = new SqlTab(poolService);
        sqlTab.build();
        this.namedDatasetsTabPane.getTabs().add(sqlTab); // Permanent tab, always added (not closeable)

        return this.namedDatasetsTabPane;
    }


    public String getSqlQuery() {

        SqlTab selectedSqlTab = (SqlTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        return selectedSqlTab.getSqlQuery();
    }


}
