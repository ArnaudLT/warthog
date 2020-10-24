package org.arnaudlt.warthog.ui.pane.transform;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlTab extends Tab {


    private TextArea sqlArea;


    public SqlTab() {

        super("SQL");
        this.setId("SQL");
    }


    public void build() {

        //https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Select
        this.sqlArea = new TextArea("");
        this.setContent(sqlArea);
        this.setClosable(false);
    }


    public String getSqlQuery() {

        // TODO manage multiple queries in the sheet !
        return this.sqlArea.getText().trim();
    }
}
