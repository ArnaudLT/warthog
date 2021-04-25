package org.arnaudlt.warthog.ui.pane.transform;

import javafx.scene.control.Tab;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class SqlTab extends Tab {

    private final PoolService poolService;

    private SqlCodeArea sqlArea;


    public SqlTab(PoolService poolService) {

        super("SQL");
        this.poolService = poolService;
        this.setId("SQL");
    }


    public void build() {

        //https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Select
        this.sqlArea = new SqlCodeArea(poolService);
        this.setContent(this.sqlArea.getWrappedSqlArea());
        this.setClosable(false);
    }


    public String getSqlQuery() {

        return this.sqlArea.getActiveQuery();
    }
}
