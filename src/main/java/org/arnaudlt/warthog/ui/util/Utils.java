package org.arnaudlt.warthog.ui.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class Utils {

    private Utils() {}


    // TODO Hack to force the refresh of the names in the combo box.
    // Can be 'fixed' with a callback and a StringProperty for the displayed name ... so I prefer the HACK !
    public static <T> void refreshComboBoxAllItems(ComboBox<T> comboBox) {

        T selectedItem = comboBox.getSelectionModel().getSelectedItem();
        for (int i = 0; i < comboBox.getItems().size(); i++) {

            comboBox.getSelectionModel().select(i);
        }
        comboBox.getSelectionModel().select(selectedItem);
    }
    

    // TODO Hack to force the refresh of the names in the combo box.
    // Can be 'fixed' with a callback and a StringProperty for the displayed name ... so I prefer the HACK !
    public static <T> void refreshTreeViewAllItems(TreeView<T> treeView) {

        TreeItem<T> selectedItem = treeView.getSelectionModel().getSelectedItem();
        for (int i = 0; i < treeView.getRoot().getChildren().size(); i++) {
            treeView.getSelectionModel().select(i);
        }
        treeView.getSelectionModel().select(selectedItem);
    }

}
