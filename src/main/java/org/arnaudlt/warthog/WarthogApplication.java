package org.arnaudlt.warthog;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WarthogApplication {

    public static void main(String[] args) {

        Application.launch(WarthogFXApplication.class, args);
    }
}
