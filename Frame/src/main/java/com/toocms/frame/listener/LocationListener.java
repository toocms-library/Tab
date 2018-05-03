package com.toocms.frame.listener;

import com.baidu.location.BDLocation;

import java.util.Map;

/**
 * 定位回调监听
 *
 * @author Zero
 *         <p>
 *         2015年6月9日
 */
public interface LocationListener {

    /**
     * 当定位成功之后的回调方法
     *
     * @param arg0
     */
    void onReceiveLocation(Map<String, String> location);

}
