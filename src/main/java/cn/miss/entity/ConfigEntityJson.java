package cn.miss.entity;

import cn.miss.util.Utils;

import java.util.Map;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/8/30.
 */
public class ConfigEntityJson {
    private Map<String, Object> map;
    private Map<String, Object> defaultMap;

    public ConfigEntityJson() {
//        map = Utils.readJsonProFile(Utils.getJarLocation());
//        defaultMap = Utils.readJsonProFile(getClass().getResource("/").getPath());
    }


    public String get(String key) {
        return get(key, defaultMap.get(key));
    }

    public String get(String key, Object defaultValue) {
        return (String) map.getOrDefault(key, defaultValue);
    }

    public <T> T get(String key, Class<T> tClass) {
        return get(key, tClass, defaultMap.get(key));
    }

    public <T> T get(String key, Class<T> tClass, Object defaultValue) {
        return (T) map.getOrDefault(key, defaultValue);
    }

    public void save() {
        Utils.saveJsonProFile(map, defaultMap);
    }

}
