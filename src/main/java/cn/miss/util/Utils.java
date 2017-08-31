package cn.miss.util;

import cn.miss.MyForm;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/8/29.
 */
public class Utils {

    private static String jarLocation = null;

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    //获取当前jar运行目录
    public static String getJarLocation() {
        String filePath;
        URL url = MyForm.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            filePath = URLDecoder.decode(url.getPath(), "utf-8");// 转化为utf-8编码，支持中文
            if (filePath.endsWith(".jar")) {// 可执行jar包运行的结果里包含".jar"
                // 获取jar包所在目录
                File parentFile = new File(filePath).getParentFile();
                return parentFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T strCast(Object value, Class<T> tClass) {
        return strCast(value, tClass, null);
    }

    //字符串转换
    public static <T> T strCast(Object value, Class<T> tClass, T defaultValue) {
        try {
            if (value == null)
                return defaultValue;
            return (T) value;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Method valueOf = tClass.getDeclaredMethod("valueOf", value.getClass());
                return (T) valueOf.invoke(null, value);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return defaultValue;
    }


    public static HashMap readJsonProFile(String path) {
        Gson gson = new Gson();
        HashMap<String, String> hashMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path, "default.json"))));
            hashMap = gson.fromJson(reader, HashMap.class);
            reader.close();
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return hashMap;
    }

    //读取配置文件
    public static HashMap readProFile(String path) {
        LinkedHashMap map = new LinkedHashMap<String, String>();
        File file = new File(path, "default.properties");
        Properties properties = new Properties();
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
                properties.forEach(map::put);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private static void write(BufferedWriter writer, String s) {
        try {
            writer.write(s);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //保存配置文件
    public static void saveProFile(Map map, Map defa) {
        jarLocation = jarLocation == null ? getJarLocation() : jarLocation;
        File file = new File(jarLocation, "default.properties");
        try {
            if (!file.exists())
                file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
            Properties properties = new Properties();
            map.forEach((k, v) -> {
                if (!isEmpty(v.toString()))
                    properties.put(k, v);
            });
            defa.forEach((k, v) -> {
                if (!map.containsKey(k) && !isEmpty(v.toString()))
                    properties.put(k, v);
            });
            properties.store(writer, "配置文件");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveJsonProFile(Map map, Map defa) {
        HashMap<Object, Object> hashMap = new HashMap(map);
        defa.forEach((k, v) -> {
            if (!map.containsKey(k))
                hashMap.put(k, v);
        });
        String s = new Gson().toJson(hashMap);
        File file = new File(getJarLocation(), "default.json");
        try {
            if (!file.exists())
                file.createNewFile();
            Files.write(file.toPath(), s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
