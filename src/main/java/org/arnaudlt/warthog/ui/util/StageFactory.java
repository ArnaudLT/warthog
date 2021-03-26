package org.arnaudlt.warthog.ui.util;

import javafx.stage.Modality;
import javafx.stage.Stage;

public class StageFactory {

    private StageFactory() {}


    public static Stage buildModalStage(Stage owner, String title) {

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);

        stage.setOnShowing(ev -> stage.hide());
        stage.setOnShown(ev -> {

            double centerX = owner.getX() + owner.getWidth() / 2d;
            double centerY = owner.getY() + owner.getHeight() / 2d;
            stage.setX(centerX - stage.getWidth() / 2d);
            stage.setY(centerY - stage.getHeight() / 2d);

            stage.show();
        });

        return stage;
    }

}
