package cn.miss.entity;

/**
 * @Author MissNull
 * @Description: 文件实体
 * @Date: Created in 2017/7/21.
 */
public class FileEntity {
    private String srcPath;//源文件路径
    private String outPath;//输出路径
    private String tranPath;//相对路径

    public FileEntity() {
    }

    public FileEntity(String srcPath, String outPath,String tranPath) {
        this.srcPath = srcPath;
        this.outPath = outPath;
        this.tranPath = tranPath;
    }

    public String getTranPath() {
        return tranPath;
    }

    public void setTranPath(String tranPath) {
        this.tranPath = tranPath;
    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }
}
