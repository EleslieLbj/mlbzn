/**
 *
 */
package cn.lbzn.m.Utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

public class AppUtil {

	/**
	 * 获取屏幕分辨率
	 * @param context
	 * @return
	 */
	public static int[] getScreenDispaly(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int width = windowManager.getDefaultDisplay().getWidth();// 手机屏幕的宽度
		int height = windowManager.getDefaultDisplay().getHeight();// 手机屏幕的高度
		int result[] = { width, height };
		return result;
	}

    public static String getDeviceId(Context mContext) {
        // 获取电话管理器
        TelephonyManager systemService = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        // 获取imei
        String deviceId = systemService.getDeviceId();
        return deviceId;
    }
}
