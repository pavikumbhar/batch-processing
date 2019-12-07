import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySftpServer {
    
    public static void main(String[] args) throws IOException {
        start();
    }
    
    private static void start() throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2222);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser")));
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        sshd.setPasswordAuthenticator((username, password, session) -> username.equals("test") && password.equals("password"));
        sshd.start();
        log.info("SFTP server started");
    }
}