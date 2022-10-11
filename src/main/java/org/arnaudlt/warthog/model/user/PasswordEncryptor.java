package org.arnaudlt.warthog.model.user;

import org.jasypt.util.StrongTextEncryptor;

public enum PasswordEncryptor {

    INSTANCE;

    public final StrongTextEncryptor encryptor = getEncryptor();


    private StrongTextEncryptor getEncryptor() {

        StrongTextEncryptor enc = new StrongTextEncryptor();
        enc.setPassword(")(-z<~?f40k+*+)b#L[)o<?iRL-v@0UQ");
        return enc;
    }
}
