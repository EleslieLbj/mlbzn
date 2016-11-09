package cn.lbzn.m;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.cookie.Cookie;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.lbzn.m.Constants.Constants;
import cn.lbzn.m.Utils.PrefUtils;
import cn.lbzn.m.Utils.Utils;
import cn.lbzn.m.bean.Data;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;
import okhttp3.Call;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int MAIN_RESULT = 1;
    private static final int MAIN_RECODE1 = 2;
    private static final int MAIN_RECODE2 = 3;
    private boolean isFirstLaunch = true;
    public static final String USER_NAME = "user_name";
    public static final String HAND_ISCOLSE = "hand_iscolse";
    public static final String STATUS_MIMA = "status_mima";
    private static final String MY_COOKIE_STR= "my_cookie_str";
    private TextView tvShared;
    private WebView wv;
    private TextView tv_title;
    private TextView tv_lbzn;
    private String web_url;
    private RelativeLayout rtRoot;
    private String userAgentString;
    private String title;
    public String status_mima;
    private String deviceId;
    private String user_name;
    private Cookie cookie;

    private Handler mHandler = new Handler();
    // 装载url地址
    private List<String> mListUrlPager = new ArrayList<String>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private boolean isFirstLogin = true;
    private int login;
    // 应用进后台标识
    private boolean flag = false;
    // 后台回来进解锁界面
    private boolean flag2;
    private boolean isLocking;
    private String forget_pwd;
    private String other_account;
    private ProgressBar progressBar;

    private boolean isOnPause = false;



    private BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 获取从黑屏到亮屏的时间
            lastLoadTime = PrefUtils.getString(context, Constants.CURRENT_LOAD_TIME, 0 + "");
            if (action.equals("android.intent.action.SCREEN_ON")) {
                String statue_pwd = PrefUtils.getString(context, STATUS_MIMA, "");
                // 调起输入手势密码
                if ("1".equals(statue_pwd)) {// 判断表示设置过手势密码
                    if (System.currentTimeMillis() - Long.parseLong(lastLoadTime) >  2 * 60 *1000) {
                        PrefUtils.setBoolean(MainActivity.this,"service",true);
                        intent = new Intent(MainActivity.this, LockActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent,MAIN_RESULT);
                    }
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                // 当屏幕暗的时候获取当前时间
                PrefUtils.setString(context, Constants.CURRENT_LOAD_TIME, System.currentTimeMillis() + "");
                // 移除状态
                PrefUtils.setBoolean(MainActivity.this,"service",false);
            }
        }
    };


    private void registerBroadcastReceiver(){
        IntentFilter intent = new IntentFilter();
        intent.addAction("android.intent.action.SCREEN_ON");
        intent.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(mScreenOReceiver,intent);
    }

    private void unregisterBroadcastReceiver(){
        unregisterReceiver(mScreenOReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerBroadcastReceiver();
//        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
//            finish();
//            return;
//        }

        Log.e("test", "onCreate");
        PrefUtils.setBoolean(MainActivity.this,"oneGoOnCreate",true);
        // 去掉标题
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //        login = getIntent().getIntExtra("login", 0);
        forget_pwd = getIntent().getStringExtra("forget_pwd");
        other_account = getIntent().getStringExtra("other_account");
        // 初始化TextView
        tvShared = (TextView) findViewById(R.id.tv_shared);
        mListUrlPager.add(Constants.ACCOUNT_URL);
        mListUrlPager.add(Constants.FIRST_URL);
        mListUrlPager.add(Constants.FIRST_URL2);
        mListUrlPager.add(Constants.PRODUCE_URL);
        // 初始化标题的返回键
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_lbzn = (TextView) findViewById(R.id.tv_lbzn);
        // 跟布局
        rtRoot = (RelativeLayout) findViewById(R.id.rt_root);
        // 初始化进度条
        progressBar=(ProgressBar)findViewById(R.id.pb_progressBar);
        // 处理返回键的点击事件
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回跳转
                clickJump();
            }
        });
        // 获取电话管理器
        TelephonyManager systemService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // 获取imei
        deviceId = systemService.getDeviceId();
        Log.e("deviceId:", deviceId);//861054031166247
        // 获取用户名
        user_name = PrefUtils.getString(MainActivity.this, USER_NAME, null);
        // 获取手势密码登录的状态
        status_mima = PrefUtils.getString(MainActivity.this, STATUS_MIMA, null);
        // webView的一些设置
        mYwebSetting();
        // 加载url
        if(!TextUtils.isEmpty(forget_pwd)){
            if(forget_pwd.equals("forget_pwd")){
                // 清空用户名
                PrefUtils.remove(MainActivity.this,USER_NAME);
                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                wv.loadUrl(Constants.ENTRANCE_URL_LOGIN);
            }
        }else if(!TextUtils.isEmpty(other_account)){
             if (other_account.equals("other_account")){
                 // 清空用户名
                 PrefUtils.remove(MainActivity.this,USER_NAME);
                 CookieSyncManager.createInstance(this);
                 CookieManager cookieManager = CookieManager.getInstance();
                 cookieManager.removeAllCookie();
                wv.loadUrl(Constants.ENTRANCE_URL_LOGIN);
            }
        }else{
            // 获取Cookie
            final String my_cookie = PrefUtils.getString(MainActivity.this, MY_COOKIE_STR, "");
            Log.e("MyCookie-->9",my_cookie);
            Log.e("test", "onCreate");
            Log.d("MyCookie-->9",my_cookie);
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
//            android.webkit.CookieSyncManager.createInstance(this);
//            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.setCookie(Constants.FIRST_URL2, my_cookie);
            CookieSyncManager.getInstance().sync();
//            android.webkit.CookieSyncManager.getInstance().sync();
            // 获取手势密码
            String password = PrefUtils.getString(MainActivity.this, "Password",null);
            if(password!=null&&user_name!=null&&user_name!=""&&deviceId!=null&&deviceId!=""){
//                @boming.liao 11/02
//                wv.loadUrl(Constants.FIRST_URL2+"services/device.aspx?type=DeviceLoginAndroid"+"&UserName="+user_name+"&Password="+password+"&IMEI="+deviceId+"");
                wv.loadUrl(Constants.FIRST_URL2);
            }else{
                wv.loadUrl(Constants.FIRST_URL2);
            }
        }

        wv.addJavascriptInterface(new JsInterface(), "android");
        wv.setWebViewClient(new MyWebViewClient());
        wv.setWebChromeClient(new WebChromeClient());
        // 屏蔽掉长按时间
        wv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    private void hardwareAccelerate() {
        if (MainActivity.this.getPhoneSDKInt()>=14){
            getWindow().setFlags(0x1000000, 0x1000000);
        }
    }
    // 获取手机的SDK值
    public int getPhoneSDKInt() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }

    private void clickJump() {
        String str = web_url;
        if (str.equals(Constants.FIRST_URL2+"member/signin.aspx")) {
            JavaExcuteJs();
        } else if (str.contains(Constants.FIRST_URL2+"login.aspx")) {
            JavaExcuteJs();
        } else if (str.equals(Constants.FIRST_URL2+"member/reward/invite.aspx")) {
            JavaExcuteJs();
        } else if (str.equals(Constants.FIRST_URL2+"member/assets/prepaid.aspx")) {
            JavaExcuteJs();
        } else if(str.contains("https://yintong.com.cn")){
            wv.loadUrl(Constants.FIRST_URL2+"member/assets/prepaid.aspx");
        } else {
            wv.goBack();
        }
    }

    private void JavaExcuteJs() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                 wv.loadUrl("javascript:toRedirectUrl()");
//                wv.loadUrl(Constants.FIRST_URL);
//            }
//        }, 2000);
        wv.loadUrl(Constants.FIRST_URL);
    }

    private boolean isColse = true;
    private boolean isClick=false;
      private  String userName;
    // 客户端与h5进行交互的方法
    public class JsInterface {
        // 调起设置手势密码
        @JavascriptInterface
        public void jsPushGesture() {
            Log.e(TAG, "回调设置手势密码");
            // TODO: 2016/9/21
            // startActivity(new Intent(MainActivity.this, LockSetupActivity.class));
            if (!isLocking) {
                isLocking = true;
                Intent intent = new Intent(MainActivity.this, LockSetupActivity.class);
                startActivityForResult(intent, MAIN_RESULT);
            }
            //  finish();
        }

        // 登录时获取用户名
        @JavascriptInterface
        public void getUserName(String str) {
            userName=str;
            // 判断是否设置过手势密码的状态
//            initData(str);
//            initIsLoginAndSetMemberDevice(cookie2);
            // 保存用户名  @boming.liao 11/02
//            PrefUtils.setString(MainActivity.this, USER_NAME, str);
            boolean include_houtaiAndSplash = PrefUtils.getBoolean(MainActivity.this, "include_houtaiAndSplash", false);
            if(include_houtaiAndSplash){
                isClick=true;
                mClickButon mClickButon = new mClickButon();
                    mClickButon.setOnClickListener(new onClickListener() {
                    @Override
                    public void clickButon() {
                            Intent intent = new Intent(MainActivity.this,LockSetupActivity.class);
                            startActivity(intent);
                    }
                });
//                    Intent intent = new Intent(MainActivity.this,LockSetupActivity.class);
//                    startActivity(intent);
            }

        }

        // 保存用户名退出时获取用户名
        @JavascriptInterface
        public void exitUserName(String str) {
            Log.e("test", "保存用户名退出时获取用户名====" + str);
            // 如果退出了，把设置手势密码的状态置0
            initData("");
            //  如果退出登录就清空用户名
            PrefUtils.remove(MainActivity.this, USER_NAME);
            // 清空数据
            PrefUtils.remove(MainActivity.this,"include_houtaiAndSplash");
            PrefUtils.setBoolean(MainActivity.this, Constants.FORGER_PWD,false);
            PrefUtils.remove(MainActivity.this,MY_COOKIE_STR);
            LbApplication.getInstance().removeSession();
        }

        // 关闭手势密码
        @JavascriptInterface
        public void jsCloseGesture() {
            Log.e(TAG, "关闭手势密码");
            // 跳转到手势密码界面
            if (!isLocking) {
                isLocking = true;
                Intent intent = new Intent(MainActivity.this, LockActivity.class);
                intent.putExtra("close", 1);
                startActivityForResult(intent, MAIN_RESULT);
            }
        }

        // 分享
        @JavascriptInterface
        public void moreShare(String url) {
            showShare(url);
        }
    }

    private  Intent houtaiComing;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        houtaiComing=data;
        isLocking = false;

//        PrefUtils.setBoolean(MainActivity.this, Constants.FORGER_PWD,false);

        if (requestCode == MAIN_RESULT && resultCode == MAIN_RECODE1) {
            // 校验页面返回
            if (data != null) {
                int login = data.getIntExtra("login", 0);
                if (login == 1) {
                    PrefUtils.setBoolean(MainActivity.this, Constants.FORGER_PWD,false);
                    // 清空用户名
                    PrefUtils.remove(MainActivity.this,USER_NAME);
                    // TODO   是否在这里清除手势密码状态
                    // 清除缓存
                    CookieSyncManager.createInstance(this);
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                    // 跳转登录界面
                    wv.loadUrl(Constants.ENTRANCE_URL_LOGIN);
                }
            }
        } else if (requestCode == MAIN_RESULT && resultCode == MAIN_RECODE2) {
            PrefUtils.setBoolean(MainActivity.this, Constants.FORGER_PWD,false);
            // 设置界面返回
            if (data != null) {
                int login = data.getIntExtra("login", 0);
                if (login == 1) {
                    PrefUtils.setBoolean(MainActivity.this, Constants.FORGER_PWD,false);
                    // 清空用户名
                    PrefUtils.remove(MainActivity.this,USER_NAME);
                    // 清除缓存
                    CookieSyncManager.createInstance(this);
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                    // 跳转登录界面
                    wv.loadUrl(Constants.ENTRANCE_URL_LOGIN);
                }
            }
        }else if (requestCode == MAIN_RESULT && resultCode == 8){
//            wv.loadUrl(Constants.ACCOUNT_URL);
            wv.loadUrl(web_url);
        }

    }

    // 调用是否设置手势密码状态接口
    private void initData(String user_name) {
        OkHttpUtils
                .post()
                .url(Constants.FIRST_URL2+"services/device.aspx?type=IsSetMemberDevice")
                .addHeader("cookie",(cookie2==null) ? "":cookie2)
                .addParams("UserName", user_name)
                .addParams("IMEI", deviceId)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "test:" + response);
                        parseData(response);
                    }
                });
    }

    // 解析json数据
    private void parseData(String result) {
        Gson gson = new Gson();
        Data data = gson.fromJson(result, Data.class);
        String res = data.getRes();
        Log.d("fromJson", res);
        // 保存手势密码状态到首选项
        PrefUtils.setString(MainActivity.this, STATUS_MIMA, res);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e("test", "onStart()");
    }

    private String lastLoadTime;
    private boolean isFirstCreate;
    @Override
    protected void onResume() {
        super.onResume();

        final String user_name = PrefUtils.getString(MainActivity.this, USER_NAME, "");
        Log.e("test", "onResume(1111111111111)");
        if (user_name != null) {
            initData(user_name);
        }else{
            initData("");
        }

//        try {
//            if (isOnPause){
//                if (wv != null) {
//                    wv.getClass().getMethod("onResume").invoke(wv, (Object[]) null);
//                }
//                isOnPause=false;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        // 获取成功登陆密码状态
        String login_success = PrefUtils.getString(MainActivity.this, "login_success", "");
        // 获取手势密码
       final String password = PrefUtils.getString(MainActivity.this, "Password",null);
        String mima = PrefUtils.getString(MainActivity.this, STATUS_MIMA, "");
        final String mWebUrl=web_url;
        if ("1".equals(mima)) {
            if ("1".equals(login_success)&&isFirstCreate) {
                isFirstCreate=false;
                if(password!=null&&user_name!=null&&user_name!=""&&deviceId!=null&&deviceId!=""){
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            wv.loadUrl(Constants.FIRST_URL2+"services/device.aspx?type=DeviceLoginAndroid"+"&UserName="+user_name+"&Password="+password+"&IMEI="+deviceId+"&rel="+mWebUrl+"");
//                        }
//                    },1000);
//                    @boming.liao 11/02
//                    wv.loadUrl(Constants.FIRST_URL2+"services/device.aspx?type=DeviceLoginAndroid"+"&UserName="+user_name+"&Password="+password+"&IMEI="+deviceId+"&rel="+mWebUrl+"");
                }
            }
        }

//        boolean service = PrefUtils.Boolean(MainActivity.this, "service", false);
//        if (service){
//
//            CookieSyncManager.createInstance(this);
//            CookieManager cookieManager = CookieManager.getInstance();
//            cookieManager.removeAllCookie();
//            wv.loadUrl(Constants.ENTRANCE_URL_LOGIN);
//        }
        Log.e("test", "onResume()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 获取在后台的时间
        lastLoadTime = PrefUtils.getString(MainActivity.this, Constants.CURRENT_LOAD_TIME, 0 + "");
        PrefUtils.setBoolean(MainActivity.this,"oneGoOnCreate",false);
        boolean isCurrentRunningForeground = PrefUtils.getBoolean(MainActivity.this, "isCurrentRunningForeground", false);
        if (!isCurrentRunningForeground) {
            //  Toast.makeText(MainActivity.this, "从后台回来", Toast.LENGTH_SHORT).show();
            String user_name = PrefUtils.getString(MainActivity.this, USER_NAME, "");
            if (!TextUtils.isEmpty(user_name)) {
                Utils.getPwdStatus(user_name, MainActivity.this, new Utils.callback() {
                    // @boming.liao 2016/9.28
                    @Override
                    public void onError(boolean error_code) {
                    }
                    @Override
                    public void onResponse(String status) {
                        Log.e("test", "状态: " + status);
                            if ("1".equals(status)) {
                            if (System.currentTimeMillis() - Long.parseLong(lastLoadTime) > 2 * 60* 1000) {
                                Intent intent = new Intent(MainActivity.this, LockActivity.class);
                                startActivityForResult(intent,MAIN_RESULT);
                            }
                        }
                    }
                });
            }
        }
        Log.e("test", "onRestart()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnPause = true;
//        try {
//            if (wv!=null){
//                wv.getClass().getMethod("onPause").invoke(wv,(Object[]) null);
//                isOnPause = true;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        PrefUtils.setBoolean(MainActivity.this,"oneGoOnCreate",false);
        // 获取系统时间
        long time = System.currentTimeMillis();
        Log.e("test", "onPause()");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
        ShareSDK.stopSDK(this);
        // 清空数据
        PrefUtils.remove(MainActivity.this,"include_houtaiAndSplash");
        if (wv != null) {
            wv.getSettings().setBuiltInZoomControls(true);
            wv.setVisibility(View.GONE);
            long delayTime = ViewConfiguration.getZoomControlsTimeout();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
//                    wv.destroy();
                    wv = null;
                }
            }, delayTime);
        }
        isOnPause = false;
        // 停止服务
//        stopService(new Intent(MainActivity.this,ScreenService.class));
        // 清除webView的缓存
        // clearWebViewCache();
        LbApplication.getInstance().SaveSession();
        Log.e("test", "onDestroy()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (isFirstLaunch && keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && wv.canGoBack()) {
            if (mListUrlPager.contains(web_url)) {
                // 调用双击退出函数
                exitBy2Click();
            } else {
                // 执行返回按钮
                clickJump();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mListUrlPager.contains(web_url)) {
                // 调用双击退出函数
                exitBy2Click();
            }
        }
        return true;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            // 准备退出
            isExit = true;
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000);
        } else {
            finish();
//            System.exit(0); // 退出系统
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        isFirstCreate=true;
        // 记录一下当前的时间
        PrefUtils.setString(MainActivity.this, Constants.CURRENT_LOAD_TIME, System.currentTimeMillis() + "");
        PrefUtils.setBoolean(MainActivity.this,"oneGoOnCreate",false);
        try {
//            Thread.sleep(1000);
            new Thread() {
                public void run() {
                    boolean isCurrentRunningForeground = isRunningForeground();
                    PrefUtils.setBoolean(MainActivity.this, "isCurrentRunningForeground", isCurrentRunningForeground);
                }
            }.start();
        } catch (Exception e) {

        }
    }


    public boolean isRunningForeground() {
        String packageName = getPackageName(this);
        String topActivityClassName = getTopActivityName(this);
        System.out.println("packageName=" + packageName + ",topActivityClassName=" + topActivityClassName);
        if (packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName)) {
            System.out.println("---> isRunningForeGround");
            return true;
        } else {
            System.out.println("---> isRunningBackGround");
            return false;
        }
    }


    public String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager =
                (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        //  android.app.ActivityManager.getRunningTasks(int maxNum)
        //  maxNum--->The maximum number of entries to return in the list
        //  即最多取得的运行中的任务信息(RunningTaskInfo)数量
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();

        }
        //  按下Home键盘后 topActivityClassName=com.android.launcher2.Launcher
        return topActivityClassName;
    }

    public String getPackageName(Context context) {
        String packageName = context.getPackageName();
        return packageName;
    }
    // 分享
    private void showShare(String str) {

        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        oks.setTheme(OnekeyShareTheme.CLASSIC);
        //关闭sso授权
        // oks.disableSSOWhenAuthorize();
        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题微信和QQ空间使用
        oks.setTitle(getString(R.string.ssdk_oks_share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        // 分享地址
        oks.setTitleUrl(str);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("注册即送10000元，更有超值大礼等着您，快来体验吧!");
        oks.setImageUrl("http://img.lbzn.cn/app/lbzn_logo.png");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        // oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(str);
        // comment是我对这条分享的评论，仅QQ空间使用
        oks.setComment("我是评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(str);
        // 启动分享GUI
        oks.show(this);
    }
    private void mYwebSetting() {
        // 初始化webView
        wv = (WebView) findViewById(R.id.wv);
        WebSettings webSetting = wv.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setLoadWithOverviewMode(true);
//        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
//        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getDir("geolocation", 0)
                .getPath());
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 获取浏览器的类型
        userAgentString = webSetting.getUserAgentString();
    }

    private static final String APP_CACAHE_DIRNAME ="/webcache";
    // 清除缓存
    public void clearWebViewCache(){
        //清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File(getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME);
        Log.e(TAG, "appCacheDir path="+appCacheDir.getAbsolutePath());

        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");
        Log.e(TAG, "webviewCacheDir path="+webviewCacheDir.getAbsolutePath());

        //删除webview 缓存目录
        if(webviewCacheDir.exists()){
            deleteFile(webviewCacheDir);
        }
        //删除webview 缓存 缓存目录
        if(appCacheDir.exists()){
            deleteFile(appCacheDir);
        }
    }
    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile(File file) {

        Log.i(TAG, "delete file path=" + file.getAbsolutePath());

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e(TAG, "delete file no exists " + file.getAbsolutePath());
        }
    }

    private  String mClickUrl;
    protected  class MyWebViewClient extends WebViewClient{
        // 此方法表明点击网页里面的链接还是在当前的WebView里跳转,不跳到浏览器那边
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            Log.i("Main", url);
            mClickUrl=url;
            isFirstLaunch = false;
            Log.d(TAG, "onCreate_cookie_url:" + url);

            if ((Constants.FIRST_URL2+"login.aspx?o=lostuser&rel=%2fmember%2finvest%2finvesttqproject.aspx").equals(url)) {
                  return true;
            }

            // 隐藏和显示返回键
            if (!mListUrlPager.contains(url)) {
                tv_title.setVisibility(View.VISIBLE);
                return false;
            } else {
                tv_title.setVisibility(View.GONE);
                return false;
            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            // CookieManager管理器
            CookieManager cookieManager = CookieManager.getInstance();
            String my_cookie_str = cookieManager.getCookie(url);
            Log.d("MyCookie-->1","my_cookie_str"+my_cookie_str);
            LbApplication.getInstance().setSession(my_cookie_str);
            // 保存cookie
            if (my_cookie_str==null){
                PrefUtils.setString(MainActivity.this,MY_COOKIE_STR,"");
            }else{
                PrefUtils.setString(MainActivity.this,MY_COOKIE_STR,my_cookie_str);
            }
            Log.d("MyCookie-->2","my_cookie_str"+my_cookie_str);
            String lastLoadTime = PrefUtils.getString(MainActivity.this, Constants.CURRENT_LOAD_TIME, 0 + "");
            PrefUtils.setString(MainActivity.this, Constants.CURRENT_LOAD_TIME, System.currentTimeMillis() + "");
            if (System.currentTimeMillis() - Long.parseLong(lastLoadTime) > 2 *  1000) {
//                if (System.currentTimeMillis() - Long.parseLong(lastLoadTime) > 2 * 1000) {
                final String user_name=PrefUtils.getString(MainActivity.this,USER_NAME,"");
                // TODO 请求
                OkHttpUtils
                        .post()
                        .addHeader("cookie",LbApplication.getInstance().getSession())
                        .url(Constants.FIRST_URL2+"services/device.aspx?type=IsMemberLogining")
                        .addParams("UserName", user_name)
                        .build()
                        .execute(new StringCallback() {

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.d(TAG, "test:" + response);
                                Gson gson = new Gson();
                                Data data = gson.fromJson(response, Data.class);
                                Log.e("test",data.getRes());

                                CookieManager cookieManager = CookieManager.getInstance();
                                String cookie = cookieManager.getCookie(Constants.FIRST_URL2+"services/device.aspx");
                                LbApplication.getInstance().setSession(cookie);
                                // 如果超过了10分钟信息自动丢失了,现在开启登录手势密码进行登录
                                String pwd_status = PrefUtils.getString(MainActivity.this, STATUS_MIMA, "");
                                if ("1".equals(pwd_status)) { // 表示设置过手势密码
                                    if ("0".equals(data.getRes())) { // 表示用户信息丢失
                                        // 清理数据
                                        PrefUtils.remove(MainActivity.this,"include_houtaiAndSplash");
                                        // 开启手势密码登录
                                        Intent intent = new Intent(MainActivity.this, LockActivity.class);
                                        intent.putExtra("OtherPager","OtherPager");
                                        startActivityForResult(intent,MAIN_RESULT);
                                    }
                                }
                            }
                        });
            }
            if (System.currentTimeMillis() - Long.parseLong(lastLoadTime) > 3 * 1000) {
                // 开始加载的时候显示进度条
                progressBar.setVisibility(View.VISIBLE);
            }

        }

        // 该方法放在非UI线程中使用
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.e("should", "request.getUrl().toString() is " + request.getUrl().toString());
            return super.shouldInterceptRequest(view, request);
        }

        // 当网页加载失败的时候调用这个方法
        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {
            super.onReceivedError(webView, i, s, s1);
            Toast.makeText(MainActivity.this,"网络加载失败,请重试",Toast.LENGTH_SHORT).show();
            // 刷新
           // webView.reload();
            // 如果出错就弹出弹框
         // dialog(webView,i,s,s1);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            android.webkit.CookieSyncManager.createInstance(MainActivity.this);
            android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();
            cookie2 = cookieManager.getCookie(url);
            // 加载完成之后隐藏进度条
            progressBar.setVisibility(View.GONE);
            // 设置标题
            title = view.getTitle();
            tv_lbzn.setText(title);
            web_url = url;

            if (Constants.ACCOUNT_URL.equals(mClickUrl)&&isClick) {
                isClick=false;
                if(mListener!=null){
                    mListener.clickButon();
                }
            }
            if (url.equals(Constants.FIRST_URL2+"member/reward/invite.aspx") || url.equals(Constants.FIRST_URL2+"member/signin.aspx")) {
                wv.loadUrl("javascript:checkGobackShow()");
            } else if (url.equals(Constants.FIRST_URL2+"member/baseinfo/safeinfo.aspx#&pageHome")) {
                wv.loadUrl("javascript:showfigerPasw()");
            } else if (url.contains(Constants.FIRST_URL2+"member/assets/prepaid.aspx")) {
                wv.loadUrl("javascript:checkGobackShow()");
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    wv.loadUrl("javascript:getDeviceInfo(" + deviceId + ")");
                }
            }, 2000);

            if (mListUrlPager.contains(url)) {
                tv_title.setVisibility(View.GONE);
            } else {
                if (!isFirstLogin||url.equals(Constants.FIRST_URL2+"login.aspx")) {
                    tv_title.setVisibility(View.VISIBLE);
                }
                isFirstLogin = false;
            }
            // 清空时间
            PrefUtils.remove(MainActivity.this,Constants.CURRENT_LOAD_TIME);
            Log.d(TAG, "onPageFinished:" + url);
            String lastLoadTime = PrefUtils.getString(MainActivity.this, Constants.CURRENT_LOAD_TIME, 0 + "");
            PrefUtils.setString(MainActivity.this, Constants.CURRENT_LOAD_TIME, System.currentTimeMillis() + "");
            if(!TextUtils.isEmpty(cookie2)&&!TextUtils.isEmpty(userName)){
                initIsLoginAndSetMemberDevice(cookie2,userName);
            }

        }

    }

    private void initIsLoginAndSetMemberDevice(String cookie2,final String userName) {
        OkHttpUtils
                .post()
                .url(Constants.FIRST_URL2+"services/device.aspx?type=IsLoginAndSetMemberDevice")
                .addHeader("cookie",(cookie2==null) ? "":cookie2)
                .addParams("UserName", userName)
                .addParams("IMEI", deviceId)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "test:" + response);
                        try {
                            JSONObject obj=new JSONObject(response);
                            String isLogin=obj.optString("islogin");
                            String isSet=obj.optString("isset");
                            if ("1".equals(isLogin)) {
                                // 保存用户名
                                PrefUtils.setString(MainActivity.this, USER_NAME, userName);
                            }
                            if ("1".equals(isSet)) {
                                // 保存手势密码状态到首选项
                                PrefUtils.setString(MainActivity.this, STATUS_MIMA, isSet);
                            }
//                            else{
//                                // 保存手势密码状态到首选项
//                                PrefUtils.setString(MainActivity.this, STATUS_MIMA, "0");
//                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });

    }
    private   String cookie2;
    // 定义一个接口
    public interface  onClickListener{
        void clickButon();
    }
    onClickListener mListener;
    public class mClickButon {
        public void setOnClickListener(onClickListener listener){
        mListener=listener;
       }
    }

}
