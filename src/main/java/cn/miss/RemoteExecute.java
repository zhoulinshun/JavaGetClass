package cn.miss;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.jcraft.jsch.*;
import sun.net.ftp.FtpClient;
import sun.net.ftp.FtpProtocolException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/8/23.
 */
public class RemoteExecute {
    private static final long default_timeout = 1000 * 60;
    private static final int default_port = 22;
    private final String ip;
    private final String user;
    private final String pwd;
    private final long timeout;
    private final int port;
    private String shellPath;
    private Consumer<String> log;
    private Connection conn;
    private List<FileEntity> files;
    private String out;
    private FtpClient ftpConn;
    private Channel sftp;
    private com.jcraft.jsch.Session ftpSession;
    private Session session = null;


    public RemoteExecute(String ip, String user, String pwd, String shellPath, int port, long timeout) {
        this.ip = ip;
        this.user = user;
        this.pwd = pwd;
        this.shellPath = shellPath;
        this.port = port;
        this.timeout = timeout;
    }

    public RemoteExecute(String ip, String user, String pwd, String shellPath, int port) {
        this(ip, user, pwd, shellPath, port, default_timeout);
    }

    public RemoteExecute(String ip, String user, String pwd, String shellPath) {
        this(ip, user, pwd, shellPath, default_port);
    }

    public RemoteExecute(String ip, String user, String pwd, int port) {
        this(ip, user, pwd, null, port);
    }

    public void shellStart(Consumer<String> log) {
        this.log = log;
        cmdExc(shellPath);
    }

    public void sftpStart(Consumer<String> log) {
        this.log = log;
        ftpExc();
    }

    private boolean shellLogin() throws IOException {
        if (conn != null) return true;
        conn = new Connection(ip);
        conn.connect();
        return conn.authenticateWithPassword(user, pwd);
    }

    private boolean ftpLogin() throws IOException, FtpProtocolException {
        FtpClient f = FtpClient.create();
        f.setConnectTimeout((int) timeout);
        f.setReadTimeout((int) timeout);
        ftpConn = f.connect(new InetSocketAddress(ip, port));
        if (!ftpConn.isConnected()) return false;
        ftpConn.login(user, pwd.toCharArray());
        return ftpConn.isLoggedIn();
    }

    private boolean sftpLogin() throws JSchException {
        JSch jSch = new JSch();
        ftpSession = jSch.getSession(user, ip, port);
        ftpSession.setPassword(pwd);
        ftpSession.setConfig("StrictHostKeyChecking", "no");
        ftpSession.setTimeout((int) timeout);
        ftpSession.connect();
        if (!ftpSession.isConnected())
            return false;
        sftp = ftpSession.openChannel("sftp");
        sftp.connect();
        return sftp.isConnected();
    }

    private Session getShellSession() {
        try {
            if (shellLogin()) {
                session = conn.openSession();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.accept("远程连接失败:" + e.getMessage());
        }
        return session;
    }

    private void cmdExc(String cmd) {
        try {
            Session shellSession = getShellSession();
            shellSession.execCommand(cmd);
            StreamGobbler outGobbler = new StreamGobbler(session.getStdout());
            String out = read(outGobbler, Charset.defaultCharset());

            StreamGobbler errGobbler = new StreamGobbler(session.getStderr());
            String err = read(errGobbler, Charset.defaultCharset());

            session.waitForCondition(ChannelCondition.EXIT_STATUS, timeout);
            log.accept(out);
            log.accept(err);
            log.accept("远程退出状态：" + session.getExitStatus());
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.accept("远程连接失败，请检查网络设置：" + e.getMessage());
        }
    }

    public void ftpExc() {
        try {
            if (sftpLogin()) {
                StringBuffer sb = new StringBuffer();
                files.forEach(f -> {
                    String dir = getDir(f);
                    if (dir != null) sb.append("mkdir -p ").append(dir).append("\n");
                });
                cmdExc(sb.toString());
                files.forEach(this::fileUpload);
            }
            closeSFTP();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDir(FileEntity fileEntity) {
        ChannelSftp channelSftp = (ChannelSftp) sftp;
        String tranPath = fileEntity.getTranPath();
        String parent = new File(tranPath).getParent();
        String[] split = parent.split("\\\\");
        String join = String.join("/", split);
        try {
            channelSftp.cd(out + "/" + join);
            return null;
        } catch (SftpException e) {
            return out + "/" + join;
        }
    }

    private void fileUpload(FileEntity fileEntity) {
        try {
            ChannelSftp channelSftp = (ChannelSftp) sftp;
            String tranPath = fileEntity.getTranPath();
            String parent = new File(tranPath).getParent();
            String[] split = parent.split("\\\\");
            String join = String.join("/", split);
            channelSftp.put(fileEntity.getOutPath(), out + "/" + join + "/", ChannelSftp.OVERWRITE);
            log.accept(fileEntity.getOutPath() + "--上传成功！");
        } catch (SftpException e) {
            e.printStackTrace();
            log.accept("连接异常：" + e.getMessage()+"\n" + fileEntity.getOutPath()+"\n"+fileEntity.getTranPath());
        }
    }

    private String read(InputStream in, Charset charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }
        return sb.toString();
    }

    private void closeSFTP() {
        Optional.ofNullable(sftp).ifPresent(s -> {
            if (!s.isClosed()) s.disconnect();
        });
        Optional.ofNullable(ftpSession).ifPresent(com.jcraft.jsch.Session::disconnect);
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public RemoteExecute setFiles(List<FileEntity> files) {
        this.files = files;
        return this;
    }

    public String getShellPath() {
        return shellPath;
    }

    public RemoteExecute setShellPath(String shellPath) {
        this.shellPath = shellPath;
        return this;
    }

    public String getOut() {
        return out;
    }

    public RemoteExecute setOut(String out) {
        this.out = out;
        return this;
    }
}
