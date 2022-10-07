package org.arnaudlt.warthog.model.user;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public final class UserSettings {

    private String directory;

    private String preferredDownloadDirectory;


    public UserSettings(UserSettings user) {

        this.directory = user.directory;
        this.preferredDownloadDirectory = user.preferredDownloadDirectory;
    }


    public UserSettings(GlobalSettings.SerializableUserSettings user) {

        this.preferredDownloadDirectory = user.getPreferredDownloadDirectory();
    }


    @Override
    public String toString() {
        return "UserSettings{" +
                "directory='" + directory + '\'' +
                ", preferredDownloadDirectory='" + preferredDownloadDirectory + '\'' +
                '}';
    }
}
