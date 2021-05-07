package org.arnaudlt.warthog.ui.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class StageFactory {

    private StageFactory() {}


    public static Stage buildModalStage(Stage owner, String title) {

        return buildModalStage(owner, title, Modality.APPLICATION_MODAL, StageStyle.DECORATED, false);
    }


    public static Stage buildModalStage(Stage owner, String title, Modality modality, StageStyle stageStyle, boolean isResizable) {

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.getIcons().add(new Image("/warthog_icon.png"));
        stage.initModality(modality);
        stage.initStyle(stageStyle);
        stage.initOwner(owner);
        stage.setResizable(isResizable);

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


    public static Scene buildScene(Parent root, double width, double height) {

        Scene dialogScene = new Scene(root, width, height);
        return enrichScene(dialogScene);
    }


    public static Scene buildScene(Parent root) {

        Scene dialogScene = new Scene(root);
        return enrichScene(dialogScene);
    }


    private static Scene enrichScene(Scene scene) {

        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(scene);
        String styleSheet = StageFactory.class.getResource("/style.css").toExternalForm();
        String sqlStyleSheet = StageFactory.class.getResource("/sql-keywords.css").toExternalForm();
        scene.getStylesheets().add(styleSheet);
        scene.getStylesheets().add(sqlStyleSheet);
        return scene;
    }

}
