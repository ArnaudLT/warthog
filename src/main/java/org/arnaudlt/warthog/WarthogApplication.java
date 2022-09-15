package org.arnaudlt.warthog;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("org.arnaudlt.warthog.model.user")
public class WarthogApplication {

    public static void main(String[] args) {

        Application.launch(WarthogFXApplication.class, args);
    }
}
