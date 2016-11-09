package cn.lbzn.m;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tencent.smtt.sdk.CookieManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.List;

import cn.lbzn.m.Constants.Constants;
import cn.lbzn.m.Utils.PrefUtils;
import cn.lbzn.m.Utils.ToastUtil;
import cn.lbzn.m.View.LockPatternView;
import cn.lbzn.m.bean.Data;
import okhttp3.Call;

public class LockActivity extends BaseActivity implements LockPatternView.OnPatternListener, View.OnClickListener {

    private static final String TAG = "LockActivity";
    private static final int MAIN_RESULT = 1;
    private static final int MAIN_RECODE1 = 2;
    private static final int MAIN_RECODE2 = 3;
    private List<LockPatternView.Cell> lockPattern;
    private LockPatternView lockPatternView;
    private TextView tvVerify;
    private TextView tvForgetPsw;
    private TextView tvAccount;
    private TextView tvNumber;
    private String user_name;
    private  String deviceId;
    private  String res;
    private  String patternToString2;
    public static final String USER_NAME = "user_name";
    public static final String HAND_ISCOLSE="hand_iscolse";
    public static final String STATUS_MIMA = "status_mima";
    private static final String MY_COOKIE_STR= "my_cookie_str";
    private boolean aBoolean;
    private String status_mima;
    private Handler mHandler =new Handler();
    // 统计绘制的次数
    private int count;
    private int close;
    private String in_splash;
    private String onrestart;
    private String OtherPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        PrefUtils.setBoolean(LockActivity.this,Constants.FORGER_PWD,false);

        SharedPreferences preferences = getSharedPreferences(SplashActivity.LOCK,MODE_PRIVATE);
        String patternString = preferences.getString(SplashActivity.LOCK_KEY,null);

        Intent intent = getIntent();
        close = intent.getIntExtra("close", 0);
        in_splash = intent.getStringExtra("in_splash");
        onrestart = intent.getStringExtra("onrestart");
        OtherPager=intent.getStringExtra("OtherPager");
//        if (patternString == null) {
//            finish();
//            return;
//        }

        // 获取关闭时的状态
         aBoolean = PrefUtils.getBoolean(LockActivity.this, HAND_ISCOLSE, false);
        // 获取手势密码登录的状态
//         status_mima = PrefUtils.getString(LockActivity.this, STATUS_MIMA, null);

        if(patternString!=null){
            lockPattern = LockPatternView.stringToPattern(patternString);
        }

        // @boming.liao
//        patternToString2=LockPatternView.patternToString2(lockPattern);
        Log.d(TAG,"patternToString2:"+patternToString2);
        // 获取电话管理器
        TelephonyManager systemService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // 获取imei
        deviceId = systemService.getDeviceId();
        // @boming.liao 打印首选项里面的内容
        Log.d(TAG,"lockPatternTAg"+lockPattern);
       // Log.d(TAG,"lockPatternString"+lockPatternString);
        setContentView(R.layout.activity_lock);
        // 初始化控件
        tvVerify= (TextView) findViewById(R.id.tv_verify);
        // 忘记密码
        tvForgetPsw = (TextView) findViewById(R.id.tv_forgetPsw);
        // 账户登录
        tvAccount = (TextView) findViewById(R.id.tv_account);
        // 显示用户名
        tvNumber= (TextView)findViewById(R.id.tv_number);
        // 获取用户名  @boming.liao 16/9/14
        user_name = PrefUtils.getString(LockActivity.this, USER_NAME, null);
//        Log.d("user_name",user_name);
        if (user_name!=null){
            String front_3 = user_name.substring(0,3);
            String later_4 = user_name.substring(7);
            tvNumber.setText(front_3+"****"+later_4);
        }
        // 点击事件
        tvForgetPsw.setOnClickListener(this);
        tvAccount.setOnClickListener(this);

        // 设置背景颜色
        Resources res= getResources();
        Drawable drawable=res.getDrawable(R.drawable.red_backgroud);
        this.getWindow().setBackgroundDrawable(drawable);

        lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
        lockPatternView.setOnPatternListener(this);

    }

    private void initData() {
        String my_cookie = PrefUtils.getString(LockActivity.this, MY_COOKIE_STR, "");
//        Log.d("MyCookie-->10",my_cookie);
        if (close!=1){
            // 手势密码登录
            OkHttpUtils
                    .post()
                    .url(Constants.FIRST_URL2+"services/device.aspx?type=DeviceLogin")
                    .addHeader("cookie",LbApplication.getInstance().getSession())
                    .addParams("UserName", user_name)
                    .addParams("Password", patternToString2)
                    .addParams("IMEI",deviceId)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }
                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, "responseLogin:" + response);
                            CookieManager cookieManager = CookieManager.getInstance();
                            String cookie = cookieManager.getCookie(Constants.FIRST_URL2+"services/device.aspx");
                            LbApplication.getInstance().setSession(cookie);
                            parseData(response);
                        }
                    });
        }else{
            // 手势密码关闭
            OkHttpUtils
                    .post()
                    .url(Constants.FIRST_URL2+"services/device.aspx?type=CloseMemberDevice")
                    .addHeader("cookie",LbApplication.getInstance().getSession())
                    .addParams("UserName", user_name)
                    .addParams("Password", patternToString2)
                    .addParams("IMEI",deviceId)
                    .build()
                    .execute(new StringCallback() {

                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }
                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, "responseModify:" + response);
                            CookieManager cookieManager = CookieManager.getInstance();
                            String cookie = cookieManager.getCookie(Constants.FIRST_URL2+"services/device.aspx");
                            LbApplication.getInstance().setSession(cookie);
                            Gson gson = new Gson();
                            Data data= gson.fromJson(response, Data.class);
                            String rec= data.getRes();
                            // 如果关闭手势密码成功就清空在本地的手势密码的值
                            if ("1".equals(rec)) {
                                PrefUtils.remove(LockActivity.this,"Password");
                            }
                            // 关闭手势密码的时候存一个状态
                            PrefUtils.setBoolean(LockActivity.this,"CLOSE_STUTAS",true);
                            parseData(response);
                        }
                    });
        }
    }

    // 解析json数据
    private void parseData(String response) {
        Gson gson= new Gson();
        Data data= gson.fromJson(response, Data.class);
        res = data.getRes();
        Log.d("fromJson",res);
        PrefUtils.setString(LockActivity.this,"login_success",res);
        if ("1".equals(res)){
            // TODO: 2016/9/21
            // 如果手势密码与设置的手势密码相同就直接跳转到MainActivity
//            startActivity(new Intent(LockActivity.this,MainActivity.class));
            // 然后关闭该页面
            if(!TextUtils.isEmpty(in_splash)){
                if("in_splash".equals(in_splash)){
                    // 从splash页面过来
                    startActivity(new Intent(LockActivity.this,MainActivity.class));
                    finish();
                }
            }else if(!TextUtils.isEmpty(onrestart)){
                // 后台进前台后过来
                if("onrestart".equals(onrestart)){
                    startActivity(new Intent(LockActivity.this,MainActivity.class));
                    finish();
                }

            }else if(!TextUtils.isEmpty(OtherPager)){
                if("OtherPager".equals(OtherPager)){
                    setResult(8);
//                    startActivity(new Intent(LockActivity.this,MainActivity.class));
                    finish();
                }
            }
            else{
                //其他页面过来
                LockActivity.this.finish();
            }

        }else{
            count++;
            lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            // @boming.liao...
            // ToastUtil.getInstance(this).makeText(R.string.lockpattern_error);
            ToastUtil.getInstance(this).makeTextCenter("手势密码不对");
            // @boming.liao
            tvVerify.setText(R.string.lockpattern_error);
            // Toast.makeText(this, R.string.lockpattern_error, Toast.LENGTH_LONG).show();
            // @boming.liao ...清除绘制图案
            clearPattern();
            // lockPatternView.clearPattern();
            // @boming.liao
            if (count>=6){
                // @boming.liao
                // 跳转到登录界面
               // startActivity(new Intent(LockActivity.this,LoginActivity.class));
//                Intent intent = new Intent(LockActivity.this,MainActivity.class);

//                in_splash = intent.getStringExtra("in_splash");
//                onrestart = intent.getStringExtra("onrestart");
//
                if ("in_splash".equals(in_splash)){
                    Intent intent = new Intent(LockActivity.this, MainActivity.class);
                    intent.putExtra("other_account","other_account");
                    startActivity(intent);
                }else{
                    // Restart
                    Intent intent = new Intent();
                    intent.putExtra("login",1);
                    setResult(MAIN_RECODE1,intent);
//                startActivity(intent);
                    finish();
                }
            }
        }
    }

    // 复写返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // disable back key
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.main, menu);
    // return true;
    // }

    @Override
    public void onPatternStart() {
        Log.d(TAG,"onPatternStart");

    }

    @Override
    public void onPatternCleared() {
        Log.d(TAG,"onPatternCleared");
    }

    @Override
    public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
        Log.d(TAG,"onPatternCellAdded");
        Log.e(TAG,LockPatternView.patternToString(pattern));
        // Toast.makeText(this, LockPatternView.patternToString(pattern),
        // Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
        Log.d(TAG,"onPatternDetected");

        patternToString2=LockPatternView.patternToString2(pattern);
        PrefUtils.setString(LockActivity.this,"Password",patternToString2);
        // 调用接口
        initData();


//        if (pattern.equals(lockPattern)) {
//            Log.d(TAG,"LockActivitypattern"+pattern);
//            Log.d(TAG,"LockActivitylockPattern"+lockPattern);
//            // 如果手势密码与设置的手势密码相同就直接跳转到MainActivity
//            startActivity(new Intent(LockActivity.this,MainActivity.class));
//
//            // 然后关闭该页面
//            finish();
//        } else {
//            count++;
//            lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
//            // @boming.liao...
//            // ToastUtil.getInstance(this).makeText(R.string.lockpattern_error);
//              ToastUtil.getInstance(this).makeTextCenter("手势密码不对");
//            // @boming.liao
//            tvVerify.setText(R.string.lockpattern_error);
//            // Toast.makeText(this, R.string.lockpattern_error, Toast.LENGTH_LONG).show();
//            // @boming.liao ...清除绘制图案
//               clearPattern();
//            // lockPatternView.clearPattern();
//            // @boming.liao
//            if (count>=6){
//               // @boming.liao
//                // 跳转到登录界面
//                startActivity(new Intent(LockActivity.this,LoginActivity.class));
//                finish();
//            }
//        }



    }

    private void clearPattern() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 清除绘制手势密码
                 lockPatternView.clearPattern();
            }
        },300);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_forgetPsw:// 忘记密码
                PrefUtils.setBoolean(LockActivity.this, Constants.FORGER_PWD,true);
                if("in_splash".equals(in_splash)){
                    //  欢迎页跳转过来
                    Intent intent = new Intent(LockActivity.this, MainActivity.class);
                    intent.putExtra("forget_pwd","forget_pwd");
                    startActivity(intent);
                    finish();
                }else{
//                    Intent intent = new Intent(LockActivity.this, MainActivity.class);
//                    intent.putExtra("forget_pwd","forget_pwd");
//                    startActivity(intent);
                    Intent intent = new Intent();
                    intent.putExtra("login",1);
                    setResult(MAIN_RECODE1,intent);
                    finish();
                }
                PrefUtils.setBoolean(LockActivity.this,"include_houtaiAndSplash", true);
                // 当忘记手势密码的时候，清空设置手势密码状态 @boming.liao 929
               //  PrefUtils.remove(LockActivity.this,STATUS_MIMA);
               // 清空用户名
              //  PrefUtils.remove(LockActivity.this,USER_NAME);
                break;
            case R.id.tv_account:
                // 账户登录
                if ("in_splash".equals(in_splash)) {
                    // 在splash忘记密码用其他账户登录
                    Intent intent = new Intent(LockActivity.this, MainActivity.class);
                    intent.putExtra("other_account","other_account");
                    startActivity(intent);
                    finish();
//                    Intent intent =new Intent();
//                    intent.putExtra("login",1);
//                    setResult(MAIN_RECODE2,intent);
//                    finish();
                }else{
                    // 从后台忘记密码,然后从其他账户登录
                    Intent intent = new Intent();
                    intent.putExtra("login",1);
                    setResult(MAIN_RECODE1,intent);
                    finish();
                }
                PrefUtils.setBoolean(LockActivity.this,"include_houtaiAndSplash", false);
                // 当忘记手势密码的时候，清空设置手势密码状态 @boming.liao  929
               // PrefUtils.remove(LockActivity.this,STATUS_MIMA);
                // 清空用户名
              //  PrefUtils.remove(LockActivity.this,USER_NAME);
            break;
        }
    }
}
