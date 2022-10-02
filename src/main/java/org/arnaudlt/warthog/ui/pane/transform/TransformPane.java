package org.arnaudlt.warthog.ui.pane.transform;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.service.OpenSqlFileService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

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

        addSqlNewTab();

        return this.namedDatasetsTabPane;
    }


    public SqlTab getSelectedSqlTab() {

        return (SqlTab) namedDatasetsTabPane.getSelectionModel().getSelectedItem();
    }


    public String getSqlQuery() {

        SqlTab selectedSqlTab = (SqlTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        return selectedSqlTab.getSqlQuery();
    }


    public void addSqlNewTab() {

        int openTabsCount = this.namedDatasetsTabPane.getTabs().size();

        SqlTab sqlTab = new SqlTab(stage, poolService);
        sqlTab.build(openTabsCount);
        this.namedDatasetsTabPane.getTabs().add(sqlTab);
        this.namedDatasetsTabPane.getSelectionModel().select(sqlTab);
    }


    public void openSqlFile(File sqlFile) {

        OpenSqlFileService openSqlFileService = new OpenSqlFileService(poolService, sqlFile);

        openSqlFileService.setOnSucceeded(success -> {

            String fileContent = openSqlFileService.getValue();
            int openTabsCount = this.namedDatasetsTabPane.getTabs().size();
            SqlTab sqlFileTab = new SqlTab(stage, poolService, sqlFile, fileContent);
            sqlFileTab.build(openTabsCount);
            this.namedDatasetsTabPane.getTabs().add(sqlFileTab);
            this.namedDatasetsTabPane.getSelectionModel().select(sqlFileTab);
        });

        openSqlFileService.setOnFailed(fail -> AlertFactory.showFailureAlert(stage, fail, "Not able to load sql file '"+ sqlFile.getName()+"'"));
        openSqlFileService.start();
    }


    public void saveAsSqlTabToFile() {

        SqlTab selectedTab = (SqlTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;

        FileChooser fc = new FileChooser();
        File sqlFile = fc.showSaveDialog(stage);
        if (sqlFile == null) return;

        selectedTab.saveToFile(sqlFile);
    }


    public void saveSqlTabToFile() {

        SqlTab selectedTab = (SqlTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !selectedTab.isLinkedToAFile()) return;

        selectedTab.saveToFile();
    }
}
