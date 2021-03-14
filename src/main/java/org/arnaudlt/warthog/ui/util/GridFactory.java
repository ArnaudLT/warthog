package org.arnaudlt.warthog.ui.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;

public class GridFactory {

    private GridFactory() {
    }





    public static GridPane buildGrid() {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setPadding(new Insets(20,20,20,20));
        grid.setHgap(10);
        grid.setVgap(10);

        return grid;
    }


    public static GridPane buildGrid(Insets insets) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setPadding(insets);
        grid.setHgap(10);
        grid.setVgap(10);

        return grid;
    }

}
