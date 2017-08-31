package cn.miss.util;

import cn.miss.SVNMark;
import cn.miss.entity.FileEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @Author MissNull
 * @Description: 差异文件本地操作
 * @Date: Created in 2017/7/21.
 */
public class FileUtils {

    //默认输出文件夹
    private static final String defaultOutPath = "D:\\";
    //工程文件夹
    private String srcPath;
    //输出文件夹
    private String sourPath;
    //后缀 ---已弃用
    private String srcSuffix;
    //输出后缀 ---已弃用
    private String sourSuffix;
    //开关   ---已弃用
    private boolean flag;
    //日志回调接口
    private Consumer<String> log;
    //文件选择回调接口  包含选择完成之后的回调
    private BiConsumer<List<String>, Consumer<List<String>>> listConsumer;
    private String projectPath;
    private Consumer<List<FileEntity>> callable;
    private String dataFormat;

    public FileUtils(String srcPath, String sourPath, String srcSuffix, String sourSuffix) {
        this.srcPath = srcPath;
        this.sourPath = sourPath;
        this.srcSuffix = srcSuffix;
        this.sourSuffix = sourSuffix;
    }

    public FileUtils(String srcPath, String sourPath, String dataFormat) {
        this.srcPath = srcPath;
        this.sourPath = sourPath;
        this.dataFormat = dataFormat;
//        this(srcPath, sourPath, ".java", ".class");
    }

    public FileUtils() {
    }

    public void start(Consumer<String> consumer, BiConsumer<List<String>, Consumer<List<String>>> listConsumer, Consumer<List<FileEntity>> callable) {
        this.callable = callable;
        this.log = consumer;
        this.listConsumer = listConsumer;
        if (!fileAdjust()) return;
        try {
            projectPath = srcPath;
            if (!projectPath.endsWith("src\\\\main"))
                projectPath += "\\src\\main";
            tranFile(SVNMark.getDiffFile(projectPath));
        } catch (Exception e) {
            e.printStackTrace();
            log.accept("文件权限异常:" + e.getMessage());
        }
    }

    //判断目录是否合法
    private boolean fileAdjust() {
        //目录判断
        File srcFile = new File(srcPath);
        File sourFile = new File(sourPath);
        //文件合法判断
        if (!srcFile.isDirectory() || !srcFile.exists()) {
            log.accept("输入路径不合法");
            return false;
        }
        if (!sourFile.isDirectory() || !sourFile.exists()) {
            sourPath = defaultOutPath;
            log.accept("输出路径不合法");
            log.accept("已经启用默认路径：" + defaultOutPath);
        }
        //加上日期
        if (!Utils.isEmpty(dataFormat)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dataFormat);
            String format = formatter.format(LocalDateTime.now());
            sourPath += "\\" + format;
        }
        return true;
    }

    //文件移动和拷贝
    private void tranFile(List<String> list) {
        listConsumer.accept(list, l -> {
            List<FileEntity> files = new ArrayList<>();
            l.forEach(s -> {
                FileEntity en = makeFileEntity(s);
                files.add(en);
                fileCopy(en);
            });
            log.accept("文件保存成功");
            callable.accept(files);
        });
    }

    //将文件信息打包为文件实体
    private FileEntity makeFileEntity(String s) {
        String out;
        String tran;
        String src = projectPath + "\\" + s;
        String l = s.substring(0, s.indexOf("\\"));
        if (l.equals("webapp")) {
            tran = s.substring(6);
            out = sourPath + tran;
        } else {
            tran = "\\WEB-INF\\classes\\" + s.substring(l.length());
            out = sourPath + tran;
            if (s.endsWith(".java")) {
                String r = s.substring(l.length());
                String classPath = srcPath + "\\target\\classes";
                String substring = r.substring(0, r.length() - 5) + ".class";
                //class文件所在文件夹
                src = classPath + "\\" + substring;
                //输出文件夹
                tran = "\\WEB-INF\\classes\\" + substring;
                out = sourPath + tran;

            }
        }
        return new FileEntity(src, out, tran);
    }

    //文件拷贝
    private void fileCopy(FileEntity entity) {
        try (FileOutputStream out = new FileOutputStream(getFile(entity.getOutPath()))) {
            Files.copy(new File(entity.getSrcPath()).toPath(), out);
            log.accept(entity.getOutPath() + "保存成功");
        } catch (IOException e) {
            e.printStackTrace();
            log.accept(entity.getOutPath() + "保存失败，文件流异常");
        }
    }

    //构造输出的空文件
    private File getFile(String filePath) throws IOException {
        File file = new File(filePath);
        File parentFile = file.getParentFile();
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


    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
