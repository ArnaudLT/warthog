package org.arnaudlt.warthog.ui.pane.transform;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.service.SaveToSqlFileService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.LabelFactory;

import java.io.File;

@Slf4j
@EqualsAndHashCode
public class SqlTab extends Tab {

    private final Stage owner;

    private final PoolService poolService;

    private final SqlCodeArea sqlArea;

    private SimpleObjectProperty<File> sqlFile;


    public SqlTab(Stage owner, PoolService poolService) {

        super();
        this.owner = owner;
        this.poolService = poolService;
        this.sqlFile = new SimpleObjectProperty<>(null);
        this.sqlArea = new SqlCodeArea(poolService);
    }


    public SqlTab(Stage owner, PoolService poolService, File sqlFile, String sqlFileContent) {

        super();
        this.owner = owner;
        this.poolService = poolService;
        this.sqlFile = new SimpleObjectProperty<>(sqlFile);
        this.sqlArea = new SqlCodeArea(poolService);
        this.sqlArea.setText(sqlFileContent);
    }


    public void build(int id) {

        setContent(sqlArea.getWrappedSqlArea());
        buildGraphic(id);
    }


    private void buildGraphic(int id) {

        if (sqlFile.getValue() == null) {

            buildSqlBufferGraphic(id);
        } else {

            buildSqlFileGraphic();
        }
    }


    private void buildSqlBufferGraphic(int id) {

        String tabName = "SQL";
        if (id > 0) {
            tabName = "SQL (" + id + ")";
        }

        final Label label = new Label(tabName);
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


    private void buildSqlFileGraphic() {

        String tabName = sqlFile.getValue().getName();
        HBox hBoxContainer = new HBox(7, LabelFactory.buildSegoeLabel("\uE7C3"), new Label(tabName));
        hBoxContainer.setAlignment(Pos.BASELINE_LEFT);
        setGraphic(hBoxContainer);
    }


    public boolean isLinkedToAFile() {

        return sqlFile.getValue() != null;
    }


    public void saveToFile() {

        SaveToSqlFileService saveToSqlFileService = new SaveToSqlFileService(poolService, sqlArea.getText(), sqlFile.getValue());
        saveToSqlFileService.setOnSucceeded(success -> {
            log.info("Sql tab content saved to {}", sqlFile.getValue().getName());
        });
        saveToSqlFileService.setOnFailed(fail ->
                AlertFactory.showFailureAlert(owner, fail, "Unable to save to file " + sqlFile.getValue().getAbsolutePath()));
        saveToSqlFileService.start();
    }


    public void saveToFile(File targetSqlFile) {

        SaveToSqlFileService saveToSqlFileService = new SaveToSqlFileService(poolService, sqlArea.getText(), targetSqlFile);
        saveToSqlFileService.setOnSucceeded(success -> {
            log.info("Sql tab content saved to {}", targetSqlFile.getName());
            sqlFile.setValue(targetSqlFile);
            buildSqlFileGraphic();
        });
        saveToSqlFileService.setOnFailed(fail ->
                AlertFactory.showFailureAlert(owner, fail, "Unable to save to file " + targetSqlFile.getAbsolutePath()));
        saveToSqlFileService.start();
    }


    public File getSqlFile() {
        return sqlFile.get();
    }


    public SimpleObjectProperty<File> sqlFileProperty() {
        return sqlFile;
    }


    public void setSqlFile(File sqlFile) {
        this.sqlFile.set(sqlFile);
    }


    public String getSqlQuery() {

        return this.sqlArea.getActiveQuery();
    }
}
