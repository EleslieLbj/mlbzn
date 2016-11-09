package cn.lbzn.m;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class LoginActivity extends AppCompatActivity {
    private static final String  TAG ="LoginActivity";
    private TextView tvTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去掉标题
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        // 初始化webView
        initWebView();
        tvTitle = (TextView)findViewById(R.id.tv_title);
        // 设置点击事件
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果按下返回键的时候,就跳转到主页
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                // 然后关闭此页
                finish();
            }
        });

    }
    private void initData() {

        // 设置手势密码
//        OkHttpUtils
//                .post()
//                .url("http://192.168.1.148:89/services/device.aspx?type=SetMemberDevice")
//                .addParams("userName", "13552461393")
//                .addParams("Password", "12345679")
//                .addParams("IMEI","861054031166247")
//                .addParams("Device","12345679ASFASdfs")
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//
//                    }
//                    @Override
//                    public void onResponse(String response, int id) {
//                        Log.d(TAG, "responseSetting:" + response);
//                    }
//                });

        // 手势密码登录
//        OkHttpUtils
//                .post()
//                .url("http://192.168.1.148:89/services/device.aspx?type=DeviceLogin")
//                .addParams("UserName", "13552461393")
//                .addParams("Password", "12345679")
//                .addParams("IMEI","861054031166247")
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//
//                    }
//                    @Override
//                    public void onResponse(String response, int id) {
//                        Log.d(TAG, "responseLogin:" + response);
//                    }
//                });
        // 修改手势密码
//        OkHttpUtils
//                .post()
//                .url("http://192.168.1.148:89/services/device.aspx?type=ModifyMemberDevice")
//                .addParams("UserName", "13552461393")
//                .addParams("Password", "12345679")
//                .addParams("IMEI","861054031166247")
//                .build()
//                .execute(new StringCallback() {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//
//                    }
//                    @Override
//                    public void onResponse(String response, int id) {
//                        Log.d(TAG, "responseModify:" + response);
//                    }
//                });

    }
    private void initWebView() {
        WebView wvLogin=(WebView)findViewById(R.id.wv_login);
       // wvLogin.loadUrl("http://m.lbzn.cn/login.aspx");
        WebSettings webSetting = wvLogin.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 设置字体的放大缩小
        // settings.setBuiltInZoomControls(true);
        wvLogin.setWebViewClient(new WebViewClient(){
            // 此方法表明点击网页里面的链接还是在当前的WebView里跳转,不跳到浏览器那边
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                Log.d(TAG, "onCreate cookie:" + url);
                return true;
            }
        });
    }
}
