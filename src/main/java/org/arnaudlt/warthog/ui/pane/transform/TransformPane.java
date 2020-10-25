package org.arnaudlt.warthog.ui.pane.transform;

import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.NamedDataset;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TransformPane {


    private final Stage stage;

    private final PoolService poolService;

    private TabPane namedDatasetsTabPane;

    private ConcurrentMap<NamedDataset, NamedDatasetTab> namedDatasetToTab;


    public TransformPane(Stage stage, PoolService poolService) {

        this.stage = stage;
        this.poolService = poolService;
    }


    public Node buildTransformPane() {

        this.namedDatasetsTabPane = new TabPane();
        this.namedDatasetsTabPane.setSide(Side.BOTTOM);
        this.namedDatasetsTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        SqlTab sqlTab = new SqlTab(poolService);
        sqlTab.build();
        this.namedDatasetsTabPane.getTabs().add(sqlTab); // Permanent tab, always added (not closeable)

        this.namedDatasetsTabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            while (change.next()) {

                List<NamedDatasetTab> removedList = (List<NamedDatasetTab>) change.getRemoved();
                for (NamedDatasetTab closedTab : removedList ) {

                    log.info("Named dataset tab {} closed", closedTab.getNamedDataset().getName());
                    this.namedDatasetToTab.remove(closedTab.getNamedDataset());
                }
            }
        });

        DecimalFormat formatter = new DecimalFormat("#.##");
        this.stage.titleProperty().bind(Bindings.createStringBinding(() -> {

            NamedDataset selectedNamedDataset = this.getSelectedNamedDataset();
            if (selectedNamedDataset != null) {

                return " - Warthog - " +
                       selectedNamedDataset.getDecoration().getFilePath().toString() + " - " +
                       formatter.format(selectedNamedDataset.getDecoration().getSizeInMegaBytes()) + "MB";
            }
            return " - Warthog - SQL";
        }, this.namedDatasetsTabPane.getSelectionModel().selectedItemProperty()));

        this.namedDatasetToTab = new ConcurrentHashMap<>();
        return this.namedDatasetsTabPane;
    }


    public void openNamedDataset(NamedDataset namedDataset) {

        NamedDatasetTab tab;
        if (!this.namedDatasetToTab.containsKey(namedDataset)) {

            log.info("Open a new named dataset tab for {}", namedDataset.getName());
            tab = buildNamedDatasetTab(namedDataset);
            this.namedDatasetsTabPane.getTabs().add(tab);
            this.namedDatasetToTab.put(namedDataset, tab);
        } else {

            log.info("Named dataset tab for {} is already opened", namedDataset.getName());
            tab = this.namedDatasetToTab.get(namedDataset);
        }
        this.namedDatasetsTabPane.getSelectionModel().select(tab);
    }


    public void closeNamedDataset(NamedDataset namedDataset) {

        NamedDatasetTab namedDatasetTab = this.namedDatasetToTab.get(namedDataset);
        if (namedDatasetTab != null) {

            this.namedDatasetsTabPane.getTabs().remove(namedDatasetTab);
        }
        this.namedDatasetToTab.remove(namedDataset);
    }


    private NamedDatasetTab buildNamedDatasetTab(NamedDataset namedDataset) {

        NamedDatasetTab namedDatasetTab = new NamedDatasetTab(namedDataset);
        namedDatasetTab.build();

        return namedDatasetTab;
    }


    public NamedDataset getSelectedNamedDataset() {

        Tab selectedTab = this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof NamedDatasetTab) {

            NamedDatasetTab selectedNamedDatasetTab = (NamedDatasetTab) selectedTab;
            return selectedNamedDatasetTab.getNamedDataset();
        } else {
            return null;
        }
    }


    public String getSqlQuery() {

        Tab selectedTab = this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab instanceof SqlTab) {

            SqlTab selectedSqlTab = (SqlTab) selectedTab;
            return selectedSqlTab.getSqlQuery();
        } else {
            return null;
        }
    }

}
