package cn.lbzn.m;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.sdk.QbSdk;
import com.zhy.http.okhttp.OkHttpUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.lbzn.m.Utils.PrefUtils;
import okhttp3.OkHttpClient;

/**
 * Created by boming.liao on 2016/8/19.
 */
public class LbApplication extends Application {
    private static LbApplication application;
    private static String mSession;
    @Override
    public void onCreate() {
        super.onCreate();
        // 腾讯浏览器初始化
        QbSdk.allowThirdPartyAppDownload(true);
        // 初始化okhttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        application = this;
        mSession = PrefUtils.getString(this,"SESSION","NULL");
        Log.d("MyCookie-->4",mSession);
    }

    public String getSession() {
        if (TextUtils.isEmpty(mSession)){
            return "NULL";
        }
        Log.d("MyCookie-->5",mSession);
        return mSession;
    }

    public void setSession(String mSession) {
        if (TextUtils.isEmpty(mSession)) {
            return;
        }
        Log.d("MyCookie-->6",mSession);
        this.mSession = mSession;
    }

    public static LbApplication getInstance() {
        Log.d("MyCookie-->7",mSession);
        return application;
    }

    public void SaveSession() {
        PrefUtils.setString(this,"SESSION",mSession);
    }

    public void removeSession() {
        PrefUtils.setString(this,"SESSION","NULL");
        mSession = "NULL";
    }

    // 获取当前时间
    public String getCurrentDate(){
        SimpleDateFormat sDateFormat =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date curDate=new Date(System.currentTimeMillis());//获取当前时间
        String res= sDateFormat.format(curDate);
        return  res;
    }
}
