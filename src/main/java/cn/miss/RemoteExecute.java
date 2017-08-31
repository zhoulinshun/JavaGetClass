package cn.miss;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import cn.miss.entity.FileEntity;
import cn.miss.exception.MyException;
import cn.miss.function.NoParamFunction;
import cn.miss.util.Utils;
import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/8/23.
 */
public class RemoteExecute {
    private static final long default_timeout = 1000 * 60;
    private static final int default_port = 22;
    private Consumer<String> log;
    private Connection conn;
    private List<FileEntity> files;
    private Channel sftp;

    private Session session = null;
    private List<Map<String, String>> ftpsConfig;
    private JSch jSch;
    private com.jcraft.jsch.Session currentFtpSession;
    private NoParamFunction allSuccessCallable;


    public RemoteExecute(List<Map<String, String>> ftpsConfig) {
        this.ftpsConfig = ftpsConfig;
    }

    public void shellStart(Consumer<String> log, String cmd) {
        this.log = log;
        shellExc(cmd, null);
    }

    //命令执行登陆
    private boolean shellLogin(String ip, String user, String pwd) throws IOException {
        if (conn != null) return true;
        conn = new Connection(ip);
        conn.connect();
        return conn.authenticateWithPassword(user, pwd);
    }

    //获取命令执行会话
    private Session getShellSession(Map<String, String> param) {
        try {
            if (shellLogin(param.get("ip"), param.get("user"), param.get("pwd"))) {
                session = conn.openSession();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.accept("远程连接失败:" + e.getMessage());
        }
        return session;
    }

    //远程执行命令  每一个会话只能执行一次命令
    private void shellExc(String cmd, Map<String, String> param) {
        try {
            Session shellSession = getShellSession(param);
            shellSession.execCommand(cmd);
            StreamGobbler outGobbler = new StreamGobbler(session.getStdout());
            String out = read(outGobbler, Charset.defaultCharset());

            StreamGobbler errGobbler = new StreamGobbler(session.getStderr());
            String err = read(errGobbler, Charset.defaultCharset());

            session.waitForCondition(ChannelCondition.EXIT_STATUS, Long.parseLong(param.getOrDefault("timeout", default_timeout + "")));
            log.accept("命令执行结果：");
            log.accept(out);
            log.accept(err);
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.accept("执行远程命令错误，此错误很有可能影响文件正常上传,请根据错误信息自行判断:" + e.getMessage());
        } catch (MyException e) {
            log.accept(e.getMessage());
        }
    }


    public void sftpStart(Consumer<String> log, Supplier<String> tempGetShellPath) {
        this.log = log;
        ftpExc(log, tempGetShellPath);
    }

    private boolean sftpLogin(String ip, String user, String pwd, int port, long timeout) throws JSchException {
        currentFtpSession = jSch.getSession(user, ip, port);
        currentFtpSession.setPassword(pwd);
        currentFtpSession.setConfig("StrictHostKeyChecking", "no");
        currentFtpSession.setTimeout((int) timeout);
        currentFtpSession.connect();
        if (!currentFtpSession.isConnected())
            return false;
        sftp = currentFtpSession.openChannel("sftp");
        sftp.connect();
        return sftp.isConnected();
    }

    //ftp上传文件
    public void ftpExc(Consumer<String> log, Supplier<String> tempGetShellPath) {
        jSch = new JSch();
        new Thread(() -> {
            //开始上传文件
            int[] index = {0};
            int[] ftpItem = {0};
            int[] shellItem = {0};
            int size = ftpsConfig.size();
            ftpsConfig.forEach(param -> {
                log.accept("\n\n当前正在上传第" + (++index[0]) + "个,还有" + (size - index[0]) + "个");
                try {
                    if (sftpLogin(param.get("ip"), param.get("user"),
                            param.get("pwd"), Integer.parseInt(param.getOrDefault("port", "22")),
                            Integer.valueOf(param.getOrDefault("timeout", default_timeout + "")))) {
                        StringBuffer sb = new StringBuffer();
                        files.forEach(f -> {
                            String dir = getDir(f, param.get("ftpOut"));
                            if (dir != null) sb.append("mkdir -p ").append(dir).append("\n");
                        });
                        shellExc(sb.toString(), param);
                        final int[] ind = {0};
                        files.forEach(fileEntity -> {
                            if (fileUpload(fileEntity, param.get("ftpOut")))
                                ind[0]++;
                        });
                        ftpItem[0]++;
                        log.accept(param.get("ip") + "---" + "、上传完成，成功上传" + ind[0] +
                                "个文件，共" + files.size() + "个文件");
                        closeCurrentSession();
                        String shellPath = param.get("shellPath");
                        if (Utils.isEmpty(shellPath)) {
                            shellPath = tempGetShellPath.get();
                            if (Utils.isEmpty(shellPath)) {
                                log.accept("本次没有执行脚本");
                                return;
                            }
                        }
                        shellExc(shellPath, param);
                        shellItem[0]++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.accept("连接失败：" + e.getMessage());
                }
            });
            closeSFTP();
            log.accept("\n\n所有连接均已上传完毕，成功上传" + ftpItem[0] + "台服务器，失败了" + (size - ftpItem[0]) + "台");
            log.accept("执行了" + shellItem[0] + "台服务器脚本，有" + (size - ftpItem[0]) + "台服务器脚本未执行");
            allSuccessCallable.accept();
        }).start();

    }

    //获取linux下文件夹
    private String getDir(FileEntity fileEntity, String out) {
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

    //文件上传
    private boolean fileUpload(FileEntity fileEntity, String out) {
        try {
            ChannelSftp channelSftp = (ChannelSftp) sftp;
            String tranPath = fileEntity.getTranPath();
            String parent = new File(tranPath).getParent();
            String[] split = parent.split("\\\\");
            String join = String.join("/", split);
            channelSftp.put(fileEntity.getOutPath(), out + "/" + join + "/", ChannelSftp.OVERWRITE);
            log.accept(fileEntity.getOutPath() + "--成功上传到：" + out + "/" + join + "/");
            return true;
        } catch (SftpException e) {
            e.printStackTrace();
            log.accept("文件上传异常：" + e.getMessage() + "\n" + fileEntity.getOutPath() + "\n" + fileEntity.getTranPath());
            return false;
        }
    }

    private String read(InputStream in, Charset charset) throws MyException {
        try {
            byte[] buf = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while (in.read(buf) != -1) {
                sb.append(new String(buf, charset));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new MyException("读取远程命令执行结果出错，此错误不影响文件上传！");
        }
    }

    //关闭整个连接
    private void closeSFTP() {
        Optional.ofNullable(sftp).ifPresent(s -> {
            if (!s.isClosed()) s.disconnect();
        });
    }

    //关闭当前会话
    private void closeCurrentSession() {
        Optional.ofNullable(currentFtpSession).ifPresent(com.jcraft.jsch.Session::disconnect);
    }

    public List<FileEntity> getFiles() {
        return files;
    }

    public RemoteExecute setFiles(List<FileEntity> files) {
        this.files = files;
        return this;
    }

    public NoParamFunction getAllSuccessCallable() {
        return allSuccessCallable;
    }

    public RemoteExecute setAllSuccessCallable(NoParamFunction allSuccessCallable) {
        this.allSuccessCallable = allSuccessCallable;
        return this;
    }
}
