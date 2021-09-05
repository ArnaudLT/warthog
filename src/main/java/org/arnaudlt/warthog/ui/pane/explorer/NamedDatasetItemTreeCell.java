package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.stage.Stage;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.Utils;

import java.util.stream.Collectors;

public class NamedDatasetItemTreeCell extends TreeCell<NamedDatasetItem> {


    private final Stage stage;


    public NamedDatasetItemTreeCell(Stage stage) {

        super();
        this.stage = stage;
    }


    @Override
    public void updateItem(NamedDatasetItem namedDatasetItem, boolean empty) {

        super.updateItem(namedDatasetItem, empty);
        if (empty) {

            setContextMenu(null);
            setText(null);
            setGraphic(null);
        } else {

            MenuItem copyMenuItem = new MenuItem("Copy");
            copyMenuItem.setOnAction(evt -> Utils.copyStringToClipboard(namedDatasetItem.getSqlName()));

            ContextMenu contextMenu = new ContextMenu(copyMenuItem);

            if (namedDatasetItem.getDataType() == null) {

                MenuItem helpMenuItem = buildTableHelpMenuItem(namedDatasetItem);
                contextMenu.getItems().add(helpMenuItem);
            }

            setContextMenu(contextMenu);
            setText(namedDatasetItem.getLabel());
            setGraphic(null);
        }
    }


    private MenuItem buildTableHelpMenuItem(NamedDatasetItem namedDatasetItem) {
        MenuItem helpMenuItem = new MenuItem("Help...");
        helpMenuItem.setOnAction(evt -> {

            String selectAll = namedDatasetItem.getChild().stream()
                    .map(ndi -> "\t" + ndi.getSqlName())
                    .collect(Collectors.joining(",\n"));

            AlertFactory.showInformationAlert(stage, "Select explicitly all fields",
                        "SELECT \n"+ selectAll + "\nFROM " + namedDatasetItem.getSqlName() + ";\n");
        });
        return helpMenuItem;
    }

}
