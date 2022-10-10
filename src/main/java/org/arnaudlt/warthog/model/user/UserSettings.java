package org.arnaudlt.warthog.model.user;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public final class UserSettings {

    private String directory;

    private String preferredDownloadDirectory;

    private String preferredExportDirectory;


    public UserSettings(UserSettings user) {

        this.directory = user.directory;
        this.preferredDownloadDirectory = user.preferredDownloadDirectory;
        this.preferredExportDirectory = user.preferredExportDirectory;
    }


    public UserSettings(GlobalSettings.SerializableUserSettings user) {

        this.preferredDownloadDirectory = user.getPreferredDownloadDirectory();
        this.preferredExportDirectory = user.getPreferredExportDirectory();
    }

}
