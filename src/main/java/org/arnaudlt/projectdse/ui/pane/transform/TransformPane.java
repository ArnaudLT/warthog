package org.arnaudlt.projectdse.ui.pane.transform;

import javafx.collections.ListChangeListener;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;
import org.arnaudlt.projectdse.model.dataset.NamedDatasetManager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TransformPane {


    private final Stage stage;

    private final NamedDatasetManager namedDatasetManager;

    private TabPane namedDatasetsTabPane;

    private ConcurrentMap<NamedDataset, NamedDatasetTab> namedDatasetToTab;


    public TransformPane(Stage stage, NamedDatasetManager namedDatasetManager) {
        this.stage = stage;
        this.namedDatasetManager = namedDatasetManager;
    }


    public Node buildTransformPane() {

        this.namedDatasetsTabPane = new TabPane();
        this.namedDatasetsTabPane.setSide(Side.BOTTOM);
        this.namedDatasetsTabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);
        this.namedDatasetsTabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            while (change.next()) {

                List<NamedDatasetTab> removedList = (List<NamedDatasetTab>) change.getRemoved();
                for (NamedDatasetTab closedTab : removedList ){

                    log.info("Named dataset tab {} closed", closedTab.getNamedDataset().getName());
                    this.namedDatasetToTab.remove(closedTab.getNamedDataset());
                }
            }
        });

        this.namedDatasetToTab = new ConcurrentHashMap<>();

        this.namedDatasetsTabPane.setPrefWidth(1080);
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


    private NamedDatasetTab buildNamedDatasetTab(NamedDataset namedDataset) {

        NamedDatasetTab namedDatasetTab = new NamedDatasetTab(namedDatasetManager, namedDataset);
        namedDatasetTab.build();

        return namedDatasetTab;
    }


    public NamedDataset getSelectedNamedDataset() {

        NamedDatasetTab selectedItem = (NamedDatasetTab) this.namedDatasetsTabPane.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.getNamedDataset();
        } else {
            return null;
        }
    }

}
