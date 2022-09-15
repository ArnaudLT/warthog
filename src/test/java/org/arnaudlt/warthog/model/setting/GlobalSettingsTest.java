package org.arnaudlt.warthog.model.setting;

import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.user.GlobalSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@Slf4j
@SpringBootTest
class GlobalSettingsTest {


    @Autowired
    private GlobalSettings globalSettings;


    @Test
    void serdeConfigTest() {

        log.info("Start to serialize {}", globalSettings.toString());

        try (FileOutputStream fos = new FileOutputStream("target/settings.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(globalSettings);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        log.info("Start to deserialize");

        GlobalSettings settings = null;
        try (FileInputStream fis = new FileInputStream("target/settings.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            settings = (GlobalSettings) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        log.info("Deserialized object {}", settings);

    }


}