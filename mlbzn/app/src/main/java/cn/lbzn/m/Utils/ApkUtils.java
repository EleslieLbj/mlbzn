package cn.lbzn.m.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import java.io.File;

/**
 * APK相关操作
 */
public class ApkUtils {
	public static final String APP_PATH = "/data/app/";
	public static final int FLAG_UNINSTALLED = 0;
	public static final int FLAG_LOW_VERSION_INSTALLED = 1;
	public static final int FLAG_SAME_INSTALLED = 2;

	/**
	 * 安装APK
	 * 
	 * @param context
	 * @param apkPath
	 */
	public static void installApk(Context context, String apkPath) {
		context.startActivity(getApkInstallIntent(apkPath));
	}

	/**
	 * 获取APK安装的意图
	 * 
	 * @param apkPath
	 * @return
	 */
	public static Intent getApkInstallIntent(String apkPath) {
		File apk = new File(apkPath);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(apk),
				"application/vnd.android.package-archive");
		return intent;
	}

	/**
	 * 判断是否需要安装
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static int isApkNeedInstall(Context context, String packageName) {
		int install = ApkUtils.isApkNeedInstall(context, packageName, 0, false);
		// 如果已经安装，显示进入
		return install;
	}

	/**
	 * 判断APP是需要安装
	 * 
	 * @param context
	 * @param packageName
	 *            APP的包名
	 * @param versionCode
	 *            要安装APP的版本
	 * @param deleteOld
	 * @return
	 */
	public static int isApkNeedInstall(Context context, String packageName,
                                       int versionCode, boolean deleteOld) {
		PackageManager pm = context.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = pm.getPackageInfo(packageName,
					PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
			// 没有找到，可以安装
			return FLAG_UNINSTALLED;
		}
		if (packageInfo != null) {
			int installedVersion = packageInfo.versionCode;
			if (installedVersion < versionCode) {
				// 删除
				if (deleteOld) {
					// deleteApkAsRoot(null, context,
					// packageInfo.applicationInfo.publicSourceDir);
				}
				return FLAG_LOW_VERSION_INSTALLED;
			}
		}
		return FLAG_SAME_INSTALLED;
	}

	/**
	 * 获取应用安装日期
	 * 
	 * @param appInfo
	 * @return
	 */
	public static long getAppInstallTime(ApplicationInfo appInfo) {
		File appFile = new File(appInfo.sourceDir);
		return appFile.lastModified();
	}

}
