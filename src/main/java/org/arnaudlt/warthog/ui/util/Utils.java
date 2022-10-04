package org.arnaudlt.warthog.ui.util;

import javafx.scene.control.ComboBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.text.DecimalFormat;

public class Utils {

    private static DecimalFormat formatter = new DecimalFormat("#.##");


    private Utils() {}

    // TODO Hack to force the refresh of the names in the combo box.
    // Can be 'fixed' with a callback and a StringProperty for the displayed name ... so I prefer the HACK !
    public static <T> void refreshComboBoxAllItems(ComboBox<T> comboBox) {

        T selectedItem = comboBox.getSelectionModel().getSelectedItem();
        comboBox.getSelectionModel().clearSelection();
        for (int i = 0; i < comboBox.getItems().size(); i++) {

            comboBox.getSelectionModel().select(i);
        }
        comboBox.getSelectionModel().select(selectedItem);
    }


    public static void copyStringToClipboard(String content) {

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }


    public static String format2Decimals(Double value) {

        return formatter.format(value);
    }

}
