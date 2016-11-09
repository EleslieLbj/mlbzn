package cn.lbzn.m.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import cn.lbzn.m.Constants.Constants;
import cn.lbzn.m.LockActivity;
import cn.lbzn.m.Utils.PrefUtils;

public class ScreenService extends Service {
    public static final String STATUS_MIMA = "status_mima";
    public ScreenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /* 注册屏幕唤醒时的广播 */
        IntentFilter mScreenOnFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        ScreenService.this.registerReceiver(mScreenOReceiver, mScreenOnFilter);

        /* 注册机器锁屏时的广播 */
        IntentFilter mScreenOffFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        ScreenService.this.registerReceiver(mScreenOReceiver, mScreenOffFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ScreenService.this.unregisterReceiver(mScreenOReceiver);
    }

    /**
     * 锁屏的管理类叫KeyguardManager，
     * 通过调用其内部类KeyguardLockmKeyguardLock的对象的disableKeyguard方法可以取消系统锁屏，
     * newKeyguardLock的参数用于标识是谁隐藏了系统锁屏
     */
    private String lastLoadTime;
    private BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 获取从黑屏到亮屏的时间
            lastLoadTime = PrefUtils.getString(context, Constants.CURRENT_LOAD_TIME, 0 + "");
            if (action.equals("android.intent.action.SCREEN_ON")) {
                String statue_pwd = PrefUtils.getString(context, STATUS_MIMA, "");
                System.out.println("—— SCREEN_ON ——");
                // 调起输入手势密码
                if ("1".equals(statue_pwd)) {// 判断表示设置过手势密码
                    if (System.currentTimeMillis() - Long.parseLong(lastLoadTime) > 1  *1000) {
                        PrefUtils.setBoolean(ScreenService.this,"service",true);
                        intent = new Intent(ScreenService.this, LockActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                System.out.println("—— SCREEN_OFF ——");
                // 当屏幕暗的时候获取当前时间
                PrefUtils.setString(context, Constants.CURRENT_LOAD_TIME, System.currentTimeMillis() + "");
                // 移除状态
                PrefUtils.setBoolean(ScreenService.this,"service",false);
            }
        }
    };
}
