package cn.miss.entity;

import cn.miss.util.Utils;

import java.io.File;
import java.util.Map;

/**
 * @Author MissNull
 * @Description: default.properties版本
 * @Date: Created in 2017/8/30.
 */
public class ConfigEntity {
    private final String type;
    private Map<String, Object> defaultMap;
    private Map<String, Object> map;


    public ConfigEntity(String type) {
        this.type = type;
        if (type.equals("json")) {
            map = Utils.readJsonProFile(Utils.getJarLocation());
            defaultMap = Utils.readJsonProFile("/");
        } else {
            map = Utils.readProFile(Utils.getJarLocation());
            defaultMap = Utils.readProFile(getClass().getResource("/").getPath());
        }
    }

    public String get(String key) {
        return get(key, defaultMap.get(key));
    }

    public String get(String key, Object defaultValue) {
        return (String) map.getOrDefault(key, defaultValue);
    }

    public <T> T get(String key, Class<T> tClass) {
        return get(key, tClass, Utils.strCast(defaultMap.get(key), tClass));
    }

    public <T> T get(String key, Class<T> tClass, T defaultValue) {
        return Utils.strCast(map.get(key), tClass, defaultValue);
    }

    public void put(String key, Object o) {
        map.put(key, o);
    }

    public void save() {
        if (type.equals("json")) {
            Utils.saveJsonProFile(map, defaultMap);
        } else {
            Utils.saveProFile(map, defaultMap);
        }

    }

}
