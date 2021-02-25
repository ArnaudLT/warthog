package org.arnaudlt.warthog;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.ui.MainPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

public class WarthogFXApplication extends Application {


    private ConfigurableApplicationContext context;


    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer =
                ctx -> {
                    ctx.registerBean(Application.class, () -> WarthogFXApplication.this);
                    ctx.registerBean(Parameters.class, this::getParameters);
                    ctx.registerBean(HostServices.class, this::getHostServices);
                };
        this.context = new SpringApplicationBuilder()
                .sources(WarthogApplication.class)
                .initializers(initializer)
                .run(getParameters().getRaw().toArray(new String[0]));
    }


    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }


    @Override
    public void start(Stage stage) {
        this.context.publishEvent(new StageReadyEvent(stage));
    }
}


@Slf4j
@Component
class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ApplicationContext applicationContext;

    private final NamedDatasetManager namedDatasetManager;

    private PoolService poolService;


    @Autowired
    StageInitializer(ApplicationContext applicationContext, NamedDatasetManager namedDatasetManager, PoolService poolService) {

        this.applicationContext = applicationContext;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
    }


    @SneakyThrows
    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {

        Stage stage = stageReadyEvent.getStage();
        stage.setTitle(" - Warthog - ");
        stage.getIcons().add(new Image("/warthog_icon.png"));
        stage.setOnCloseRequest(closeApplication);

        MainPane mainPane = new MainPane(stage, namedDatasetManager, poolService);
        Parent root = mainPane.build();

        Scene scene = new Scene(root, 1280, 720);
        String styleSheet = getClass().getResource("/style.css").toExternalForm();
        String sqlStyleSheet = getClass().getResource("/sql-keywords.css").toExternalForm();

        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(false);
        metro.setScene(scene);

        stage.setScene(scene);
        scene.getStylesheets().add(styleSheet);
        scene.getStylesheets().add(sqlStyleSheet);
        stage.show();
    }


    private final EventHandler<WindowEvent> closeApplication = event  -> {

        if (poolService.isActive()) {

            log.warn("Request close while tasks are running !");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Are you sure ?");
            alert.setHeaderText("Tasks are still running !");
            alert.setContentText("Are you sure that you want to quit and cancel tasks ?");
            Optional<ButtonType> response = alert.showAndWait();
            if (ButtonType.OK != response.get()) {
                log.info("Request close cancelled");
                event.consume(); // prevent closing ?
                return;
            }
        }
        poolService.shutdown();
    };

}


class StageReadyEvent extends ApplicationEvent {

    private final Stage stage;

    StageReadyEvent(Stage stage) {
        super(stage);
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
