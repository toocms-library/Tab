package com.toocms.tab.control.update;

import com.toocms.tab.control.R;
import com.toocms.tab.control.update.entity.CheckVersionResult;
import com.toocms.tab.control.update.entity.UpdateEntity;
import com.toocms.tab.control.update.listener.IUpdateParseCallback;
import com.toocms.tab.control.update.proxy.IUpdateParser;
import com.toocms.tab.toolkit.AppManager;
import com.toocms.tab.toolkit.GSONUtils;
import com.toocms.tab.toolkit.x;

/**
 * App版本更新
 * <p>
 * Author：Zero
 * Date：2020/4/17 21:20
 *
 * @version v1.0
 */
public class UpdateManager {

    /**
     * 检查更新
     */
    public static final void checkUpdate() {
        XUpdate.newBuild(AppManager.getInstance().getTopActivity())
                .updateUrl(x.dataSet().getUrls().getUpdateUrl())
                .updateParser(new TooCMSUpdateParser())
                .supportBackgroundUpdate(true)
                .promptThemeColor(x.app().getResources().getColor(R.color.clr_main))
                .promptTopResId(R.drawable.xupdate_icon_app_rocket)
                .update();
    }

    /**
     * 自定义json解析器
     */
    static class TooCMSUpdateParser implements IUpdateParser {

        @Override
        public UpdateEntity parseJson(String json) throws Exception {
            TooCMSUpdateEntity result = GSONUtils.fromJson(json, TooCMSUpdateEntity.class);
            if (result != null) {
                UpdateEntity entity = new UpdateEntity();
                entity
                        .setHasUpdate(result.getData().getUpdate_status() != CheckVersionResult.NO_NEW_VERSION)
                        .setForce(result.getData().getUpdate_status() == CheckVersionResult.HAVE_NEW_VERSION_FORCED_UPLOAD)
                        .setVersionCode(result.getData().getVersion_code())
                        .setVersionName(result.getData().getVersion_name())
                        .setUpdateContent(result.getData().getDescription().replaceAll("\\\\r\\\\n", "\r\n"))
                        .setDownloadUrl(result.getData().getUrl())
                        .setMd5(result.getData().getApk_md5())
                        .setSize(result.getData().getApk_size());
                return entity;
            }
            return null;
        }

        @Override
        public void parseJson(String json, IUpdateParseCallback callback) throws Exception {
        }

        @Override
        public boolean isAsyncParser() {
            return false;
        }
    }
}
