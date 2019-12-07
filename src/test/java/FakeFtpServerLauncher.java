import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;

/**
 *
 * @author Pravin Kumbhar
 *
 */
public class FakeFtpServerLauncher {
    
    /**
     *
     * @param args
     */
    public static void main(final String[] args) {

        FakeFtpServer fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("Admin", "pravin", "D:/WinFTP"));
        FileSystem fileSystem = new WindowsFakeFileSystem();
        fileSystem.add(new DirectoryEntry("D:/WinFTP/files"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(266);
        fakeFtpServer.start();

    }
    
}
