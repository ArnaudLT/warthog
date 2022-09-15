package org.arnaudlt.warthog.model.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class UserSettings {

    private String directory;


    public UserSettings(UserSettings user) {

        this.directory = user.directory;
    }
}
