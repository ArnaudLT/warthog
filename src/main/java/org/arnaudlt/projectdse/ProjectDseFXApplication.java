package org.arnaudlt.projectdse;

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
import lombok.extern.log4j.Log4j2;
import org.apache.log4j.PropertyConfigurator;
import org.arnaudlt.projectdse.model.dataset.NamedDatasetManager;
import org.arnaudlt.projectdse.ui.MainPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Properties;

public class ProjectDseFXApplication extends Application {


    private ConfigurableApplicationContext context;


    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer =
                ctx -> {
                    ctx.registerBean(Application.class, () -> ProjectDseFXApplication.this);
                    ctx.registerBean(Parameters.class, this::getParameters);
                    ctx.registerBean(HostServices.class, this::getHostServices);
                };
        this.context = new SpringApplicationBuilder()
                .sources(ProjectDseApplication.class)
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


@Log4j2
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

        // For logger inside the app
        Properties loggingProperties = new Properties();
        loggingProperties.load(getClass().getResourceAsStream("/log4j.properties"));
        PropertyConfigurator.configure(loggingProperties);

        MainPane mainPane = new MainPane(stage, namedDatasetManager, poolService);
        Parent root = mainPane.build();

        Scene scene = new Scene(root, 1280, 720);
        String styleSheet = getClass().getResource("/style.css").toExternalForm();
        stage.setTitle(" - Warthog - ");
        stage.getIcons().add(new Image("file:src/main/resources/black-metal.png"));
        stage.setOnCloseRequest(closeApplication);

        JMetro metro = new JMetro(Style.DARK);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(scene);

        stage.setScene(scene);
        scene.getStylesheets().add(styleSheet);
        stage.show();
    }


    private final EventHandler<WindowEvent> closeApplication = event  -> {

        if (poolService.isActive()) {

            log.warn("Request close while tasks are running !");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Are you sure ?");
            alert.setHeaderText("Tasks are still running !");
            alert.setContentText("Are you sure that you want to quit an cancel tasks ?");
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
