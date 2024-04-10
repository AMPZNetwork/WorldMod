package org.comroid.mcsd.core.module.remote.ssh;

import lombok.SneakyThrows;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.comroid.api.info.Log;
import org.comroid.mcsd.core.entity.module.remote.ssh.SshFileModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.FileModule;
import org.comroid.mcsd.core.util.ApplicationContextProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class SshFileModule extends FileModule<SshFileModulePrototype> {
    private ClientSession session;
    private SftpClient sftp;

    public SshFileModule(Server server, SshFileModulePrototype proto) {
        super(server, proto);
    }

    @Override
    @SneakyThrows
    protected void $initialize() {
        var sh= proto.getShConnection();
        session = ApplicationContextProvider.bean(SshClient.class)
                .connect(sh.getUsername(),sh.getHost(),sh.getPort())
                .verify()
                .getSession();
        session.addPasswordIdentity(sh.getPassword());
        session.auth().verify();
        sftp = SftpClientFactory.instance().createSftpClient(session);
    }

    @Override
    public boolean mkDir(String path) {
        try {
            path = server.path(path).toString();
            sftp.mkdir(path);
            return exists(path);
        } catch (IOException e) {
            Log.at(Level.WARNING, "Failed to create directory "+path, e);
            return false;
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            path = server.path(path).toString();
            var stat = sftp.stat(path);
            return stat.isDirectory() || stat.isRegularFile();
        } catch (IOException e) {
            Log.at(Level.WARNING, "Failed to stat path "+path, e);
            return false;
        }
    }

    @Override
    public long size(String path) {
        try {
            path = server.path(path).toString();
            var stat = sftp.stat(path);
            return stat.getSize();
        } catch (IOException e) {
            Log.at(Level.WARNING, "Failed to stat path "+path, e);
            return -1;
        }
    }

    @Override
    @SneakyThrows
    public InputStream readFile(String path) {
        path= server.path(path).toString();
        return sftp.read(path);
    }

    @Override
    @SneakyThrows
    public OutputStream writeFile(String path) {
        path= server.path(path).toString();
        return sftp.write(path, SftpClient.OpenMode.Truncate);
    }
}
