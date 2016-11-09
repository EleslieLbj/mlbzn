package cn.lbzn.m;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.lbzn.m.Constants.Constants;
import cn.lbzn.m.Utils.PrefUtils;
import cn.lbzn.m.Utils.ToastUtil;
import cn.lbzn.m.View.LockPatternView;
import cn.lbzn.m.bean.Data;
import okhttp3.Call;

public class LockSetupActivity extends BaseActivity implements LockPatternView.OnPatternListener,View.OnClickListener {
    private static final String TAG = "LockSetupActivity";
    private static final int MAIN_RESULT = 1;
    private static final int MAIN_RECODE1 = 2;
    private static final int MAIN_RECODE2 = 3;
    private LockPatternView lockPatternView;
    private Button leftButton;
    private Button rightButton;
    private static final int STEP_1 = 1; // 开始
    private static final int STEP_2 = 2; // 第一次设置手势完成
    private static final int STEP_3 = 3; // 按下继续按钮
    private static final int STEP_4 = 4; // 第二次设置手势完成
    // private static final int SETP_5 = 4; // 按确认按钮
    public static final String USER_NAME = "user_name";
    public static final String HANDER_MIMA="hander_mima";
    public static final String HAND_ISCOLSE="hand_iscolse";
    private static final String MY_COOKIE_STR= "my_cookie_str";
    private  TextView tvNumber;
    // 跳过按钮
    private ImageView btn;

    // 首选项
    private  SharedPreferences preferences;
    // 创建Handler
   private Handler mHandler= new Handler(); ;
    private int step;

    private List<LockPatternView.Cell> choosePattern;

    private boolean confirm = false;
    private TextView tvText;
    private  int count = 0;
    private String deviceId;
    private String user_name;
    private String patternString;
    // @boming.liao 16/9/14
    private List<LockPatternView.Cell> lockPattern;
    private String  patternToString2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lock_setup);
        // 设置背景颜色
        Resources res= getResources();
        Drawable drawable = res.getDrawable(R.drawable.red_backgroud);
        this.getWindow().setBackgroundDrawable(drawable);

        lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
        lockPatternView.setOnPatternListener(this);
        leftButton = (Button) findViewById(R.id.left_btn);
        rightButton = (Button) findViewById(R.id.right_btn);
        tvText = (TextView) findViewById(R.id.tv_text);
        // 显示用户名
        tvNumber= (TextView)findViewById(R.id.tv_number);
        // 获取电话管理器
        TelephonyManager systemService = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // 获取imei
        deviceId = systemService.getDeviceId();
        // 初始化跳转按钮
        // @boming.liao
        btn= (ImageView) findViewById(R.id.bt_jump);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接跳转到MainActivity
                // TODO: 2016/9/21
//                startActivity(new Intent(LockSetupActivity.this,MainActivity.class));
                // 关闭此页
                finish();
            }
        });
        step = STEP_1;
        updateView();

        // 获取用户名  @boming.liao 16/9/14
        user_name = PrefUtils.getString(LockSetupActivity.this, USER_NAME, "");
//        Log.d("user_name",user_name);
            String front_3 = user_name.substring(0,3);
            String later_4 = user_name.substring(7);
            tvNumber.setText(front_3+"****"+later_4);
//        // 获取手势密码 @boming.liao 16/9/14
//        SharedPreferences preferences = getSharedPreferences(SplashActivity.LOCK,MODE_PRIVATE);
//        patternString = preferences.getString(SplashActivity.LOCK_KEY,null);
//        if (patternString!=null){
//            lockPattern = LockPatternView.stringToPattern(patternString);
//        }
//        // 把手势密码转换成字符串@boming.liao 16/9/14
//        patternToString2=LockPatternView.patternToString2(lockPattern);

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

    private void initData() {
        // 获取cookie
        String my_cookie = PrefUtils.getString(LockSetupActivity.this, MY_COOKIE_STR, "");
        // 设置手势密码
        OkHttpUtils
                .post()
                .url(Constants.FIRST_URL2+"services/device.aspx?type=SetMemberDevice")
                .addHeader("cookie",my_cookie)
                .addParams("UserName", user_name)
                .addParams("Password", patternToString2)
                .addParams("IMEI",deviceId)
                .addParams("Device","android")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }
                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "responseSetting:" + response);
                        parseData(response);
                    }
                });
    }

    // 解析json数据
    private void parseData(String response) {
        Gson gson= new Gson();
        Data data= gson.fromJson(response, Data.class);
        String str=data.res;
        Log.d("fromJson",str);
        if("1".equals(str)){
            //关闭当前页面
            LockSetupActivity.this.finish();
        }else{
            LockSetupActivity.this.finish();
        }


    }

    private void updateView() {
        switch (step) {
            case STEP_1:
                leftButton.setText(R.string.cancel);
                // 如果开始设置手势密码,就把右边按钮设置为空,并且设置为不可用
                rightButton.setText("");
                rightButton.setEnabled(false);
                // 第一次设置密码的时候把集合里面的内容清空
                choosePattern = null;
                confirm = false;
                // 首先一进来要清除线条
                lockPatternView.clearPattern();
                // 能输入密码
                lockPatternView.enableInput();
                // @boming.liao 由于现在按钮没有起作用,然后我把它隐藏
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);
                break;
            case STEP_2:
                leftButton.setText(R.string.try_again);
                rightButton.setText(R.string.goon);
                rightButton.setEnabled(true);
                // 清除绘制的线条
                // @boming.liao
                clearPattern();
              //  lockPatternView.clearPattern();
                tvText.setText("请再次绘制解锁图案");
                // @boming.liao
                // lockPatternView.disableInput();
                lockPatternView.enableInput();
                // @boming.liao
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);
                break;
            case STEP_3:
                leftButton.setText(R.string.cancel);
                rightButton.setText("");
                rightButton.setEnabled(false);
                lockPatternView.clearPattern();
                lockPatternView.enableInput();
                // @boming.liao
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);
                break;
            case STEP_4:
                leftButton.setText(R.string.cancel);
                if (confirm) {
                    rightButton.setText(R.string.confirm);
                    rightButton.setEnabled(true);
                    // 清除绘制的线条
                    // @boming.liao
                    clearPattern();
                 //   lockPatternView.clearPattern();
                    tvText.setText("恭喜您,设置成功!");
                    // @boming.liao
                    // lockPatternView.disableInput();
                    lockPatternView.enableInput();
                    // @boming.liao
                   // leftButton.setVisibility(View.GONE);
                   // rightButton.setVisibility(View.GONE);

                    // @boming.liao ....SharedPreferences
                    // 首选项 保存手势密码
                     preferences = getSharedPreferences(
                            SplashActivity.LOCK, MODE_PRIVATE);
                    preferences
                            .edit()
                            .putString(SplashActivity.LOCK_KEY, LockPatternView.patternToString(choosePattern))
                            .commit();
                    patternToString2=LockPatternView.patternToString2(choosePattern);
                    // 把手势密码保存到文件里
                    PrefUtils.setString(LockSetupActivity.this,HANDER_MIMA,patternToString2);
                    // 删除之前的状态
                    PrefUtils.remove(LockSetupActivity.this,HAND_ISCOLSE);
                    // 设置手势密码打开状态
                    PrefUtils.setBoolean(LockSetupActivity.this,HAND_ISCOLSE,false);
                    initData();
                    // TODO: 2016/9/21
                    // 从手势密码页面跳到校验密码页
//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Intent intent = new Intent(LockSetupActivity.this, LockActivity.class);
////                            Intent intent = new Intent(LockSetupActivity.this, MainActivity.class);
////                            startActivity(intent);
//                            // 关闭当前的页面
//                            LockSetupActivity.this.finish();
//                        }
//                    },0);


                } else {

                    if(count < 6 ){
                        rightButton.setText("");
                        lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                        lockPatternView.enableInput();
                        rightButton.setEnabled(false);
                        // @bomig.liao
                        tvText.setText("与上一次输入不一致,请重新设置");
                        // @boming.liao
                        // Toast.makeText(this, "第一次和第二次绘制的密码不一致!", Toast.LENGTH_SHORT).show();
                        ToastUtil.getInstance(this).makeText("与上一次输入不一致,请重新设置");
                        // 提示显示到中间
                      //  String pwd_error= getResources().getString(R.string.lockpattern_error);
                      //  ToastUtil.getInstance(this).makeTextCenter(pwd_error);
                        //@boming.liao
                        // 如果是取消设置就清除手势密码
                        //  getSharedPreferences(SplashActivity.LOCK, MODE_PRIVATE).edit().clear().commit();
                        // @boming.liao
                        // 设置confirm为true
                        // confirm=true;
                        // 清除绘制的线条
                        // @boming.liao
                        clearPattern();
                      //  lockPatternView.clearPattern();
                        // @boming.liao
                        leftButton.setVisibility(View.GONE);
                        rightButton.setVisibility(View.GONE);
                    }else{
                        // @boming.liao
//                        startActivity(new Intent(LockSetupActivity.this,LoginActivity.class));
//                        Intent intent = new Intent(LockSetupActivity.this,MainActivity.class);
//                        intent.putExtra("login",1);
//                        startActivity(intent);
//                        // 关闭此页
//                         finish();
                        Intent intent = new Intent();
                        intent.putExtra("login",1);
                        setResult(MAIN_RECODE2,intent);
                        finish();
                    }
                }

                break;

            default:
                break;
        }
    }

    private void clearPattern() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 清除绘制密码
                lockPatternView.clearPattern();
            }
        },300);

    }

    // 点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                if (step == STEP_1 || step == STEP_3 || step == STEP_4) {
                    finish();
                } else if (step == STEP_2) {
                    step = STEP_1;
                    updateView();
                }
                break;

            case R.id.right_btn:
                if (step == STEP_2) {
                    step = STEP_3;
                    updateView();
                } else if (step == STEP_4) {

                    SharedPreferences preferences = getSharedPreferences(
                            SplashActivity.LOCK, MODE_PRIVATE);
                    preferences
                            .edit()
                            .putString(SplashActivity.LOCK_KEY, LockPatternView.patternToString(choosePattern))
                            .commit();

                    // 从手势密码页面跳到校验密码页
                    Intent intent = new Intent(this, LockActivity.class);
                    startActivity(intent);
                    // 关闭当前的页面
                    finish();
                }

                break;

            default:
                break;
        }

    }

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

    }

    @Override
    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
        Log.d(TAG,"onPatternDetected");
        if (pattern.size() < LockPatternView.MIN_LOCK_PATTERN_SIZE) {
            // @boming.liao
            clearPattern();
           // lockPatternView.clearPattern();
            // 吐司提示至少链接4个点
            tvText.setText("至少连接4个点,请重试!");
            // @boming.liao
           // Toast.makeText(this,R.string.lockpattern_recording_incorrect_too_short,Toast.LENGTH_LONG).show();
            ToastUtil.getInstance(this).makeText(R.string.lockpattern_recording_incorrect_too_short);
            // DisplayMode 是一个枚举类,里面放的是几种状态
            lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
            // @boming.liao 打印一下pattern的大小
            Log.d(TAG,"pattern_size" + pattern.size());
            return;
        }

        if (choosePattern == null) {
            // 如果choosePattern为空 把原有的集合赋值到现在新创建的集合里面
            choosePattern = new ArrayList<LockPatternView.Cell>(pattern);

            // Log.d(TAG, "choosePattern = "+choosePattern.toString());
            // Log.d(TAG, "choosePattern.size() = "+choosePattern.size());
            Log.d(TAG,"choosePattern++++++++++++ = "+ Arrays.toString(choosePattern.toArray()));
            // Log.d(TAG,"choosePattern_choosePattern = "+ choosePattern);
            // Log.d(TAG,"choosePattern_choosePattern_toString = "+ choosePattern.toString());
            step = STEP_2;
            updateView();
            return;
        }
        // [(row=1,clmn=0), (row=2,clmn=0), (row=1,clmn=1), (row=0,clmn=2)]
        // [(row=1,clmn=0), (row=2,clmn=0), (row=1,clmn=1), (row=0,clmn=2)]
        Log.d(TAG,"choosePattern = "+ Arrays.toString(choosePattern.toArray()));
        Log.d(TAG,"pattern = "+ Arrays.toString(pattern.toArray()));

        if (choosePattern.equals(pattern)) {
            // Log.d(TAG, "pattern = "+pattern.toString());
            // Log.d(TAG, "pattern.size() = "+pattern.size());
            Log.d(TAG, "pattern = " + Arrays.toString(pattern.toArray()));

            confirm = true;
        } else {

            // @boming.liao
            // 清除集合里面的内容
            //  pattern.clear();
           //  choosePattern.clear();
            step = STEP_1;
            updateView();
              confirm = false;
              count++;
        }
        step = STEP_4;
        updateView();
    }
}
