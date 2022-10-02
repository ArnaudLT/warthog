package org.arnaudlt.warthog.ui.pane.transform;

import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.service.OpenSqlFileService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

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

        this.namedDatasetsTabPane.setOnDragOver(dragEvent -> {

            if (dragEvent.getDragboard().hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        });

        this.namedDatasetsTabPane.setOnDragDropped(dragEvent -> {

            List<File> files = dragEvent.getDragboard().getFiles();
            for (File file : files) {

                openSqlFile(file);
            }
        });

        addSqlNewTab();

        return this.namedDatasetsTabPane;
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

        SqlTab target = alreadyOpened(sqlFile);
        if (target != null) {
            namedDatasetsTabPane.getSelectionModel().select(target);
            return;
        }

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


    private SqlTab alreadyOpened(File sqlFile) {

        for (Tab t : namedDatasetsTabPane.getTabs()) {

            SqlTab tab = (SqlTab) t;
            if (sqlFile.equals(tab.getSqlFile())) {
                return tab;
            }
        }
        return null;
    }


    public void saveAsSqlTabToFile() {

        SqlTab selectedTab = (SqlTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;

        selectedTab.saveToFileWithChooser();
    }


    public void saveSqlTabToFile() {

        SqlTab selectedTab = (SqlTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !selectedTab.isLinkedToAFile()) return;

        selectedTab.saveToFile();
    }
}
