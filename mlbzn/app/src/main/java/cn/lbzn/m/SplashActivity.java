package cn.lbzn.m;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import cn.lbzn.m.Utils.PrefUtils;
import cn.lbzn.m.Utils.UpdateManager;
import cn.lbzn.m.Utils.Utils;
import cn.lbzn.m.bean.Data;

public class SplashActivity extends BaseActivity {


    private static final String TAG = "MainActivity";
    public static final String LOCK = "lock";
    public static final String LOCK_KEY = "lock_key";
    private RelativeLayout root;
    private String lockPattenString;
    public static final String HAND_ISCOLSE="hand_iscolse";
    public static final String STATUS_MIMA = "status_mima";
    private static final String MY_COOKIE_STR= "my_cookie_str";
    private String status_mima;
    private Handler mHandler= new Handler();
    private String deviceId;
    private String user_name;
    private String  res;
    public static final String USER_NAME = "user_name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        // 获取电话管理器
        TelephonyManager systemService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // 获取imei
        deviceId = systemService.getDeviceId();
        Log.e("deviceId:", deviceId);
        // 获取用户名
        user_name = PrefUtils.getString(SplashActivity.this, USER_NAME, "");
        // 隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 去掉标题
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        // 保存手势密码,如果有手势密码直接跳到密码页
        SharedPreferences preferences = getSharedPreferences(LOCK, MODE_PRIVATE);
        lockPattenString= preferences.getString(LOCK_KEY, null);
        new UpdateManager(this).checkUpdate(new UpdateManager.UpdateCallBack() {
            @Override
            public void checkUpdateComplete(boolean update) {
                // 判断是否更新
                if(!update){
                // 取消更新
                  init();
                }

            }
            @Override
            public void cancel() {
                // 取消更新
                   init();
            }

            @Override
            public void close() {

            }
        });
    }

    /**
     * 实时获取手势密码登录状态
     */
    private void getPwdStatus() {
        String user_name = PrefUtils.getString(SplashActivity.this, USER_NAME, "");
        Log.e("test","user_name==="+user_name);
        if(!TextUtils.isEmpty(user_name)){
            Utils.getPwdStatus(user_name, SplashActivity.this, new Utils.callback() {

                        @Override
               public void onError(boolean error_code) {
                            if(error_code){
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            }
                        }

                        @Override
                public void onResponse(String status) {
                    Log.e("test","状态: "+status);
                    if ("1".equals(status)){
                        Intent intent = new Intent(SplashActivity.this, LockActivity.class);
                        intent.putExtra("in_splash","in_splash");
                        startActivity(intent);
                    }else {
                        // 跳转到手势密码设置页面
                        // startActivity(new Intent(SplashActivity.this, LockSetupActivity.class));
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }
            }

            );
        }else{
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
    }

    private void init() {
        root=(RelativeLayout)findViewById(R.id.rl_root);
        // 跳转页面
        // jumpNextPage();
        // 开启动画
        startAnim();
    }
    // 解析json数据
    private void parseData(String result) {
        Gson gson = new Gson();
        Data data = gson.fromJson(result, Data.class);
        res = data.getRes();
    }

    private void startAnim() {
        // 渐变动画
        AlphaAnimation alpha = new AlphaAnimation(0.8f, 1.0f);
        alpha.setDuration(2000);// 动画时间
        alpha.setFillAfter(true);// 保持动画状态
        // 设置动画监听
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            // 动画结束
            @Override
            public void onAnimationEnd(Animation animation) {
                // 跳转页面
                jumpNextPage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        // 开始动画
        root.startAnimation(alpha);
    }

    private void jumpNextPage() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean is_user_guide_showed = PrefUtils.getBoolean(SplashActivity.this, "is_user_guide_showed", false);
                if (!is_user_guide_showed){
                    // 跳转到新手引导页
                    startActivity(new Intent(SplashActivity.this,GuideActivity.class));
                }else{
                    // 获取手势密码状态
                    getPwdStatus();
                }
                // 关闭当前页面
                finish();
            }
        },3000);
    }
}
