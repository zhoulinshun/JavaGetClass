package cn.miss;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/7/21.
 */
public class FileUtils {

    private String srcPath;
    private String sourPath;
    private String srcSuffix;
    private String sourSuffix;
    private TextAppend append;
    private List<FileEntity> list;
    private boolean flag;

    public FileUtils(String srcPath, String sourPath, String srcSuffix, String sourSuffix) {
        this.srcPath = srcPath;
        this.sourPath = sourPath;
        this.srcSuffix = srcSuffix;
        this.sourSuffix = sourSuffix;
        list = new ArrayList<>();
    }

    public FileUtils(String srcPath, String sourPath) {
        this(srcPath, sourPath, ".java", ".class");
    }

    private FileUtils() {
    }

    public void start(TextAppend append) {
        this.append = append;
        //目录判断
        if (srcPath.isEmpty() || sourPath.isEmpty()) {
            append.append("输出文件夹或源文件夹不是个合法的目录");
            return;
        }
        File srcFile = new File(srcPath);
        File sourFile = new File(sourPath);
        //文件合法判断
        if (!srcFile.isDirectory()) {
            append.append("输入路径不合法");
            return;
        }
        if (!sourFile.isDirectory()) {
            append.append("输出路径不合法");
            return;
        }
        try {
            tranFile(SVNMark.getDiffFile(srcPath));
        } catch (IOException e) {
            e.printStackTrace();
            append.append("文件权限异常");
        }
    }

    //文件移动和拷贝
    private void tranFile(List<String> list) {
        list.forEach(this::foreach);
    }

    private void foreach(String s) {
        String filePath = null;
        try {
            String l = s.substring(0, s.indexOf("\\"));
            if (l.equals("webapp")) {
                filePath = sourPath + s.substring(6);
            } else {
                filePath = sourPath + "\\WEB-INF\\classes\\" + s.substring(l.length());
                if (s.endsWith(".java")) {
                    javaToClass(s.substring(l.length()));
                    return;
                }
            }
            FileOutputStream out = new FileOutputStream(getFile(filePath));
            Files.copy(new File(SVNMark.projectPath + "\\" + s).toPath(), out);
            out.close();
            append.append(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            append.append(filePath);
        }
    }

    //包路径加文件名
    private void javaToClass(String s) throws IOException {
        String classPath = srcPath + "\\target\\classes";
        String substring = s.substring(0, s.length() - 5) + ".class";
        //class文件所在文件夹
        String srcClass = classPath + "\\" + substring;
        //输出文件夹
        String sourClass = sourPath + "\\WEB-INF\\classes\\" + substring;
        FileOutputStream out = new FileOutputStream(getFile(sourClass));
        Files.copy(new File(srcClass).toPath(), out);
        out.close();
        append.append(sourClass);
    }

    private File getFile(String filePath) throws IOException {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        System.out.println("Ss");
        //创建所有父级目录
        if (!parentFile.exists()) parentFile.mkdirs();
        //创建文件
        if (!file.exists()) file.createNewFile();
        return file;
    }


    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getSourPath() {
        return sourPath;
    }

    public void setSourPath(String sourPath) {
        this.sourPath = sourPath;
    }

    public String getSrcSuffix() {
        return srcSuffix;
    }

    public void setSrcSuffix(String srcSuffix) {
        this.srcSuffix = srcSuffix;
    }

    public String getSourSuffix() {
        return sourSuffix;
    }

    public void setSourSuffix(String sourSuffix) {
        this.sourSuffix = sourSuffix;
    }

    public TextAppend getAppend() {
        return append;
    }

    public void setAppend(TextAppend append) {
        this.append = append;
    }

    public List<FileEntity> getList() {
        return list;
    }

    public void setList(List<FileEntity> list) {
        this.list = list;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
