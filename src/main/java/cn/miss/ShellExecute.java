package cn.miss;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/8/23.
 */
public class ShellExecute {
    private static final long default_timeout = 1000 * 60;
    private final String ip;
    private final String user;
    private final String pwd;
    private final String shellPath;
    private final long timeout;
    private Consumer<String> log;
    private Connection conn;
    private List<FileEntity> files;
    private String out;


    public ShellExecute(String ip, String user, String pwd, String shellPath, long timeout) {
        this.ip = ip;
        this.user = user;
        this.pwd = pwd;
        this.shellPath = shellPath;
        this.timeout = timeout;
    }

    public ShellExecute(String ip, String user, String pwd, String shellPath) {
        this(ip, user, pwd, shellPath, default_timeout);
    }

    public void start(Consumer<String> log) {
        this.log = log;
        exc();
    }

    private boolean login() throws IOException {
        conn = new Connection(ip);
        conn.connect();
        return conn.authenticateWithPassword(user, pwd);
    }

    private void exc() {
        try {
            if (login()) {
                Session session = conn.openSession();
                session.execCommand(shellPath);

                StreamGobbler outGobbler = new StreamGobbler(session.getStdout());
                String out = read(outGobbler, Charset.defaultCharset());

                StreamGobbler errGobbler = new StreamGobbler(session.getStderr());
                String err = read(errGobbler, Charset.defaultCharset());

                session.waitForCondition(ChannelCondition.EXIT_STATUS, timeout);

                log.accept(out);
                log.accept(err);
                log.accept("远程退出状态：" + session.getExitStatus());
            } else {
                log.accept("远程连接失败，用户名密码");
            }
        } catch (Exception e) {
            log.accept("远程连接失败，请检查网络设置：" + e.getMessage());
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


    public List<FileEntity> getFiles() {
        return files;
    }

    public ShellExecute setFiles(List<FileEntity> files) {
        this.files = files;
        return this;
    }

    public String getOut() {
        return out;
    }

    public ShellExecute setOut(String out) {
        this.out = out;
        return this;
    }
}
