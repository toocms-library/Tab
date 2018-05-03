package com.toocms.frame.web.modle;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Author：Zero
 * Date：2017/3/9 15:19
 */
public class SimpleResponse implements Serializable {

    private static final long serialVersionUID = -9173796407893698790L;

    public String flag;
    public String message;
    public Object data;

    public TooCMSResponse toTooCMSResponse() {
        TooCMSResponse tooCMSResponse = new TooCMSResponse();
        tooCMSResponse.setFlag(flag);
        tooCMSResponse.setMessage(message);
        tooCMSResponse.setData(data);
        return tooCMSResponse;
    }
}
