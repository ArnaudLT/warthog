package org.arnaudlt.warthog.model.user;


import javafx.beans.property.SimpleStringProperty;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public final class UserSettings {

    private String directory;

    private SimpleStringProperty preferredDownloadDirectory;


    public UserSettings(UserSettings user) {

        this.directory = user.directory;
        this.preferredDownloadDirectory = user.preferredDownloadDirectory;
    }


    public UserSettings(GlobalSettings.SerializableUserSettings user) {

        this.preferredDownloadDirectory = new SimpleStringProperty();
        if (user != null) {
            this.preferredDownloadDirectory.setValue(user.getPreferredDownloadDirectory());
        }
    }


    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getPreferredDownloadDirectory() {
        return preferredDownloadDirectory.get();
    }

    public SimpleStringProperty preferredDownloadDirectoryProperty() {
        return preferredDownloadDirectory;
    }

    public void setPreferredDownloadDirectory(String preferredDownloadDirectory) {
        this.preferredDownloadDirectory.set(preferredDownloadDirectory);
    }
}
