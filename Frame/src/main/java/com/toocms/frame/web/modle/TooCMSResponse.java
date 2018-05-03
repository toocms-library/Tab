package com.toocms.frame.web.modle;

import java.io.Serializable;

/**
 * 类描述：晟轩科技响应BEAN类
 * 创建人：Zero
 * 创建时间：2017/2/15 11:58
 * 修改人：Zero
 * 修改时间：2017/3/9 15:25
 * 修改备注：
 */

public class TooCMSResponse<T> implements Serializable {

    private static final long serialVersionUID = -3470353247357331047L;

    /**
     * 标识
     */
    private String flag;

    /**
     * 信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
