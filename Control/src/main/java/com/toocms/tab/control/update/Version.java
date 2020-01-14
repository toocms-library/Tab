package com.toocms.tab.control.update;

/**
 * 类描述：版本信息实体类
 * 创建人：Zero
 * 创建时间：2017/2/15 12:47
 * 修改人：Zero
 * 修改时间：2017/3/13 17:02
 * 修改备注：
 */

public class Version {

    private String version;
    private String url;
    private String description;
    private String is_force;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIs_force() {
        return is_force;
    }

    public void setIs_force(String is_force) {
        this.is_force = is_force;
    }
}
