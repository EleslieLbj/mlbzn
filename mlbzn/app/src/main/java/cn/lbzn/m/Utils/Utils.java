package cn.lbzn.m.Utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import cn.lbzn.m.Constants.Constants;
import cn.lbzn.m.bean.Data;
import okhttp3.Call;

/**
 * Created by liaoboming on 2016/8/19.
 */
public class Utils {
    private static final String MY_COOKIE_STR= "my_cookie_str";
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    // 手势密码状态
    public static void getPwdStatus(String user_name, final Context context, final callback mCallback) {
        // 获取cookie
        String my_cookie = PrefUtils.getString(context, MY_COOKIE_STR, "");
        OkHttpUtils
                .post()
                .addHeader("cookie",my_cookie)
                .url(Constants.FIRST_URL2+"services/device.aspx?type=IsSetMemberDevice")
                .addParams("UserName", user_name)
                .addParams("IMEI",AppUtil.getDeviceId(context)+"")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                        mCallback.onError(true);
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        Log.d("test", "responseLogin:" + response);
                        Gson gson = new Gson();
                        Data data = gson.fromJson(response, Data.class);
                        String status = data.getRes();
                        mCallback.onResponse(status);
                    }
                });
    }


    /**
     * 回调接口
     */
    public interface callback{
        void onResponse(String status);
        void onError(boolean error_code);
    }
}
