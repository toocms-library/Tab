package com.toocms.tab.crash;

import android.text.TextUtils;
import android.widget.Toast;

import com.lzy.okgo.model.HttpParams;
import com.toocms.tab.network.ApiListener;
import com.toocms.tab.network.ApiTool;
import com.toocms.tab.network.modle.TooCMSResponse;
import com.toocms.tab.toolkit.AppManager;
import com.toocms.tab.toolkit.NetWorkUtils;
import com.toocms.tab.toolkit.x;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 验证使用框架的项目
 *
 * @author Zero
 * @date 2016/4/27 11:35
 */
public class VerificationService {

    private static VerificationService instance = null;

    private String verification_state = null;
    private String prompt_language = null;

    public static VerificationService getInstance() {
        if (instance == null)
            synchronized (VerificationService.class) {
                if (instance == null)
                    instance = new VerificationService();
            }
        return instance;
    }

    private VerificationService() {
    }

    public final void verification() {
        if (!NetWorkUtils.isNetConnected(x.app())) return;
        if (TextUtils.isEmpty(verification_state)) {
            HttpParams params = new HttpParams();
            params.put("pack", x.app().getPackageName().replace(".", "_"));
            params.put("type", "1");
            new ApiTool<TooCMSResponse<Void>>().postApi(x.app(), "http://twp.toocms.com/PaCheck/Docheck",
                    params, new ApiListener<TooCMSResponse<Void>>() {
                        @Override
                        public void onComplete(TooCMSResponse<Void> data, Call call, Response response) {
                            verification_state = "\u9a8c\u8bc1\u901a\u8fc7";
                        }

                        @Override
                        public void onError(String error, Call call, Response response) {
                            verification_state = "\u9a8c\u8bc1\u672a\u901a\u8fc7";
                            prompt_language = error;
                            showPromptLanguageAndExitApp();
                        }
                    });
        } else {
            verificationState();
        }
    }

    private final void verificationState() {
        if (TextUtils.equals(verification_state, "\u9a8c\u8bc1\u672a\u901a\u8fc7"))
            showPromptLanguageAndExitApp();
    }

    private final void showPromptLanguageAndExitApp() {
        Toast.makeText(x.app(), prompt_language, Toast.LENGTH_LONG).show();
        AppManager.getInstance().AppExit(x.app());
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
