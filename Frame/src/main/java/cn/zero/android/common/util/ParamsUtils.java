package cn.zero.android.common.util;

import android.text.TextUtils;

import com.lzy.okgo.model.HttpParams;

import org.xutils.common.util.LogUtil;

import java.lang.reflect.Field;

/**
 * 网络请求参数工具类
 * <p>
 * Author：Zero
 * Date：2017/5/19 17:26
 *
 * @version v4.0.1
 */

public class ParamsUtils {

    public static HttpParams createHttpParams(Object object) {
        HttpParams params = new HttpParams();
        // 获取object对象对应类中的所有属性域
        Field[] fields = object.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                // 获取原来的访问控制权限
                boolean accessFlag = fields[i].isAccessible();
                // 修改访问控制权限
                fields[i].setAccessible(true);
                // 对于每个属性，获取属性名
                String fieldName = fields[i].getName();
                // 获取在对象object中属性fields[i]对应的对象中的变量
                String fieldValue = String.valueOf(fields[i].get(object));
                if (!TextUtils.equals(fieldValue, "null"))
                    params.put(fieldName, fieldValue);
                // 恢复访问控制权限
                fields[i].setAccessible(accessFlag);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return params;
    }
}
