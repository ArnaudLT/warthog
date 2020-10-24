package org.arnaudlt.warthog.ui.pane.transform;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlTab extends Tab {

    private final NamedDatasetManager namedDatasetManager;


    public SqlTab(NamedDatasetManager namedDatasetManager) {

        super("SQL");
        this.namedDatasetManager = namedDatasetManager;
        this.setId("SQL");
    }


    public void build() {

        //https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Select
        TextArea sqlArea = new TextArea("Not Yet functional !");
        this.setContent(sqlArea);
        this.setClosable(false);
    }

}
