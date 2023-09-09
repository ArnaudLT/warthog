package org.arnaudlt.warthog.model.history;

import com.google.gson.Gson;
import com.sun.nio.file.ExtendedOpenOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.setting.ImportSettings;
import org.arnaudlt.warthog.model.user.WorkspaceSettings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Slf4j
public class Workspace {


    private final Gson gson;

    private WorkspaceSettings workspaceSettings;

    private final List<ImportSettings> importSettings;


    public Workspace(Gson gson, WorkspaceSettings workspaceSettings, List<ImportSettings> importSettings) {
        this.gson = gson;
        this.workspaceSettings = workspaceSettings;
        this.importSettings = importSettings;
    }


    public static Workspace load(Gson gson, WorkspaceSettings workspaceSettings) throws FileNotFoundException {

        log.info("Start to load previous workspace");

        Workspace workspace;

        SerializableWorkspace serializableWorkspace = gson.fromJson(
                new FileReader(Paths.get(workspaceSettings.getDirectory(), "datasets.json").toFile()), SerializableWorkspace.class);
        workspace = new Workspace(gson, workspaceSettings, serializableWorkspace.importSettings);

        while (workspace.importSettings.remove(null)) ;

        log.info("{} datasets loaded from previous session", workspace.importSettings.size());
        return workspace;
    }


    public void persist(List<ImportSettings> importSettings) {

        String importSettingsAsString = gson.toJson(new SerializableWorkspace(importSettings));
        try {
            Files.createDirectories(Paths.get(workspaceSettings.getDirectory()));
            Files.writeString(Paths.get(workspaceSettings.getDirectory(), "datasets.json"), importSettingsAsString,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    ExtendedOpenOption.NOSHARE_WRITE);
        } catch (IOException e) {
            log.warn("Unable to save workspace", e);
        }
    }


    public List<ImportSettings> getImportSettings() {
        return importSettings;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SerializableWorkspace implements Serializable {

        private List<ImportSettings> importSettings;
    }

}
