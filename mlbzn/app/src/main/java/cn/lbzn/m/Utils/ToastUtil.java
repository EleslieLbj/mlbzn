package cn.lbzn.m.Utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Toast封装，可避免4秒之内重复出现。
 */
public class ToastUtil {

	private HashMap<Object, Long> map = new HashMap<Object, Long>();

	private static ToastUtil toast;

	private Context context;

	private ToastUtil(Context context) {
		this.context = context.getApplicationContext();
	}

	public static ToastUtil getInstance(Context context) {
		if (toast == null) {
			toast = new ToastUtil(context);
		}
		return toast;
	}

	/**
	 * 显示Toast,显示时间为{@link Toast#LENGTH_LONG}
	 * 
	 * @param res
	 *            　显示字符串的resourceId
	 * @param
	 */
	public void makeText(int res) {
		makeText(res, Toast.LENGTH_SHORT);
	}

	/**
	 * 显示toast,显示时间为{@link Toast#LENGTH_LONG}
	 */
	public void makeText(String str) {
		if (str != null && str.length() > 0) {
			makeText(str, Toast.LENGTH_SHORT);
		}
	}

	/**
	 * 屏幕中间谈Toast
	 * 
	 * @param str
	 */
	public void makeTextCenter(String str) {
		Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * 显示Toast
	 * 
	 * @param str
	 *            显示文字
	 * @param type
	 *            需要显示的时间{@link Toast#LENGTH_SHORT}|{@link Toast#LENGTH_LONG}
	 * @param
	 */
	public void makeText(String str, int type) {
		if (map.get(str) == null || System.currentTimeMillis() - map.get(str) > 10) {
			Toast.makeText(context, str, type).show();
			map.put(str, System.currentTimeMillis());
		}
	}

	/**
	 * 显示Toast
	 * 
	 * @param res
	 *            显示字符串的资源id
	 * @param type
	 *            需要显示的时间{@link Toast#LENGTH_SHORT}|{@link Toast#LENGTH_LONG}
	 * @param
	 */
	public void makeText(int res, int type) {
		if (map.get(res) == null || System.currentTimeMillis() - map.get(res) > 2000) {
			Toast.makeText(context, res, type).show();
			map.put(res, System.currentTimeMillis());
		}
	}

}
