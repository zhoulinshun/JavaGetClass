package cn.miss;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author MissNull
 * @Description: 调用svn命令
 * @Date: Created in 2017/7/21.
 */
public class SVNMark {

    private final static String cmd = "cmd /k svn status";
    public static String path;

    //获取差异文件
    public static List<String> getDiffFile(String projectPath) throws Exception {
        SVNMark.path = projectPath;
        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec(cmd,
                null, new File(SVNMark.path));
        InputStream in = exec.getInputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        List<String> list = new ArrayList<>();
        String len;
        //此处必须要加判空，否则会无限循环
        while ((len = r.readLine()) != null && !len.isEmpty()) {
            String s;
            if ((s = nameAnalysis(len)) != null) {
                list.add(s);
            }
        }
        r.close();
        in.close();
        exec.destroy();
        return list;
    }


    private static String nameAnalysis(String s) {
        //cmd结果除开头标识外会有7个空格
        String substring = s.substring(8);
        //文件夹忽略
        if (!new File(path + "\\" + substring).isDirectory())
            return substring;
        return null;
    }
}

