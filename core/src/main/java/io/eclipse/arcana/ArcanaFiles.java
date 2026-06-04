package io.eclipse.arcana;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public final class ArcanaFiles {
    public static final String ASSETS_DIR_PROPERTY = "arcana.assets.dir";

    private ArcanaFiles() {}

    public static FileHandle asset(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (file.exists()) return file;

        file = Gdx.files.local(path);
        if (file.exists()) return file;

        String configuredAssetsDir = System.getProperty(ASSETS_DIR_PROPERTY);
        if (configuredAssetsDir != null && !configuredAssetsDir.isEmpty()) {
            file = Gdx.files.absolute(configuredAssetsDir).child(path);
            if (file.exists()) return file;
        }

        file = Gdx.files.local("assets/" + path);
        if (file.exists()) return file;

        file = Gdx.files.local("../assets/" + path);
        if (file.exists()) return file;

        return Gdx.files.local("../../assets/" + path);
    }
}
