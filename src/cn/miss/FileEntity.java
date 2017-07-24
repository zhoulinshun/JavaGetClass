package cn.miss;

/**
 * @Author MissNull
 * @Description:
 * @Date: Created in 2017/7/21.
 */
public class FileEntity {
    private String path;//相对路径
    private String name;//文件名，不包含后缀
    private String absolutePath;//所在目录
    private String allPath;//绝对路径

    public FileEntity(String path, String name,String absolutePath,String allPath) {
        this.path = path;
        this.name = name;
        this.absolutePath = absolutePath;
        this.allPath = allPath;
    }

    public String getAllPath() {
        return allPath;
    }

    public void setAllPath(String allPath) {
        this.allPath = allPath;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
