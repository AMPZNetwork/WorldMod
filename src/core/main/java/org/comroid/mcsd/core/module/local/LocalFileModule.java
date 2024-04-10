package org.comroid.mcsd.core.module.local;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.comroid.api.io.FileHandle;
import org.comroid.mcsd.core.entity.module.local.LocalFileModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.FileModule;

import java.io.*;

@Log
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocalFileModule extends FileModule<LocalFileModulePrototype> {
    public LocalFileModule(Server server, LocalFileModulePrototype proto) {
        super(server, proto);
    }

    @Override
    public boolean mkDir(String path) {
        var prnt = new FileHandle(path).getParentFile();
        return prnt.exists() || prnt.mkdirs();
    }

    @Override
    public boolean exists(String path) {
        return new FileHandle(path).exists();
    }

    @Override
    public long size(String path) {
        return new File(path).length();
    }

    @Override
    @SneakyThrows
    public InputStream readFile(String path) {
        return new FileInputStream(path);
    }

    @Override
    @SneakyThrows
    public OutputStream writeFile(String path) {
        if (!mkDir(path))
            throw new IOException("Failed to create directory: " + path);
        return new FileOutputStream(path, false);
    }
}
