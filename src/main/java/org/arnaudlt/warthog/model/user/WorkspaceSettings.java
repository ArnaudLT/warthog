package org.arnaudlt.warthog.model.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceSettings {

    private String directory;

    public WorkspaceSettings(WorkspaceSettings workspaceSettings) {

        this.directory = workspaceSettings.getDirectory();
    }
}
