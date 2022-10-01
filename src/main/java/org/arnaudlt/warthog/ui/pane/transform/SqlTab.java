package org.arnaudlt.warthog.ui.pane.transform;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class SqlTab extends Tab {

    private final PoolService poolService;

    private SqlCodeArea sqlArea;


    public SqlTab(PoolService poolService) {

        super();
        this.poolService = poolService;
    }


    public void build(String name) {

        sqlArea = new SqlCodeArea(poolService);
        setContent(sqlArea.getWrappedSqlArea());

        final Label label = new Label(name);
        setGraphic(label);
        final TextField textField = new TextField();

        label.setOnMouseClicked(evt -> {

            textField.setText(label.getText());
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        });

        textField.setOnAction(evt -> {

            label.setText(textField.getText());
        });

        textField.setOnKeyPressed(evt -> {

            if (KeyCode.ENTER.equals(evt.getCode())) {
                label.setText(textField.getText());
                setGraphic(label);
            }
        });

        textField.focusedProperty().addListener((obs, oldValue, newValue) -> {

            if (Boolean.FALSE.equals(newValue)) {
                label.setText(textField.getText());
                setGraphic(label);
            }
        });
    }


    public String getSqlQuery() {

        return this.sqlArea.getActiveQuery();
    }
}
