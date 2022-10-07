package org.arnaudlt.warthog.ui.util;

import javafx.scene.control.ComboBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.text.DecimalFormat;

@Slf4j
public class Utils {

    private static final DecimalFormat formatter = new DecimalFormat("#.##");


    private Utils() {}


    public static <T> void refreshComboBoxItems(ComboBox<T> comboBox) {

        T selectedItem = comboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            comboBox.getSelectionModel().selectFirst();
        }
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
