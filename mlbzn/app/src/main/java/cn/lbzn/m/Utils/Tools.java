package cn.lbzn.m.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Description ${TODO}
 * Author boming.liao
 * Date   2016/10/24 23:34
 **/
public class Tools {

    /**
     * 获取Meta Data 配置
     *
     * @param context
     * @param key
     *            Meta_Data name
     * @return
     */
    public static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本号
     * @param context
     * @return
     */
    public static String getVersionCode(Context context){
        try{
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String  versionCode = String.valueOf(info.versionCode);
            return versionCode;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}
