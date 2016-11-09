package cn.lbzn.m.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.android.download.DownLoadManager;
import cn.android.download.DownloadError;
import cn.android.download.DownloadListener;
import cn.lbzn.m.Constants.Constants;
import okhttp3.Call;


/**
 * 升级管理
 * 
 * @author lucio
 * 
 */
public class UpdateManager {

	// 首选项名称
	public static final String PREFERENCE_NAME = "PREFERENCE_HULUTAN_CL";

	private Context mContext;
	private UpdateCallBack mUpdateCallBack;
	private boolean isLoading = false;
	private String mFilePaht = null;
	private String mFileName = null;
	private String mUrl = null;

	public UpdateManager(Context context) {
		this.mContext = context;
	}

	/**
	 * 检查更新
	 * 
	 * @param
	 *
	 * @param updateCallBack
	 *            完成回调
	 */
	public void checkUpdate(UpdateCallBack updateCallBack) {
		if (isLoading) {
			return;
		}
		isLoading = true;
		this.mUpdateCallBack = updateCallBack;
//		final int version = Integer.parseInt(YktApplication.get().versionCode);
		Map<String, String> params = new HashMap<>();
//        String downloadUrl = "";
//        弹出更新提示框
//        updateDialog(downloadUrl,false,"本次更新内容:\n 1.优化首页\n 2.修复若干bug\n 3.优化网络访问速度");

                OkHttpUtils
                        .post()
                        .url(Constants.FIRST_URL2+"services/device.aspx?type=GetAppVersion&os=android")
//                        .addParams("osv", Tools.getVersionCode(mContext))
//                        .addParams("channel",Tools.getMetaData(mContext,"UMENG_CHANNEL"))
                        .addParams("os","android")
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                e.printStackTrace();
                                // 对外执行回调
                                if (null != mUpdateCallBack) {
                                    mUpdateCallBack.cancel();
                                    mUpdateCallBack = null;
                                }
                            }
                            @Override
                            public void onResponse(String response, int id) {
                                Log.d("UpdateManager", "test:" + response);
                                try {
                                    JSONObject obj = new JSONObject(response);
//                                        String data = obj.optString("data");
//                                        JSONObject jsonObject = new JSONObject(data);
                                        // downloadUrl  apk下载链接
                                        String downloadUrl = obj.optString("downloadurl");
                                        // message 更新文案
                                        String message = obj.optString("message");
                                        // forceUpdate 强制更新参数  0 不强制更新   1 强制更新
//                                        int forceUpdate = obj.optInt("forceUpdate");
                                         int forceUpdate = Integer.parseInt(obj.optString("forceUpdate"));
                                        //update 更新参数  0 不需要更新  1 需要更新
//                                        int update = jsonObject.optInt("update");
                                        // 获取版本号
                                        int version=Integer.parseInt(obj.optString("version"));
                                        boolean isMustUpdate = false;
                                        if(version<=Integer.parseInt(Tools.getVersionCode(mContext))&& TextUtils.isEmpty(downloadUrl)){
                                            //不需要更新
                                            if (null != mUpdateCallBack) {
                                                mUpdateCallBack.checkUpdateComplete(false);
                                                mUpdateCallBack = null;
                                            }
                                        }else if(version>Integer.parseInt(Tools.getVersionCode(mContext)) && TextUtils.isEmpty(downloadUrl)){
                                            //更新
                                            //判断是否强制更新
                                            if(forceUpdate==1){
                                                //强制更新
                                                isMustUpdate = true;
                                            }else if(forceUpdate==0){
                                                //普通更新
                                                isMustUpdate = false;
                                            }
                                            // 是否重新走引导页置为false
                                            PrefUtils.setBoolean(mContext,"is_user_guide_showed",false);
                                            //弹出更新提示框
                                            updateDialog(downloadUrl,isMustUpdate,message);
                                        }else{
                                            //不需要更新
                                            if (null != mUpdateCallBack) {
                                                mUpdateCallBack.checkUpdateComplete(false);
                                                mUpdateCallBack = null;
                                            }
                                        }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    //不需要更新
                                    if (null != mUpdateCallBack) {
                                        mUpdateCallBack.checkUpdateComplete(false);
                                        mUpdateCallBack = null;
                                    }
                                }
                   }
               });

//        NetworkController.getInstance().checkUpdata(params, new NetworkCallBack() {
//			@Override
//			public void response(String response) {
//				try {
//					JSONObject obj = new JSONObject(response);
//					int errcode = obj.optInt("errcode");
//					String errmsg = obj.optString("errmsg");
//					if(errcode== 0){
//						String data = obj.optString("data");
//						JSONObject jsonObject = new JSONObject(data);
//                        // downloadUrl  apk下载链接
//						String downloadUrl = jsonObject.optString("downloadUrl");
//                        // message 更新文案
//						String message = jsonObject.optString("message");
//                        // forceUpdate 强制更新参数  0 不强制更新   1 强制更新
//						int forceUpdate = jsonObject.optInt("forceUpdate");
//                        //update 更新参数  0 不需要更新  1 需要更新
//						int update = jsonObject.optInt("update");
//						boolean isMustUpdate = false;
//						if(update==0){
//							//不需要更新
//							if (null != mUpdateCallBack) {
//								mUpdateCallBack.checkUpdateComplete(false);
//								mUpdateCallBack = null;
//							}
//						}else if(update==1){
//							//更新
//							//判断是否强制更新
//							if(forceUpdate==1){
//								//强制更新
//								isMustUpdate = true;
//							}else if(forceUpdate==0){
//								//普通更新
//								isMustUpdate = false;
//							}
//							//弹出更新提示框
//							updateDialog(downloadUrl,isMustUpdate,message);
//						}
//					}else{
////						ToastUtil.getInstance(mContext).makeText(Utils.convertMsg(errmsg));
//						if (null != mUpdateCallBack) {
//							mUpdateCallBack.checkUpdateComplete(false);
//							mUpdateCallBack = null;
//						}
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void error(int errorCode, String errorLog) {
////				ToastUtil.getInstance(mContext).makeText(Utils.convertMsg(errorLog));
//				// 对外执行回调
//				if (null != mUpdateCallBack) {
//					mUpdateCallBack.cancel();
//					mUpdateCallBack = null;
//				}
//			}
//		});
	}


	/**
	 * 弹出更新提示框
	 * */
	private void updateDialog(final String downloadUrl, boolean isMustUpdate, String message) {
		final boolean ifMustUpdate = isMustUpdate;
		AlertDialog.Builder mBuilder = new Builder(mContext);
		mBuilder.setCancelable(false);
		mBuilder.setMessage(message+"");
		mBuilder.setTitle("更新提醒");
		mBuilder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// mFileName = "2010522";
				mFileName = "lban";
				// mUrl = "http://gameykt-packages.stor.sinaapp.com/ykt.apk";
				mUrl = downloadUrl;
				// 执行下载
				doDownload(mFileName, mUrl, false, ifMustUpdate);
			}
		});
		if (!ifMustUpdate) {
			mBuilder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// 对外执行回调
					if (null != mUpdateCallBack) {
						mUpdateCallBack.cancel();
						mUpdateCallBack = null;
					}
				}
			});
//			mBuilder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					// 对外执行回调
//					if (null != mUpdateCallBack) {
//						mUpdateCallBack.close();;
//						mUpdateCallBack = null;
//					}
//				}
//			});
		}
		// 创建提示框
		mBuilder.create().show();
	}

	/**
	 * 下载文件
	 * 
	 * @param
	 *
	 * @param url
	 */
	private void doDownload(String fileName, String url, boolean forceDownload, final boolean ifMustUpdate) {
		DownLoadManager.getInstance().download(mContext, url, null, fileName, forceDownload, null, null, new DownloadListener() {

			@Override
			public void onSuccess(final String url, String realUrl, final String contentType, final File f) {
				mFilePaht = f.getPath();
				progressDialog.setCancelable(true);
				ApkUtils.installApk(mContext, mFilePaht);
			}

			@Override
			public void onStart(String url, String realUrl) {
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setProgress(0);
					}
				});
			}

			@Override
			public void onLoading(final long current, final long count) {
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setProgress(FileUtils.getSize(current, count) * 100);
					}
				});
			}

			@Override
			public void onFailure(final DownloadError error) {
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (error.getErrorType() == DownloadError.ErrorType.DOWNLOADFILE_EXISTING) {
							File file = error.getFile();
							if (null != file) {
								ApkUtils.installApk(mContext, file.getPath());
							} else {
								doDownload(mFileName, mUrl, true, ifMustUpdate);
							}
						} else {
							ToastUtil.getInstance(mContext).makeText(error.getMessage());
							if (null != mUpdateCallBack) {
								mUpdateCallBack.cancel();
								mUpdateCallBack = null;
							}
						}
						if (!ifMustUpdate) {
							if (null != mUpdateCallBack) {
								mUpdateCallBack.cancel();
								mUpdateCallBack = null;
							}
						}
					}
				});
			}
		});
	}

	private ProgressDialog progressDialog;

	private void setProgress(double progress) {
		try {
			if (null == progressDialog) {
				// 创建ProgressDialog对象
				progressDialog = new ProgressDialog(mContext);
				// 设置进度条风格，风格为长形
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				// 设置ProgressDialog 标题
				progressDialog.setTitle("更新提示");
				// // 设置ProgressDialog 提示信息
				progressDialog.setMessage("正在下载更新包,请稍候...");
				// 设置ProgressDialog 标题图标
				// progressDialog.setIcon(R.drawable.icon_ykt);
				progressDialog.setMax(100);
				// 设置ProgressDialog 进度条进度
				// progressDialog.setProgress(100);
				// 设置ProgressDialog 的进度条是否不明确
				progressDialog.setIndeterminate(false);
				// 设置ProgressDialog 是否可以按退回按键取消
				progressDialog.setCancelable(false);
				// 让ProgressDialog显示
				progressDialog.show();
			}
			progressDialog.setProgress((int) progress);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    // 更新回调
	public interface UpdateCallBack {
		public void checkUpdateComplete(boolean update);

		public void cancel();
		
		public void close();
	}
}
