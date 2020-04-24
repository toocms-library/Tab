package com.toocms.tab.control.update;

/**
 * 类描述：版本信息实体类
 * 创建人：Zero
 * 创建时间：2017/2/15 12:47
 * 修改人：Zero
 * 修改时间：2017/3/13 17:02
 * 修改备注：
 */

public class TooCMSUpdateEntity {

    private String flag;
    private String message;
    private Data data;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        /**
         * "update_status":1		          0不更新，1非强制更新，2强制更新
         * "version_code": 20200416	    版本号
         * "version_name": "1.0.2"		    版本名
         * "description": "更改订单状态"	    更新记录
         * "url": "5e97fe911b78f.apk"	    下载地址
         * "apk_size":2048		              安装包大小
         * "apk_md5":"........"		           安装包的MD5，用于校验安装包完整性
         */

        private int update_status;
        private int version_code;
        private String version_name;
        private String description;
        private String url;
        private int apk_size;
        private String apk_md5;

        public int getUpdate_status() {
            return update_status;
        }

        public void setUpdate_status(int update_status) {
            this.update_status = update_status;
        }

        public int getVersion_code() {
            return version_code;
        }

        public void setVersion_code(int version_code) {
            this.version_code = version_code;
        }

        public String getVersion_name() {
            return version_name;
        }

        public void setVersion_name(String version_name) {
            this.version_name = version_name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getApk_size() {
            return apk_size;
        }

        public void setApk_size(int apk_size) {
            this.apk_size = apk_size;
        }

        public String getApk_md5() {
            return apk_md5;
        }

        public void setApk_md5(String apk_md5) {
            this.apk_md5 = apk_md5;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "update_status=" + update_status +
                    ", version_code=" + version_code +
                    ", version_name='" + version_name + '\'' +
                    ", description='" + description + '\'' +
                    ", url='" + url + '\'' +
                    ", apk_size=" + apk_size +
                    ", apk_md5='" + apk_md5 + '\'' +
                    '}';
        }
    }
}
