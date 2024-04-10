package org.comroid.mcsd.core.module;

import lombok.SneakyThrows;
import org.comroid.api.data.seri.adp.JSON;
import org.comroid.api.io.FileHandle;
import org.comroid.api.net.MD5;
import org.comroid.mcsd.core.entity.module.FileModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;

public abstract class FileModule<T extends FileModulePrototype> extends ServerModule<T> {
    public FileModule(Server server, T proto) {
        super(server, proto);
    }

    public abstract boolean mkDir(String path);

    public abstract boolean exists(String path);

    public abstract long size(String path);

    public abstract InputStream readFile(String path);

    public abstract OutputStream writeFile(String path);

    @SneakyThrows
    public boolean isJarUpToDate() {
        if (Objects.requireNonNullElse(proto.getForceCustomJar(), FileModulePrototype.DefaultForceCustomJar))
            return true;
        var serverJar = new FileHandle(server.path("server.jar").toFile());
        if (!serverJar.exists())
            return false;
        try (var source = new JSON.Deserializer(new URL(server.getJarInfoUrl()).openStream());
             var local = readFile(serverJar.getAbsolutePath())) {
            var sourceMd5 = source.readObject().get("response").get("md5").asString("");
            var localMd5 = MD5.calculate(local);
            return sourceMd5.equals(localMd5);
        }
    }
}
