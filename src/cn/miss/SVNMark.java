package cn.miss;

import java.io.*;
import java.util.*;

/**
 * @Author MissNull
 * @Description: 调用svn命令aa
 * @Date: Created in 2017/7/21.
 */
public class SVNMark {

    public static String projectPath;
    private final static String cmd = "cmd /k svn status";

    //获取差异文件
    public static List<String> getDiffFile(String projectPath) throws IOException {
        SVNMark.projectPath = projectPath;
        if (!projectPath.endsWith("src\\main"))
            SVNMark.projectPath += "\\src\\main";
        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(cmd,
                null, new File(SVNMark.projectPath));
        InputStream in = exec.getInputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        List<String> list = new ArrayList<>();
        String len;
        //此处必须要加判空，否则会无限循环
        while ((len = r.readLine()) != null && !len.isEmpty())
            Optional.ofNullable(nameAnalysis(len)).ifPresent(list::add);
        r.close();
        in.close();
        exec.destroy();
        return list;
    }


    private static String nameAnalysis(String s) {
        //cmd结果除开头标识外会有7个空格
        String substring = s.substring(8);
        //空文件夹忽略
        if (!new File(substring).isDirectory())
            return substring;
        return null;
    }
}

